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
 *   2023-04-13 (Alexander Bondaletov, Redfield SE): created
 */
package org.knime.credentials.base.oauth2.password;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.webui.node.dialog.configmapping.ConfigsDeprecation;
import org.knime.core.webui.node.dialog.defaultdialog.layout.After;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Before;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Section;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.DefaultPersistorWithDeprecations;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.setting.credentials.Credentials;
import org.knime.core.webui.node.dialog.defaultdialog.setting.credentials.LegacyCredentials;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ValueSwitchWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.credentials.CredentialsWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect.EffectType;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.ValueReference;
import org.knime.credentials.base.oauth2.base.OAuth2AuthenticatorSettings;
import org.knime.credentials.base.oauth2.base.PublicAppSettings;
import org.knime.credentials.base.oauth2.base.ScopeSettings;
import org.knime.credentials.base.oauth2.base.Sections.AppSection;
import org.knime.credentials.base.oauth2.base.Sections.ScopesSection;
import org.knime.credentials.base.oauth2.base.TokenEndpointSettings;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * Node settings for the OAuth2 Authenticator (Password) node.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
@SuppressWarnings("restriction")
final class OAuth2AuthenticatorPasswordSettings implements OAuth2AuthenticatorSettings {

    @Section(title = "Username and Password")
    @After(AppSection.class)
    @Before(ScopesSection.class)
    interface UsernamePasswordSection {
    }

    TokenEndpointSettings m_service = new TokenEndpointSettings();

    @Widget(title = "Type", description = CLIENT_TYPE_DESCRIPTION)
    @ValueSwitchWidget
    @Layout(AppSection.TypeChooser.class)
    @ValueReference(AppTypeRef.class)
    AppType m_appType = AppType.PUBLIC;

    PublicAppSettings m_publicApp = new PublicAppSettings();

    @Effect(predicate = IsPublicApp.class, type = EffectType.HIDE)
    @Layout(AppSection.Confidential.class)
    @Persist(customPersistor = ConfidentialLegacyCredentialsPersistor.class)
    @Widget(title = "ID and Secret", //
            description = "The client/app ID and secret to use.")
    @CredentialsWidget(usernameLabel = "ID", passwordLabel = "Secret") // NOSONAR: no PASSWORD here
    LegacyCredentials m_confidentialApp = new LegacyCredentials(new Credentials());

    static final class ConfidentialLegacyCredentialsPersistor
            implements DefaultPersistorWithDeprecations<LegacyCredentials> {
        private static final String LEGACY_KEY = "confidentialApp";
        private static final String LEGACY_SUB_KEY = "flowVariable";

        static LegacyCredentials loadFromLegacy(final NodeSettingsRO settings) throws InvalidSettingsException {
            final var flowVariableName = settings.getNodeSettings(LEGACY_KEY).getString(LEGACY_SUB_KEY);
            if (flowVariableName == null) {
                return new LegacyCredentials(new Credentials());
            }
            return new LegacyCredentials(flowVariableName);
        }

        @Override
        public List<ConfigsDeprecation<LegacyCredentials>> getConfigsDeprecations() {
            return List.of(ConfigsDeprecation.builder(ConfidentialLegacyCredentialsPersistor::loadFromLegacy)
                    .withDeprecatedConfigPath(LEGACY_KEY, LEGACY_SUB_KEY).build());
        }
    }

    @Layout(UsernamePasswordSection.class)
    @Persist(customPersistor = LegacyCredentialsPersistor.class)
    @Widget(title = "Username/Password", //
            description = "The username and password to use.")
    LegacyCredentials m_usernamePasswordV2 = new LegacyCredentials(new Credentials());

    static final class LegacyCredentialsPersistor implements DefaultPersistorWithDeprecations<LegacyCredentials> {

        private static final String LEGACY_KEY = "usernamePassword";
        private static final String LEGACY_SUB_KEY = "flowVariable";

        static LegacyCredentials loadFromLegacy(final NodeSettingsRO settings) throws InvalidSettingsException {
            final var flowVariableName = settings.getNodeSettings(LEGACY_KEY).getString(LEGACY_SUB_KEY);
            if (flowVariableName == null) {
                return new LegacyCredentials(new Credentials());
            }
            return new LegacyCredentials(flowVariableName);
        }

        @Override
        public List<ConfigsDeprecation<LegacyCredentials>> getConfigsDeprecations() {
            return List.of(ConfigsDeprecation.builder(LegacyCredentialsPersistor::loadFromLegacy)
                    .withDeprecatedConfigPath(LEGACY_KEY, LEGACY_SUB_KEY).build());
        }

    }

    void validateClientIdAndSecret(final CredentialsProvider credentialsProvider) throws InvalidSettingsException {
        CheckUtils.checkSetting(
                StringUtils.isNotEmpty(m_confidentialApp.toCredentials(credentialsProvider).getUsername()),
                "Client/App ID is required");
        CheckUtils.checkSetting(
                StringUtils.isNotEmpty(m_confidentialApp.toCredentials(credentialsProvider).getPassword()),
                "Client/App secret is required");
    }

    void validateUsernameAndPassword(final CredentialsProvider credentialsProvider) throws InvalidSettingsException {
        CheckUtils.checkSetting(
                StringUtils.isNotEmpty(m_usernamePasswordV2.toCredentials(credentialsProvider).getUsername()),
                "Username is required");
        CheckUtils.checkSetting(
                StringUtils.isNotEmpty(m_usernamePasswordV2.toCredentials(credentialsProvider).getPassword()),
                "Password is required");
    }

    ScopeSettings m_scopes = new ScopeSettings();

    @Override
    public OAuth20Service createService(final CredentialsProvider credsProvider) {
        final var api = m_service.createApi();

        if (m_appType == AppType.PUBLIC) {
            return new ServiceBuilder(m_publicApp.m_appId).build(api);
        } else {
            return new ServiceBuilder(m_confidentialApp.toCredentials(credsProvider).getUsername()) //
                    .apiSecret(m_confidentialApp.toCredentials(credsProvider).getPassword())//
                    .build(api);
        }
    }
}
