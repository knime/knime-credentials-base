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
 *   2023-05-08 (Alexander Bondaletov, Redfield SE): created
 */
package org.knime.credentials.base.oauth.api.scribejava;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import com.github.scribejava.core.base64.Base64;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
import com.github.scribejava.core.oauth2.clientauthentication.HttpBasicAuthenticationScheme;

/**
 * {@link DefaultApi20} implementation that allows to configure several
 * settings, such as authorization/token URLs.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
public class CustomApi20 extends DefaultApi20 {

    private final String m_tokenUrl;
    private final String m_authorizationUrl;
    private final Verb m_requestMethod;
    private final ClientAuthentication m_clientAuthentication;

    /**
     * @param tokenUrl
     *            Access token endpoint URL.
     * @param authorizationUrl
     *            Authorization endpoint URL.
     * @param requestMethod
     *            Authorization request method.
     * @param clientAuthentication
     *            Client authentication type.
     *
     */
    public CustomApi20(final String tokenUrl, final String authorizationUrl, final Verb requestMethod,
            final ClientAuthentication clientAuthentication) {
        m_tokenUrl = tokenUrl;
        m_authorizationUrl = authorizationUrl;
        m_requestMethod = requestMethod;

        if (clientAuthentication == HttpBasicAuthenticationScheme.instance()) {
            // fixes a bug in HttpBasicAuthenticationScheme, see
            // FixedHttpBasicAuthenticationScheme javadoc
            m_clientAuthentication = FixedHttpBasicAuthenticationScheme.instance();
        } else {
            m_clientAuthentication = clientAuthentication;
        }
    }

    @Override
    public String getAccessTokenEndpoint() {
        return m_tokenUrl;
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return m_authorizationUrl;
    }

    @Override
    public Verb getAccessTokenVerb() {
        return m_requestMethod;
    }

    @Override
    public ClientAuthentication getClientAuthentication() {
        return m_clientAuthentication;
    }

    @Override
    public OAuth20Service createService(final String apiKey, final String apiSecret, final String callback,
            final String defaultScope, final String responseType, final OutputStream debugStream,
            final String userAgent, final HttpClientConfig httpClientConfig, final HttpClient httpClient) {

        return createService(apiKey, //
                apiSecret, //
                callback, //
                defaultScope, //
                responseType, //
                debugStream, //
                userAgent, //
                httpClientConfig, //
                httpClient, //
                Collections.emptyMap());
    }

    /**
     * Creates an {@link OAuth20Service} instance using the given parameters. This
     * method allows to specify additional request body fields.
     *
     *
     * @param apiKey
     * @param apiSecret
     * @param callback
     * @param defaultScope
     * @param responseType
     * @param debugStream
     * @param userAgent
     * @param httpClientConfig
     * @param httpClient
     * @param additionalRequestBodyFields
     * @return a new {@link OAuth20Service} instance
     */
    public CustomOAuth20Service createService(final String apiKey, final String apiSecret, final String callback,
            final String defaultScope, final String responseType, final OutputStream debugStream,
            final String userAgent, final HttpClientConfig httpClientConfig, final HttpClient httpClient,
            final Map<String, String> additionalRequestBodyFields) {

        return new CustomOAuth20Service(this, //
                apiKey, //
                apiSecret, //
                callback, //
                defaultScope, //
                responseType, //
                debugStream, //
                userAgent, //
                httpClientConfig, //
                httpClient, //
                additionalRequestBodyFields);
    }

    /**
     * Fixes an issue in the scribejava HttpBasicAuthenticationScheme class, which
     * is that the client_id is not put into the request body, if no apiSecret
     * (client secret) is provided. RFC 6749 Section 4.1.3 mandates this:
     * https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.3
     */
    private static class FixedHttpBasicAuthenticationScheme implements ClientAuthentication {

        private static ClientAuthentication INSTANCE = new FixedHttpBasicAuthenticationScheme();

        private FixedHttpBasicAuthenticationScheme() {
        }

        static ClientAuthentication instance() {
            return INSTANCE;
        }

        @Override
        public void addClientAuthentication(final OAuthRequest request, final String apiKey, final String apiSecret) {
            if (apiKey != null && apiSecret != null) {
                request.addHeader(OAuthConstants.HEADER, //
                        OAuthConstants.BASIC + ' ' + Base64
                                .encode(String.format("%s:%s", apiKey, apiSecret).getBytes(Charset.forName("UTF-8"))));
            } else if (apiKey != null && apiSecret == null) {
                request.addParameter(OAuthConstants.CLIENT_ID, apiKey);
            }
        }
    }
}
