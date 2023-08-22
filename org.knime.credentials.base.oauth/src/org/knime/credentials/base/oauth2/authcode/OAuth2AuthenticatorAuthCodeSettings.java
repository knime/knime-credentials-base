/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   2023-06-07 (Alexander Bondaletov, Redfield SE): created
 */
package org.knime.credentials.base.oauth2.authcode;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.webui.node.dialog.defaultdialog.dataservice.DialogDataServiceHandlerResult;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.NodeSettingsPersistorWithConfigKey;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.rule.Effect;
import org.knime.core.webui.node.dialog.defaultdialog.rule.Effect.EffectType;
import org.knime.core.webui.node.dialog.defaultdialog.rule.Signal;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ValueSwitchWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.button.ButtonWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.button.CancelableActionHandler;
import org.knime.credentials.base.CredentialCache;
import org.knime.credentials.base.oauth.api.scribejava.AuthCodeFlow;
import org.knime.credentials.base.oauth2.base.ConfidentialAppSettings;
import org.knime.credentials.base.oauth2.base.OAuth2AuthenticatorSettings;
import org.knime.credentials.base.oauth2.base.PublicAppSettings;
import org.knime.credentials.base.oauth2.base.ScopeSettings;
import org.knime.credentials.base.oauth2.base.Sections.AppSection;
import org.knime.credentials.base.oauth2.base.Sections.Footer;
import org.knime.credentials.base.oauth2.base.Sections.ScopesSection;
import org.knime.credentials.base.oauth2.base.Sections.ServiceSection;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * Node settings of the (interactive) OAuth2 Authenticator node.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
@SuppressWarnings("restriction")
class OAuth2AuthenticatorAuthCodeSettings implements OAuth2AuthenticatorSettings {

    private static final NodeLogger LOG = NodeLogger.getLogger(OAuth2AuthenticatorAuthCodeSettings.class);

    @Widget(title = "Service type", description = """
            Whether to connect to a standard OAuth service from a predefined list, or
            to manually specify endpoint URLs.""")
    @Layout(ServiceSection.TypeChooser.class)
    @ValueSwitchWidget
    @Signal(condition = IsStandardService.class)
    ServiceType m_serviceType = ServiceType.STANDARD;

    @Widget(title = "Service", description = "A standard OAuth service from a predefined list.")
    @Layout(ServiceSection.Standard.class)
    @Effect(signals = IsStandardService.class, type = EffectType.SHOW)
    StandardService m_standardService;

    CustomServiceSettings m_customService = new CustomServiceSettings();

    @Widget(title = "Client/App type", description = CLIENT_TYPE_DESCRIPTION)
    @Layout(AppSection.TypeChooser.class)
    @ValueSwitchWidget
    @Signal(condition = IsPublicApp.class)
    AppType m_appType = AppType.PUBLIC;

    PublicAppSettings m_publicApp = new PublicAppSettings();

    ConfidentialAppSettings m_confidentialApp = new ConfidentialAppSettings();

    @Widget(title = "Redirect URL (should be http://localhost:XXXXX)", description = """
            The redirect URL to be used at the end of the interactive login. Should be chosen as http://localhost:XXXXX
            with a random number in the 10000 - 65000 range to avoid conflicts. Often, the redirect URL is part of the
            client/app registration at the OAuth2 service.
            """)
    @Layout(AppSection.Bottom.class)
    String m_redirectUrl = "http://localhost:43769";

    @Layout(ScopesSection.class)
    ScopeSettings m_scopes = new ScopeSettings();

    @ButtonWidget(invokeButtonText = "Login", //
            cancelButtonText = "Cancel login", //
            succeededButtonText = "Login again", //
            actionHandler = LoginActionHandler.class, //
            isMultipleUse = true, //
            showTitleAndDescription = false)
    @Widget(title = "Login", //
            description = "Clicking on login opens a new browser window/tab which "
            + "allows to interactively log into the service.")
    @Persist(optional = true, hidden = true, customPersistor = TokenCacheKeyPersistor.class)
    @Layout(Footer.class)
    UUID m_tokenCacheKey;

