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
 *   2023-06-05 (bjoern): created
 */
package org.knime.credentials.base.oauth2.base;

import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Label;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;

import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
import com.github.scribejava.core.oauth2.clientauthentication.HttpBasicAuthenticationScheme;
import com.github.scribejava.core.oauth2.clientauthentication.RequestBodyAuthenticationScheme;

/**
 * Base class for OAuth2 Authenticator settings. Defines the common settings and
 * enums of all OAuth2 Authenticator settings classes.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
@SuppressWarnings({ "restriction", "javadoc" })
public class OAuth2AuthenticatorSettingsBase implements DefaultNodeSettings {

    public static final String CLIENT_TYPE_DESCRIPTION = """
            Whether a public or confidential application flow should be used. A confidential application requires a secret.
            """;

    public static final String CLIENT_SECRET_DESCRIPTION = """
            The secret for the confidential application.
            """;

    @Widget(title = "Token endpoint URL", description = "The token endpoint URL of the OAuth2 service.")
    public String m_tokenUrl;

    @Widget(title = "Token endpoint request method", //
            description = "HTTP method to use when requesting the access token from the token endpoint.", //
            advanced = true)
    public HttpRequestMethod m_tokenRequestMethod = HttpRequestMethod.POST;

    @Widget(title = "ID", description = "The client/application ID. In some services this is called API key.")
    public String m_clientId;

    @Widget(title = "Authentication mechanism", //
            description = """
                    How to transfer Client/App ID and secret to the service endpoints. HTTP Basic Auth is the most common mechanism,
                    but some services expect these values to be part of the form-encoded request body.
                        """, //
            advanced = true)
    public ClientAuthenticationType m_clientAuthMechanism = ClientAuthenticationType.HTTP_BASIC_AUTH;

    @Widget(title = "Scopes", description = "The list of scopes separated by the whitespace or new line.")
    public String m_scopes;

    public enum HttpRequestMethod {
        @Label("POST")
        POST,

        @Label("GET")
        GET;
    }

    public enum ClientAuthenticationType {
        @Label("HTTP Basic Auth")
        HTTP_BASIC_AUTH,

        @Label("Request Body")
        REQUEST_BODY
    }

    public enum ClientType {
        PUBLIC, CONFIDENTIAL;
    }

    public static Verb toScribeVerb(final HttpRequestMethod method) {
        return Verb.valueOf(method.toString());
    }

    public static ClientAuthentication toScribeClientAuthentication(final ClientAuthenticationType authType) {
        if (authType == ClientAuthenticationType.HTTP_BASIC_AUTH) {
            return HttpBasicAuthenticationScheme.instance();
        } else {
            return RequestBodyAuthenticationScheme.instance();
        }
    }
}
