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

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.credentials.base.oauth.api.scribejava.PasswordFlow;
import org.knime.credentials.base.oauth2.base.OAuth2AuthenticatorNodeModel;
import org.knime.credentials.base.oauth2.base.OAuth2AuthenticatorSettings.AppType;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * Node model for the OAuth2 Authenticator (Password) node. Performs OAuth
 * authentication using the ROPC grant.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
@SuppressWarnings("restriction")
class OAuth2AuthenticatorPasswordNodeModel
        extends OAuth2AuthenticatorNodeModel<OAuth2AuthenticatorPasswordSettings> {

    /**
     * @param configuration
     *            The node configuration.
     */
    protected OAuth2AuthenticatorPasswordNodeModel(final WebUINodeConfiguration configuration) {
        super(configuration, OAuth2AuthenticatorPasswordSettings.class);
    }

    @Override
    protected void validateOnConfigure(final PortObjectSpec[] inSpecs,
            final OAuth2AuthenticatorPasswordSettings settings) throws InvalidSettingsException {

        settings.m_service.validate();

        if (settings.m_appType == AppType.CONFIDENTIAL) {
            settings.m_confidentialApp.validateOnConfigure(getCredentialsProvider());
        } else {
            settings.m_publicApp.validate();
        }

        settings.m_usernamePassword.validateOnConfigure(getCredentialsProvider());
        settings.m_scopes.validate();
    }

    @Override
    protected void validateOnExecute(final PortObject[] inObjects, final OAuth2AuthenticatorPasswordSettings settings)
            throws InvalidSettingsException {

        // additional validation steps to ensure that credentials flow variables are
        // present (this was not done during configure())
        if (settings.m_appType == AppType.CONFIDENTIAL) {
            settings.m_confidentialApp.validateOnExecute(getCredentialsProvider());
        }
        settings.m_usernamePassword.validateOnExecute(getCredentialsProvider());
    }

    @Override
    protected OAuth2AccessToken fetchOAuth2AccessToken(final OAuth2AuthenticatorPasswordSettings settings,
            final OAuth20Service service) throws Exception {

        return new PasswordFlow(service, //
                settings.m_usernamePassword.login(getCredentialsProvider()), //
                settings.m_usernamePassword.secret(getCredentialsProvider()))//
                        .login(settings.m_scopes.toScopeString());
    }
}
