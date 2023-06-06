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
package org.knime.credentials.base.oauth2.base;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.github.scribejava.core.builder.ScopeBuilder;
import com.github.scribejava.core.builder.ServiceBuilderOAuth20;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.utils.Preconditions;

/**
 * Client credentials service builder (allows to specify custom request body
 * fields).
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 */
public class CustomOAuth2ServiceBuilder implements ServiceBuilderOAuth20 {

    private String m_callback;
    private String m_apiKey;
    private String m_apiSecret;
    private String m_scope;
    private OutputStream m_debugStream;
    private String m_responseType = "code";
    private String m_userAgent;
    private HttpClientConfig m_httpClientConfig;
    private HttpClient m_httpClient;
    private Map<String, String> m_additionalRequestBodyFields = new HashMap<>();

    public CustomOAuth2ServiceBuilder(final String apiKey) {
        apiKey(apiKey);
    }

    @Override
    public CustomOAuth2ServiceBuilder callback(final String callback) {
        m_callback = callback;
        return this;
    }

    @Override
    public CustomOAuth2ServiceBuilder apiKey(final String apiKey) {
        Preconditions.checkEmptyString(apiKey, "Invalid Api key");
        m_apiKey = apiKey;
        return this;
    }

    @Override
    public CustomOAuth2ServiceBuilder apiSecret(final String apiSecret) {
        Preconditions.checkEmptyString(apiSecret, "Invalid Api secret");
        m_apiSecret = apiSecret;
        return this;
    }

    @Override
    public CustomOAuth2ServiceBuilder apiSecretIsEmptyStringUnsafe() {
        m_apiSecret = "";
        return this;
    }

    public CustomOAuth2ServiceBuilder setScope(final String scope) {
        Preconditions.checkEmptyString(scope, "Invalid OAuth scope");
        m_scope = scope;
        return this;
    }

    @Override
    public CustomOAuth2ServiceBuilder defaultScope(final String defaultScope) {
        return setScope(defaultScope);
    }

    @Override
    public ServiceBuilderOAuth20 defaultScope(final ScopeBuilder scopeBuilder) {
        return setScope(scopeBuilder.build());
    }

    public CustomOAuth2ServiceBuilder withScope(final String scope) {
        return setScope(scope);
    }

    @Override
    public CustomOAuth2ServiceBuilder debugStream(final OutputStream debugStream) {
        Preconditions.checkNotNull(debugStream, "debug stream can't be null");
        m_debugStream = debugStream;
        return this;
    }

    @Override
    public CustomOAuth2ServiceBuilder responseType(final String responseType) {
        Preconditions.checkEmptyString(responseType, "Invalid OAuth responseType");
        m_responseType = responseType;
        return this;
    }

    @Override
    public CustomOAuth2ServiceBuilder httpClientConfig(final HttpClientConfig httpClientConfig) {
        Preconditions.checkNotNull(httpClientConfig, "httpClientConfig can't be null");
        m_httpClientConfig = httpClientConfig;
        return this;
    }

    @Override
    public CustomOAuth2ServiceBuilder httpClient(final HttpClient httpClient) {
        m_httpClient = httpClient;
        return this;
    }

    @Override
    public CustomOAuth2ServiceBuilder userAgent(final String userAgent) {
        m_userAgent = userAgent;
        return this;
    }

    @Override
    public CustomOAuth2ServiceBuilder debug() {
        return debugStream(System.out);
    }

    public CustomOAuth2ServiceBuilder additionalRequestBodyField(final String key, final String value) {
        m_additionalRequestBodyFields.put(key, value);
        return this;
    }

    @Override
    public OAuth20Service build(final DefaultApi20 api) {
        return api.createService(m_apiKey, //
                m_apiSecret, //
                m_callback, //
                m_scope, //
                m_responseType, //
                m_debugStream, //
                m_userAgent, //
                m_httpClientConfig, //
                m_httpClient);
    }

    public OAuth20Service build(final CustomApi20 api) {
        return api.createService(m_apiKey, //
                m_apiSecret, //
                m_callback, //
                m_scope, //
                m_responseType, //
                m_debugStream, //
                m_userAgent, //
                m_httpClientConfig, //
                m_httpClient, //
                m_additionalRequestBodyFields);
    }
}
