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

import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.knime.credentials.base.CredentialPortObject;

/**
 * Node factory for the {@link OAuth2AuthenticatorAuthCodeNodeModel} node.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
@SuppressWarnings("restriction")
public class OAuth2AuthenticatorAuthCodeNodeFactory
        extends WebUINodeFactory<OAuth2AuthenticatorAuthCodeNodeModel> {

    private static final String FULL_DESCRIPTION = """
                    <p>This node supports the (interactive)
                    <a href="https://oauth.net/2/grant-types/authorization-code/">OAuth 2.0 Authorization Code</a> grant flow.
                    The target audience of this node are users with a technical understanding of OAuth 2 and (web) developers.
                    For less technical users, it may be simpler to use service-specific nodes, such as Microsoft Authenticator
                    or Google Authenticator.
                    </p>
                    <p>The auth code flow is used to obtain an access token via an interactive login, which works as follows:
                    <ul>
                        <li><b>In the node settings:</b> the user specifies all required information and then
                             clicks on "Login", which will open a new browser window.</li>
                        <li><b>In the new browser window:</b>The user logs into the authentication service, consenting
                            to any required permissions (scopes). At the end of this process the authentication service
                            redirects the browser to the configured redirect URL, passing an <i>authorization code</i>.</li>
                        <li><b>In the node settings:</b> The authorization code is received (via the redirect) and is used to acquire
                            an access token. The user can now close the node settings (OK).</li>
                        <li>The node can now be executed.</li>
                        <li>Whenever the user closes the workflow, the access token is deleted. Opening the workflow again will
                        require a fresh interactive login as above.</li>
                    </ul>
                    </p>
                    <p>
                    <b>Note:</b> Currently, the node can only be used in KNIME Analytics Platform. The node does not support
                    execution on KNIME (Business) Hub or KNIME Server, also not via Remote Workflow Editor.
                    </p>
            """;
    private static final WebUINodeConfiguration CONFIGURATION = WebUINodeConfiguration.builder()//
            .name("OAuth2 Authenticator")//
            .icon("../base/oauth.png")//
            .shortDescription("Authenticator node that supports the Authorization Code grant flow.")//
            .fullDescription(FULL_DESCRIPTION) //
            .modelSettingsClass(OAuth2AuthenticatorAuthCodeSettings.class)//
            .addOutputPort("Credential", CredentialPortObject.TYPE,
                    "Credential with access token.")//
            .sinceVersion(5, 1, 0)//
            .build();

    /**
     * Creates new instance.
     */
    public OAuth2AuthenticatorAuthCodeNodeFactory() {
        super(CONFIGURATION);
    }

    @Override
    public OAuth2AuthenticatorAuthCodeNodeModel createNodeModel() {
        return new OAuth2AuthenticatorAuthCodeNodeModel(CONFIGURATION);
    }
}
