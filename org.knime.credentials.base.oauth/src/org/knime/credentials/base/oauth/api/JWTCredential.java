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
 *   2023-04-16 (Alexander Bondaletov, Redfield SE): created
 */
package org.knime.credentials.base.oauth.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.knime.credentials.base.Credential;
import org.knime.credentials.base.CredentialType;
import org.knime.credentials.base.CredentialTypeRegistry;
import org.knime.credentials.base.NoOpCredentialSerializer;

/**
 * An interface representing JWT {@link Credential} type.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
public interface JWTCredential extends Credential, BearerTokenCredentialValue {
    /**
     *
     * The serializer class
     */
    public static class Serializer extends NoOpCredentialSerializer<JWTCredential> {
    }

    /**
     * Credential type.
     */
    static final CredentialType TYPE = CredentialTypeRegistry
            .getCredentialType("org.knime.credentials.base.oauth.api.JWTCredential");

    /**
     * Returns the access token. Access token is refreshed if necessary.
     *
     * @return The access token.
     * @throws IOException
     *             May be thrown during token refresh.
     */
    JWT getAccessToken() throws IOException;

    /**
     * @return The optional holding the id token.
     */
    Optional<JWT> getIdToken();

    /**
     * Returns the refresh token. The access token is refreshed if necessary.
     *
     * @return The optional holding the refresh token.
     * @throws IOException
     *             May be thrown during token refresh.
     */
    Optional<JWT> getRefreshToken() throws IOException;

    @Override
    default String getBearerToken() throws IOException {
        return getAccessToken().asString();
    }

    @Override
    default CredentialType getType() {
        return TYPE;
    }

    @Override
    default String[][] describe() {
        List<String[]> list = new ArrayList<>();
        try {
            list.addAll(describe(getAccessToken(), "access-token-"));
            getIdToken().ifPresent(t -> list.addAll(describe(t, "id-token-")));
            getRefreshToken().ifPresent(t -> list.addAll(describe(t, "refresh-token-")));
        } catch (IOException ex) {// NOSONAR error message is attached to description
            list.add(new String[] { "error", ex.getMessage() });
        }
        return list.toArray(new String[0][0]);
    }

    private static List<String[]> describe(final JWT token, final String prefix) {
        List<String[]> list = new ArrayList<>();
        Map<String, Object> claims = token.getAllClaims();
        for (Entry<String, Object> e : claims.entrySet()) {
            list.add(new String[] { prefix + e.getKey(), e.getValue().toString() });
        }
        return list;
    }
}