    OAuth2AuthenticatorAuthCodeSettings() {
    }

    enum GrantType {
        AUTH_CODE, IMPLICIT;
    }

    static class LoginActionHandler extends CancelableActionHandler<UUID, OAuth2AuthenticatorAuthCodeSettings> {

        @Override
        protected Future<DialogDataServiceHandlerResult<UUID>> invoke(
                final OAuth2AuthenticatorAuthCodeSettings settings, final SettingsCreationContext context) {

            return KNIMEConstants.GLOBAL_THREAD_POOL.enqueue(() -> doDialogLogin(settings, context));
        }

        private static DialogDataServiceHandlerResult<UUID> doDialogLogin(
                final OAuth2AuthenticatorAuthCodeSettings settings, final SettingsCreationContext context) {

            try {
                settings.validate(context.getCredentialsProvider().orElseThrow());
            } catch (InvalidSettingsException e) { // NOSONAR
                return DialogDataServiceHandlerResult.fail(e.getMessage());
            }

            try {
                var tokenHolder = new OAuth2AccessTokenHolder();
                tokenHolder.m_token = fetchAccessToken(settings, context);
                tokenHolder.m_cacheKey = CredentialCache.store(tokenHolder);
                return DialogDataServiceHandlerResult.succeed(tokenHolder.m_cacheKey);
            } catch (Exception e) {
                LOG.debug("Interactive login failed: " + e.getMessage(), e);
                return DialogDataServiceHandlerResult.fail(e.getMessage());
            }
        }
    }

    /**
     * Performs interactive login. The method will be called by the login button in
     * the dialog.
     *
     * @param settings
     *            The node settings.
     * @param context
     * @return the {@link OAuth2AccessToken} if the login was successful.
     * @throws Exception
     *             if the login failed for some reason.
     */
    static OAuth2AccessToken fetchAccessToken(final OAuth2AuthenticatorAuthCodeSettings settings,
            final SettingsCreationContext context) throws Exception {

        try (var service = settings.createService(context.getCredentialsProvider().orElseThrow())) {
            return new AuthCodeFlow(service, URI.create(settings.m_redirectUrl))//
                    .login(settings.m_scopes.toScopeString());
        }
    }

    @Override
    public OAuth20Service createService(final CredentialsProvider credsProvider) {
        final DefaultApi20 api;

        if (m_serviceType == ServiceType.CUSTOM) {
            api = m_customService.createApi();
        } else {
            api = m_standardService.getApi();
        }


        final ServiceBuilder builder;
        if (m_appType == AppType.PUBLIC) {
            builder = new ServiceBuilder(m_publicApp.m_appId);
        } else {
            builder = new ServiceBuilder(m_confidentialApp.login(credsProvider))//
                    .apiSecret(m_confidentialApp.secret(credsProvider));
        }

        builder.callback(m_redirectUrl);

        return builder.build(api);
    }

    private static class TokenCacheKeyPersistor extends NodeSettingsPersistorWithConfigKey<UUID> {

        @Override
        public UUID load(final NodeSettingsRO settings) throws InvalidSettingsException {
            if (settings.containsKey(getConfigKey())) {
                var uuidStr = settings.getString(getConfigKey());
                if (!StringUtils.isBlank(uuidStr)) {
                    final var uuid = UUID.fromString(uuidStr);
                    if (CredentialCache.get(uuid).isPresent()) {
                        return uuid;
                    }
                }
            }

            return null;
        }

        @Override
        public void save(final UUID uuid, final NodeSettingsWO settings) {
            if (uuid != null) {
                settings.addString(getConfigKey(), uuid.toString());
            }
        }
    }

    void validate(final CredentialsProvider credentialsProvider) throws InvalidSettingsException {
        if (m_serviceType == ServiceType.CUSTOM) {
            m_customService.validate();
        } else if (m_standardService == null) {
            throw new InvalidSettingsException("No service is selected");
        }

        if (m_appType == AppType.CONFIDENTIAL) {
            m_confidentialApp.validateOnExecute(credentialsProvider);
        } else {
            m_publicApp.validate();
        }

        m_scopes.validate();
    }
}
