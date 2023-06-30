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
 *   2023-06-06 (bjoern): created
 */
package org.knime.credentials.base.oauth.api.scribejava;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * Custom {@link OAuth20Service} subclass that can inject custom request body
 * fields into the client credentials grant request.
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 */
class CustomOAuth20Service extends OAuth20Service {

    private Map<String, String> m_additionalRequestBodyFields = new HashMap<>();

    CustomOAuth20Service(final DefaultApi20 api, // NOSONAR
            final String apiKey, //
            final String apiSecret, //
            final String callback, //
            final String defaultScope, //
            final String responseType, //
            final OutputStream debugStream, //
            final String userAgent, //
            final HttpClientConfig httpClientConfig, //
            final HttpClient httpClient, //
            final Map<String, String> additionalRequestBodyFields) {

        super(api, apiKey, apiSecret, callback, defaultScope, responseType, debugStream, userAgent, httpClientConfig,
                httpClient);
        m_additionalRequestBodyFields = additionalRequestBodyFields;
    }

    @Override
    protected OAuthRequest createAccessTokenClientCredentialsGrantRequest(final String scope) {

        final var request = new OAuthRequest(getApi().getAccessTokenVerb(), //
                getApi().getAccessTokenEndpoint());

        getApi().getClientAuthentication().addClientAuthentication(request, getApiKey(), getApiSecret());

        if (scope != null) {
            request.addParameter(OAuthConstants.SCOPE, scope);
        } else if (getDefaultScope() != null) {
            request.addParameter(OAuthConstants.SCOPE, getDefaultScope());
        }
        request.addParameter(OAuthConstants.GRANT_TYPE, OAuthConstants.CLIENT_CREDENTIALS);

        m_additionalRequestBodyFields.entrySet().stream()
                .forEach(entry -> request.addParameter(entry.getKey(), entry.getValue()));

        logRequestWithParams("access token client credentials grant", request);

        return request;
    }
}
