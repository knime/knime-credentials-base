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

import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeFactory;
import org.knime.credentials.base.CredentialPortObject;

/**
 * The node factory for the {@link OAuth2AuthenticatorPasswordNodeModel} node.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
@SuppressWarnings("restriction")
public class OAuth2AuthenticatorPasswordNodeFactory extends WebUINodeFactory<OAuth2AuthenticatorPasswordNodeModel> {

    private static final String FULL_DESCRIPTION = """
                    <p>OAuth2 Authenticator that supports the <a href="https://oauth.net/2/grant-types/password/">resource
                    owner password credentials (ROPC)</a> grant flow.
                    </p>
                    <p>The ROPC grant is considered legacy and does not support 2FA/MFA. Usage of this grant is
                    discouraged and the client credentials grant should be used instead.</p>
            """;

    private static final WebUINodeConfiguration CONFIGURATION = WebUINodeConfiguration.builder()//
            .name("OAuth2 Authenticator (Password)")//
            .icon("../base/oauth.png")//
            .shortDescription("OAuth2 Authenticator that supports the resource owner password credentials (ROPC) grant.")//
            .fullDescription(FULL_DESCRIPTION)
            .modelSettingsClass(OAuth2AuthenticatorPasswordSettings.class)//
            .addOutputPort("Credential", CredentialPortObject.TYPE, "Credential with access token.")//
            .sinceVersion(5, 1, 0)//
            .build();

    /**
     * Creates new instance.
     */
    public OAuth2AuthenticatorPasswordNodeFactory() {
        super(CONFIGURATION);
    }

    @Override
    public OAuth2AuthenticatorPasswordNodeModel createNodeModel() {
        return new OAuth2AuthenticatorPasswordNodeModel(CONFIGURATION);
    }
}
