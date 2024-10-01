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
 *   2023-06-06 (Alexander Bondaletov, Redfield SE): created
 */
package org.knime.credentials.base.oauth.api.scribejava;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.util.DesktopUtil;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.oauth.AccessTokenRequestParams;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.pkce.PKCEService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Performs an interactive login using the OAuth2 authorization code flow. It
 * opens a browser window and sets up a temporary local webserver to listen for
 * the redirect by the OAuth provider.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
public class AuthCodeFlow extends FlowBase {

    private final URI m_redirectUri;
    private String m_state;
    private CompletableFuture<String> m_authCodeFuture;
    private HttpServer m_server;
    private final boolean m_usePKCE;

    /**
     * Creates a new instance.
     *
     * @param service
     *            The {@link OAuth20Service} instance to use.
     * @param redirectUri
     *            The redirect URL.
     * @param usePKCE
     *            whether to use PKCE to secure the interactive login.
     */
    public AuthCodeFlow(final OAuth20Service service, final URI redirectUri, final boolean usePKCE) {
        super(service);
        m_redirectUri = redirectUri;
        m_usePKCE = usePKCE;
    }

    /**
     * Performs interactive login. Opens browser window and setups listener for the
     * callback URL. After authorization code is received it is stored into the
     * cache and listener is disposed.
     *
     * @param scopes
     *            The scopes to request for the access token. May be null.
     * @return the {@link OAuth2AccessToken} if the login was successful.
     * @throws Exception
     *             if the login failed for some reason.
     */
    @SuppressWarnings("resource")
    @Override
    public OAuth2AccessToken login(final String scopes) throws Exception {
        // state parameter that associates authorization request with redirect response
        final var pkce = PKCEService.defaultInstance().generatePKCE();
        m_state = UUID.randomUUID().toString().replace("-", "");

        var urlBuilder = getService().createAuthorizationUrlBuilder()//
                .state(m_state);

        if (m_usePKCE) {
            urlBuilder = urlBuilder.pkce(pkce);
        }

        if (!StringUtils.isBlank(scopes)) {
            urlBuilder = urlBuilder.scope(scopes);
        }

        m_authCodeFuture = new CompletableFuture<>();

        setupListener(m_redirectUri);

        final String authCode;
        try {
            DesktopUtil.browse(new URL(urlBuilder.build()));
            authCode = m_authCodeFuture.get(1, TimeUnit.MINUTES);
        } catch (ExecutionException e) { // NOSONAR this is just a wrapper
            throw (Exception) e.getCause();
        } catch (TimeoutException e) { // NOSONAR
            throw new Exception("Login timed out"); // NOSONAR
        } catch (InterruptedException | CancellationException e) {// NOSONAR
            throw new Exception("Login was interrupted/canceled");// NOSONAR
        } finally {
            stopListener();
        }

        try {
            return getService().getAccessToken(
                    AccessTokenRequestParams.create(authCode) //
                            .pkceCodeVerifier(pkce.getCodeVerifier()) //
                            .scope(scopes));
        } catch (OAuth2AccessTokenErrorResponse e) {
            throw wrapAccessTokenErrorResponse(e);
        }
    }

    private void setupListener(final URI redirectUrl) throws IOException {
        stopListener();

        m_server = HttpServer.create(new InetSocketAddress(redirectUrl.getPort()), 10);
        m_server.createContext("/", new AuthCodeHandler());
        m_server.start();
    }

    private void stopListener() {
        if (m_server != null) {
            m_server.stop(0);
            m_server = null;
        }
    }

    private class AuthCodeHandler implements HttpHandler {
        private static final String SUCCESS_MESSAGE = """
                <html>
                    <head><title>Authentication complete</title></head>
                    <body>Authentication complete. You can close the browser.</body>
                </html>
                """;
        private static final String ERROR_MESSAGE_FORMAT = """
                <html>
                    <head><title>Authentication failed</title></head>
                    <body>Authentication failed. You may close the browser. Error details: <br> %s</body>
                </html>
                """;

        @Override
        public void handle(final HttpExchange exchange) throws IOException {
            try {
                var code = extractCode(exchange);
                sendResponse(exchange, SUCCESS_MESSAGE);
                m_authCodeFuture.complete(code);
            } catch (IOException e) {
                sendResponse(exchange, String.format(ERROR_MESSAGE_FORMAT, //
                        e.getMessage()));
                m_authCodeFuture.completeExceptionally(e);
            }
        }

        private String extractCode(final HttpExchange exchange) throws IOException {
            var code = extractField(exchange, "code");

            if (code != null) {
                var state = extractField(exchange, "state");
                if (!m_state.equals(state)) {
                    throw new IOException(
                            "Failed to validate authorization server response (state mismatch)");
                }
            } else {
                var error = extractField(exchange, "error");
                var errorDescription = Optional.ofNullable(extractField(exchange, "error_description"))
                        .orElse("not provided");

                throw createLoginFailedException(error, errorDescription);
            }
            return code;
        }

        private String extractField(final HttpExchange exchange, final String field) {

            final var query = exchange.getRequestURI().getQuery();
            if (query != null) {
                for (var param : query.split("&")) {
                    final var keyValue = param.split("=");
                    if (keyValue.length == 2 && field.equalsIgnoreCase(keyValue[0])) {
                        return URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    }
                }
            }

            return null;
        }

        private void sendResponse(final HttpExchange httpExchange, final String response) {
            try {
                httpExchange.sendResponseHeaders(200, response.length());
                try (var os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException e) { // NOSONAR
                // do nothing, browser may have already closed connection
            }
        }
    }
}
