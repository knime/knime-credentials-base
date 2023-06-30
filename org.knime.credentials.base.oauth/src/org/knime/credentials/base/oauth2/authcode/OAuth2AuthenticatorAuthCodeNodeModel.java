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

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.credentials.base.CredentialCache;
import org.knime.credentials.base.oauth.api.JWTCredential;
import org.knime.credentials.base.oauth2.base.OAuth2AuthenticatorNodeModel;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * Node model of the (interactive) OAuth2 authenticator node. Performs OAuth
 * authentication using the Auth code or implicit grant and produces credential
 * port object with {@link JWTCredential}.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
@SuppressWarnings("restriction")
final class OAuth2AuthenticatorAuthCodeNodeModel
        extends OAuth2AuthenticatorNodeModel<OAuth2AuthenticatorAuthCodeSettings> {

    private static final String LOGIN_FIRST_ERROR = "Please use the configuration dialog to log in first.";

    private OAuth2AccessTokenHolder m_tokenHolder;

    /**
     * @param configuration
     *            The node configuration.
     */
    protected OAuth2AuthenticatorAuthCodeNodeModel(final WebUINodeConfiguration configuration) {
        super(configuration, OAuth2AuthenticatorAuthCodeSettings.class);
    }

    @Override
    protected void validateOnConfigure(final PortObjectSpec[] inSpecs,
            final OAuth2AuthenticatorAuthCodeSettings settings) throws InvalidSettingsException {

        settings.validate(getCredentialsProvider());

        if (settings.m_tokenCacheKey == null) {
            throw new InvalidSettingsException(LOGIN_FIRST_ERROR);
        } else {
            m_tokenHolder = CredentialCache.<OAuth2AccessTokenHolder>get(settings.m_tokenCacheKey)//
                    .orElseThrow(() -> new InvalidSettingsException(LOGIN_FIRST_ERROR));
        }
    }

    @Override
    protected OAuth2AccessToken fetchOAuth2AccessToken(final OAuth2AuthenticatorAuthCodeSettings settings,
            final OAuth20Service service) throws Exception {
        return m_tokenHolder.m_token;
    }

    @Override
    protected void onDisposeInternal() {
        // dispose of the scribejava token that was retrieved interactively in the node
        // dialog
        if (m_tokenHolder != null) {
            CredentialCache.delete(m_tokenHolder.m_cacheKey);
            m_tokenHolder = null;
        }
    }
}
