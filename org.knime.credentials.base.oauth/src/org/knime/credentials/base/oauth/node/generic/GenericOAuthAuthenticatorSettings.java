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

import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;

/**
 * The node settings for the Generic OAuth Authenticator node.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
@SuppressWarnings("restriction")
public class GenericOAuthAuthenticatorSettings implements DefaultNodeSettings {

    @Widget(title = "Service type", description = "Defines whether the user selects service from the list or enters custom URLs manually.")
    ServiceType m_serviceType = ServiceType.STANDARD;

    @Widget(title = "Supported standard service", description = "The service to perform authentication to.")
    @ChoicesWidget(choices = AllStandardServices.class)
    String m_standardService;

    @Widget(title = "Authorization endpoint URL", description = "The authorization endpoint URL of the OAuth service.")
    String m_authorizationUrl;

    @Widget(title = "Token endpoint URL", description = "The token endpoint URL of the OAuth service.")
    String m_tokenUrl;

    @Widget(title = "Client type", description = "Whether a public or confidential application flow should be used. A confidential application requires a secret.")
    ClientType m_clientType = ClientType.PUBLIC;

    @Widget(title = "Client/App ID", description = "The Client/Application ID. In some services this is called API key.")
    String m_clientId;

    @Widget(title = "Client/App secret", description = "The secret for the confidential application.")
    String m_clientSecret;

    @Widget(title = "Grant type", description = "Desired OAuth Grant type")
    GrantType m_grantType = GrantType.CLIENT_CREDENTIALS;

    @Widget(title = "Scopes", description = "The list of scopes separated by the whitespace or new line.")
    String m_scopes;

    private static final class AllStandardServices implements ChoicesProvider {

        @Override
        public String[] choices(final SettingsCreationContext context) {
            return new String[] {};
        }

    }

    enum ServiceType {
        STANDARD, CUSTOM;
    }

    enum ClientType {
        PUBLIC, CONFIDENTIAL;
    }

    enum GrantType {
        AUTH_CODE, CLIENT_CREDENTIALS, PASSWORD, IMPLICIT;
    }

}
