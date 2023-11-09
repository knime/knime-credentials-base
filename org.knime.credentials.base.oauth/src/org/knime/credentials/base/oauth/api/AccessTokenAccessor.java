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
 *   2023-11-12 (bjoern): created
 */
package org.knime.credentials.base.oauth.api;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import org.knime.credentials.base.Credential;
import org.knime.credentials.base.CredentialAccessor;

/**
 * Accessor interface for {@link Credential}s that provide an OAuth2 access
 * token.
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 */
public interface AccessTokenAccessor extends CredentialAccessor {

    /**
     * Returns the access token, which is refreshed if necessary (hence the
     * {@link IOException}).
     *
     * @return The access token.
     * @throws IOException
     *             May be thrown during token refresh.
     */
    String getAccessToken() throws IOException;

    /**
     * @return the optional expiry time of the access token.
     */
    Optional<Instant> getExpiresAfter();

    /**
     * @return the token type, e.g. "Bearer"
     */
    String getTokenType();

    /**
     * @return the scopes granted to the access token; the set might be empty if the
     *         no scopes were listed in the token endpoint response.
     */
    Set<String> getScopes();
}
