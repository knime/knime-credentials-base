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
import org.knime.credentials.base.CredentialType;
import org.knime.credentials.base.oauth.api.JWTCredential;
import org.knime.credentials.base.oauth2.authcode.OAuth2AuthenticatorAuthCodeSettings.ServiceType;
import org.knime.credentials.base.oauth2.base.CredentialFactory;
import org.knime.credentials.base.oauth2.base.OAuth2AuthenticatorSettingsBase.ClientType;

/**
 * Node model of the OAuth2 authenticator (Interactive) node. Performs OAuth
 * authentication using the Auth code or implicit grant and produces credential
 * port object with {@link JWTCredential}.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
@SuppressWarnings("restriction")
public class OAuth2AuthenticatorAuthCodeNodeModel extends WebUINodeModel<OAuth2AuthenticatorAuthCodeSettings> {

    /**
     * @param configuration
     *            The node configuration.
     */
    protected OAuth2AuthenticatorAuthCodeNodeModel(final WebUINodeConfiguration configuration) {
        super(configuration, OAuth2AuthenticatorAuthCodeSettings.class);
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs,
            final OAuth2AuthenticatorAuthCodeSettings modelSettings) throws InvalidSettingsException {
        validate(modelSettings);
        return new PortObjectSpec[] { createSpec(null) };
    }

    private static CredentialPortObjectSpec createSpec(final CredentialType type) {
        return new CredentialPortObjectSpec(type);
    }

    private static void validate(final OAuth2AuthenticatorAuthCodeSettings settings)
            throws InvalidSettingsException {
        if (settings.m_serviceType == ServiceType.CUSTOM) {
            if (StringUtils.isEmpty(settings.m_tokenUrl)) {
                throw new InvalidSettingsException("Token endpoint URL is required");
            }
            if (StringUtils.isEmpty(settings.m_authorizationUrl)) {
                throw new InvalidSettingsException("Authorization endpoing URL is required");
            }
        }

        if (settings.m_serviceType == ServiceType.STANDARD) {
            if (settings.m_standardService == null) {
                throw new InvalidSettingsException("No service is selected");
            }
        }

        if (StringUtils.isEmpty(settings.m_clientId)) {
            throw new InvalidSettingsException("Client/App ID is required");
        }

        if (settings.m_clientType == ClientType.CONFIDENTIAL && StringUtils.isEmpty(settings.m_clientSecret)) {
            throw new InvalidSettingsException("Client/App secret is required");
        }
    }

    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec,
            final OAuth2AuthenticatorAuthCodeSettings modelSettings) throws Exception {
        var credential = fetchCredential(modelSettings);
        var uuid = CredentialCache.store(credential);
        return new PortObject[] { new CredentialPortObject(createSpec(credential.getType()), uuid) };
    }

    private static Credential fetchCredential(final OAuth2AuthenticatorAuthCodeSettings settings)
            throws Exception {

        var scribeJavaToken = settings.fetchAccessToken();
        return CredentialFactory.fromScribeToken(scribeJavaToken, () -> settings.createService());
    }
}
