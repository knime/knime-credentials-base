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
 *   2023-04-19 (Alexander Bondaletov, Redfield SE): created
 */
package org.knime.credentials.base.oauth.api;

import java.io.IOException;
import java.text.ParseException;
import java.util.Optional;

/**
 * Generic implementation of the {@link JWTCredential}.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
public class GenericJWTCredential implements JWTCredential {

    private JWT m_accessToken;
    private JWT m_idToken;
    private JWT m_refreshToken;

    /**
     * Default constructor for serialization
     */
    public GenericJWTCredential() {
        // for serealization
    }

    /**
     * @param accessToken
     *            The access token.
     * @param idToken
     *            The id token.
     * @param refreshToken
     *            The refresh token.
     * @throws ParseException
     *
     */
    public GenericJWTCredential(final String accessToken, final String idToken, final String refreshToken)
            throws ParseException {
        m_accessToken = new JWT(accessToken);

        if (idToken != null) {
            m_idToken = new JWT(idToken);
        }

        if (refreshToken != null) {
            m_refreshToken = new JWT(refreshToken);
        }
    }

    @Override
    public JWT getAccessToken() throws IOException {
        return m_accessToken;
    }

    @Override
    public Optional<JWT> getIdToken() {
        return Optional.ofNullable(m_idToken);
    }

    @Override
    public Optional<JWT> getRefreshToken() throws IOException {
        return Optional.ofNullable(m_refreshToken);
    }
}
