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
 *   2023-06-12 (bjoern): created
 */
package org.knime.credentials.base.oauth.api.scribejava;

import java.io.IOException;
import java.util.Optional;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * Base class for OAuth2 based login implementations. Each subclasses implements
 * an OAuth 2 authentication flow and produces a {@link OAuth2AccessToken}.
 * Errors during login are handled consistently across subclasses.
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 */
abstract class FlowBase {

    private final OAuth20Service m_service;

    /**
     * Creates a new instance.
     *
     *
     * @param service
     */
    FlowBase(final OAuth20Service service) {
        m_service = service;
    }

    /**
     * @return the underlying {@link OAuth20Service}.
     */
    public final OAuth20Service getService() {
        return m_service;
    }

    /**
     * Performs the actual login.
     *
     * @param scopes
     *            The OAuth2 scopes to request. May be null.
     * @return the {@link OAuth2AccessToken} if the login was successful.
     * @throws Exception
     *             if the login failed.
     */
    public abstract OAuth2AccessToken login(final String scopes) throws Exception; // NOSONAR

    /**
     * Internal helper method to consistently handle an
     * {@link OAuth2AccessTokenErrorResponse}.
     *
     * @param tokenError
     *            The {@link OAuth2AccessTokenErrorResponse} to handle.
     * @return the wrapped exception with a nicer error message.
     */
    @SuppressWarnings("resource")
    protected Exception wrapAccessTokenErrorResponse(final OAuth2AccessTokenErrorResponse tokenError) {

        var oauth2Error = tokenError.getError();
        if (oauth2Error != null) {
            return createLoginFailedException(oauth2Error.getErrorString(), tokenError.getErrorDescription());
        } else {
            var response = tokenError.getResponse();
            return new IOException(String.format("Could not retrieve access token (HTTP %d - %s)", //
                    response.getCode(), //
                    Optional.ofNullable(response.getMessage()).orElse("no message provided")));
        }
    }

    /**
     * Internal helper method to consistently handle an OAuth error.
     *
     * @param error
     *            The error code.
     * @param errorDescription
     *            The error description.
     *
     * @return the wrapped exception with a nicer error message.
     */
    protected IOException createLoginFailedException(final String error, final String errorDescription) {
        return new IOException(String.format("Could not retrieve access token (%s - %s", //
                error, //
                Optional.ofNullable(errorDescription).orElse("no message provided")));
    }
}
