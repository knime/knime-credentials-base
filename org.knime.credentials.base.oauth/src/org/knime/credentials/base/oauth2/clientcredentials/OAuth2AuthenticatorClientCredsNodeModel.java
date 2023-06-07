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
package org.knime.credentials.base.oauth2.clientcredentials;

import static org.knime.credentials.base.oauth2.base.OAuth2AuthenticatorSettingsBase.toScribeVerb;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
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
import org.knime.credentials.base.oauth2.base.CustomApi20;
import org.knime.credentials.base.oauth2.base.CustomOAuth2ServiceBuilder;

/**
 * Node model of the OAuth2 authenticator (Client Credentials) node. Performs
 * OAuth authentication using the client credentials grant and produces
 * credential port object with {@link JWTCredential}.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
@SuppressWarnings("restriction")
public class OAuth2AuthenticatorClientCredsNodeModel extends WebUINodeModel<OAuth2AuthenticatorClientCredsSettings> {

    /**
     * @param configuration
     *            The node configuration.
     */
    protected OAuth2AuthenticatorClientCredsNodeModel(final WebUINodeConfiguration configuration) {
        super(configuration, OAuth2AuthenticatorClientCredsSettings.class);
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs,
            final OAuth2AuthenticatorClientCredsSettings modelSettings) throws InvalidSettingsException {
        validate(modelSettings);
        return new PortObjectSpec[] { createSpec() };
    }

    private static CredentialPortObjectSpec createSpec() {
        return new CredentialPortObjectSpec(JWTCredential.TYPE);
    }

    private static void validate(final OAuth2AuthenticatorClientCredsSettings settings)
            throws InvalidSettingsException {
        if (StringUtils.isEmpty(settings.m_tokenUrl)) {
            throw new InvalidSettingsException("Token endpoint URL is required");
        }

        if (StringUtils.isEmpty(settings.m_clientId)) {
            throw new InvalidSettingsException("Client/App ID is required");
        }

        if (StringUtils.isEmpty(settings.m_clientSecret)) {
            throw new InvalidSettingsException("Client/App secret is required");
        }
    }

    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec,
            final OAuth2AuthenticatorClientCredsSettings modelSettings) throws Exception {

        var credential = fetchCredential(modelSettings);
        var uuid = CredentialCache.store(credential);
        return new PortObject[] { new CredentialPortObject(createSpec(), uuid) };
    }

    private static Credential fetchCredential(final OAuth2AuthenticatorClientCredsSettings settings)
            throws IOException, InterruptedException, ExecutionException, ParseException {

        final var api = new CustomApi20(settings.m_tokenUrl, //
                "", //
                toScribeVerb(settings.m_tokenRequestMethod), //
                settings.m_clientAuthMechanism);

        var builder = new CustomOAuth2ServiceBuilder(settings.m_clientId);
        builder.apiSecret(settings.m_clientSecret);
        Arrays.stream(settings.m_additionalRequestFields)//
                .forEach(field -> builder.additionalRequestBodyField(field.m_name, field.m_value));

        try (var service = builder.build(api)) {
            var scribeJavaToken = service.getAccessTokenClientCredentialsGrant(settings.m_scopes);
            return new GenericJWTCredential(scribeJavaToken.getAccessToken(), null, scribeJavaToken.getRefreshToken());
        }
    }
}
