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
package org.knime.credentials.base.oauth.node.generic;

import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeModel;
import org.knime.credentials.base.Credential;
import org.knime.credentials.base.CredentialCache;
import org.knime.credentials.base.CredentialPortObject;
import org.knime.credentials.base.CredentialPortObjectSpec;
import org.knime.credentials.base.oauth.api.GenericJWTCredential;
import org.knime.credentials.base.oauth.api.JWTCredential;
import org.knime.credentials.base.oauth.node.generic.GenericOAuthAuthenticatorSettings.ClientType;
import org.knime.credentials.base.oauth.node.generic.GenericOAuthAuthenticatorSettings.GrantType;
import org.knime.credentials.base.oauth.node.generic.GenericOAuthAuthenticatorSettings.HttpRequestMethod;
import org.knime.credentials.base.oauth.node.generic.GenericOAuthAuthenticatorSettings.ServiceType;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * Generic OAuth authentication node. Performs OAuth authentication using
 * scribejava library and produces credential port object with
 * {@link JWTCredential}.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
@SuppressWarnings("restriction")
public class GenericOAuthAuthenticatorNodeModel extends WebUINodeModel<GenericOAuthAuthenticatorSettings> {

    /**
     * @param configuration
     *            The node configuration.
     */
    protected GenericOAuthAuthenticatorNodeModel(final WebUINodeConfiguration configuration) {
        super(configuration, GenericOAuthAuthenticatorSettings.class);
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs,
            final GenericOAuthAuthenticatorSettings modelSettings) throws InvalidSettingsException {
        validate(modelSettings);
        return new PortObjectSpec[] { createSpec() };
    }

    private static CredentialPortObjectSpec createSpec() {
        return new CredentialPortObjectSpec(JWTCredential.TYPE);
    }

    private static void validate(final GenericOAuthAuthenticatorSettings settings) throws InvalidSettingsException {
        if (settings.m_serviceType == ServiceType.CUSTOM) {
            if (StringUtils.isEmpty(settings.m_tokenUrl)) {
                throw new InvalidSettingsException("Token endpoint URL is required");
            }
            if (settings.m_grantType != GrantType.CLIENT_CREDENTIALS
                    && StringUtils.isEmpty(settings.m_authorizationUrl)) {
                throw new InvalidSettingsException("Authorization endpoing URL is required");
            }
        }

        if (settings.m_serviceType == ServiceType.STANDARD) {
            if (settings.m_standardService == null) {
                throw new InvalidSettingsException("No service is selected");
            }

            if (!settings.m_standardService.isApi20() && settings.m_grantType != GrantType.AUTH_CODE) {
                throw new InvalidSettingsException("Only Auth code grant type is supported by OAuth 1.0");
            }
        }

        if (StringUtils.isEmpty(settings.m_clientId)) {
            throw new InvalidSettingsException("Client/App ID is required");
        }

        if (settings.m_clientType == ClientType.CONFIDENTIAL && StringUtils.isEmpty(settings.m_clientSecret)) {
            throw new InvalidSettingsException("Client/App secret is required");
        }

        if (settings.m_grantType == GrantType.PASSWORD) {
            if (StringUtils.isEmpty(settings.m_pwdGrantUsername)) {
                throw new InvalidSettingsException("Username is required");
            }

            if (StringUtils.isEmpty(settings.m_pwdGrantPassword)) {
                throw new InvalidSettingsException("Password is required");
            }
        }

        if (settings.m_grantType != GrantType.CLIENT_CREDENTIALS && settings.m_grantType != GrantType.PASSWORD) {
            throw new InvalidSettingsException("Grant type is not implemented yet");
        }
    }

    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec,
            final GenericOAuthAuthenticatorSettings modelSettings) throws Exception {
        var credential = fetchCredential(modelSettings);
        var uuid = CredentialCache.store(credential);
        return new PortObject[] { new CredentialPortObject(createSpec(), uuid) };
    }

    private static Credential fetchCredential(final GenericOAuthAuthenticatorSettings settings)
            throws IOException, InterruptedException, ExecutionException, ParseException {

        var builder = new ServiceBuilder(settings.m_clientId);

        if (settings.m_clientType == ClientType.CONFIDENTIAL) {
            builder.apiSecret(settings.m_clientSecret);
        }

        if (settings.m_serviceType == ServiceType.CUSTOM || settings.m_standardService.isApi20()) {
            return fetchCredentialOAuth20(settings, builder);
        } else {
            return fetchCredentialOAuth1(settings, builder);
        }


    }

    private static Credential fetchCredentialOAuth1(final GenericOAuthAuthenticatorSettings settings,
            final ServiceBuilder builder) throws IOException {

        final var api = settings.m_standardService.getApi10();
        try (var service = builder.build(api)) {
            // TODO
        }
        return null;
    }

    private static Credential fetchCredentialOAuth20(final GenericOAuthAuthenticatorSettings settings,
            final ServiceBuilder builder) throws IOException, InterruptedException, ExecutionException, ParseException {

        final DefaultApi20 api;

        if (settings.m_serviceType == ServiceType.CUSTOM) {
            api = new CustomApi20(settings.m_tokenUrl, //
                    settings.m_authorizationUrl, //
                    toScribeVerb(settings.m_customRequestMethod), //
                    settings.m_clientAuthMechanism);
        } else {
            api = settings.m_standardService.getApi20();
        }

        try (var service = builder.build(api)) {
            var token = fetchAccessToken(service, settings);
            return new GenericJWTCredential(token.getAccessToken(), null, token.getRefreshToken());
        }
    }

    private static OAuth2AccessToken fetchAccessToken(final OAuth20Service service,
            final GenericOAuthAuthenticatorSettings settings)
            throws IOException, InterruptedException, ExecutionException {
        switch (settings.m_grantType) {
        case CLIENT_CREDENTIALS:
            return service.getAccessTokenClientCredentialsGrant(settings.m_scopes);
        case PASSWORD:
            return service.getAccessTokenPasswordGrant(settings.m_pwdGrantUsername, settings.m_pwdGrantPassword, settings.m_scopes);
        default:
            throw new IllegalArgumentException("Usupported grant type: " + settings.m_grantType);
        }
    }

    private static Verb toScribeVerb(final HttpRequestMethod method) {
        return Verb.valueOf(method.toString());
    }
}
