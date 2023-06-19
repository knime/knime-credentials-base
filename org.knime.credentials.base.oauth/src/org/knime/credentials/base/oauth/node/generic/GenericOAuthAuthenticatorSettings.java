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

import org.knime.core.node.workflow.VariableType.CredentialsType;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.Persist;
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

    @Widget(title = "Service type", description = "Whether to connect to a standard OAuth service from a predefined list, or to manually specify service URLs.")
    ServiceType m_serviceType = ServiceType.STANDARD;

    @Widget(title = "Service", description = "A standard OAuth service from a predefined list.")
    StandardService m_standardService;

    @Widget(title = "Authorization endpoint URL", description = "The authorization endpoint URL of the OAuth service.")
    String m_authorizationUrl;

    @Widget(title = "Token endpoint URL", description = "The token endpoint URL of the OAuth service.")
    String m_tokenUrl;

    @Widget(title = "Token endpoint request method", description = "HTTP method to use when requesting the access token from the token endpoint.")
    HttpRequestMethod m_customRequestMethod = HttpRequestMethod.POST;

    @Widget(title = "Client/App type", description = "Whether a public or confidential application flow should be used. A confidential application requires a secret.")
    ClientType m_clientType = ClientType.PUBLIC;

    @Widget(title = "ID", description = "The Client/Application ID. In some services this is called API key.")
    String m_clientId;

    @Widget(title = "Secret", description = "The secret for the confidential application.")
    String m_clientSecret;

    @Widget(title = "Authentication mechanism", description = "How to transfer Client/App ID and secret to the service endpoints. HTTP Basic Auth is the most common mechanism, "
            + "but some services expect these values to be part of the form-encoded request body.")
    ClientAuthenticationType m_clientAuthMechanism = ClientAuthenticationType.HTTP_BASIC_AUTH;

    @Widget(title = "Grant type", description = "Desired OAuth Grant type")
    GrantType m_grantType = GrantType.CLIENT_CREDENTIALS;

    @Widget(title = "Username", description = "The username to use")
    String m_pwdGrantUsername;

    @Widget(title = "Password", description = "The password to use")
    String m_pwdGrantPassword;

    @Widget(title = "Scopes", description = "The list of scopes separated by the whitespace or new line.")
    String m_scopes;

    @Widget(title = "Credentials", description = "")
    @ChoicesWidget(choices = CredentialsProvider.class, showNoneColumn = true)
    @Persist(optional = true)
    public String m_credentialsColumn = "";

    enum ServiceType {
        STANDARD, CUSTOM;
    }

    enum HttpRequestMethod {
        POST, GET;
    }

    enum ClientAuthenticationType {
        HTTP_BASIC_AUTH, REQUEST_BODY
    }

    enum ClientType {
        PUBLIC, CONFIDENTIAL;
    }

    enum GrantType {
        AUTH_CODE, CLIENT_CREDENTIALS, PASSWORD, IMPLICIT;
    }

    /**
     * A {@link ChoicesProvider} yielding those columns in the given input table
     * spec which have a color handler appended.
     *
     * @author Daniel Bogenrieder
     */
    public static final class CredentialsProvider implements ChoicesProvider {

        /**
         * {@inheritDoc}
         */
        @Override
        public String[] choices(final SettingsCreationContext context) {
            return context.getAvailableInputFlowVariables(CredentialsType.INSTANCE).keySet().toArray(String[]::new);
        }

    }
}
