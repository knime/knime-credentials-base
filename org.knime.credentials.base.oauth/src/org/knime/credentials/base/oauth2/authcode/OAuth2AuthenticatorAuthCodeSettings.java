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

import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.credentials.base.oauth2.base.CustomApi20;
import org.knime.credentials.base.oauth2.base.OAuth2AuthenticatorSettingsBase;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.oauth.AccessTokenRequestParams;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * OAuth2 Authenticator (Interactive) node settings.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
@SuppressWarnings("restriction")
public class OAuth2AuthenticatorAuthCodeSettings extends OAuth2AuthenticatorSettingsBase {

    @Widget(title = "Service type", description = """
            Whether to connect to a standard OAuth service from a predefined list, or
            to manually specify endpoint URLs.""")
    ServiceType m_serviceType = ServiceType.STANDARD;

    @Widget(title = "Service", description = "A standard OAuth service from a predefined list.")
    StandardService m_standardService;

    @Widget(title = "Authorization endpoint URL", description = "The authorization endpoint URL of the OAuth service.")
    String m_authorizationUrl;

    @Widget(title = "Client/App type", description = CLIENT_TYPE_DESCRIPTION)
    ClientType m_clientType = ClientType.PUBLIC;

    @Widget(title = "Secret", description = CLIENT_SECRET_DESCRIPTION)
    String m_clientSecret;

    @Widget(title = "Redirect URL (should be http://localhost:XXXXX)", description = """
            The redirect URL to be used at the end of the interactive login. Should be chosen as http://localhost:XXXXX
            with a random number in the 10000 - 65000 range to avoid conflicts. Often, the redirect URL is part of the
            client/app registration at the OAuth2 service.
            """)
    String m_redirectUrl = "http://localhost:43769";

    enum ServiceType {
        STANDARD, CUSTOM;
    }

    enum GrantType {
        AUTH_CODE, IMPLICIT;
    }

    /**
     * Performs interactive login. The method will be called by the login button in
     * the dialog.
     *
     * @param settings
     *            The node settings.
     * @return The {@link CompletableFuture} for auth code.
     * @throws IOException
     */
    OAuth2AccessToken fetchAccessToken() throws Exception {

        try (var service = createService()) {
            var state = UUID.randomUUID().toString().replace("-", "");
            var authorizationUrl = service.createAuthorizationUrlBuilder()//
                    .scope(m_scopes)//
                    .state(state)//
                    .build();
            var authCode = new InteractiveLogin(state)//
                    .login(URI.create(authorizationUrl), URI.create(m_redirectUrl));

            try {
                return service.getAccessToken(AccessTokenRequestParams.create(authCode).scope(m_scopes));
            } catch (OAuth2AccessTokenErrorResponse e) {
                throw new Exception(String.format("Login failed (%s): %s", //
                        e.getError().getErrorString(), //
                        e.getErrorDescription()), e);
            }
        }
    }

    /**
     * Creates {@link OAuth20Service} from the current settings.
     *
     * @return The {@link OAuth20Service} instance.
     */
    public OAuth20Service createService() {
        var builder = new ServiceBuilder(m_clientId);

        builder.callback(m_redirectUrl);

        if (m_clientType == ClientType.CONFIDENTIAL) {
            builder.apiSecret(m_clientSecret);
        }

        final DefaultApi20 api;

        if (m_serviceType == ServiceType.CUSTOM) {
            api = new CustomApi20(m_tokenUrl, //
                    m_authorizationUrl, //
                    toScribeVerb(m_tokenRequestMethod), //
                    m_clientAuthMechanism);
        } else {
            api = m_standardService.getApi();
        }

        return builder.build(api);
    }
}
