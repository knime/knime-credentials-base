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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.knime.credentials.base.Credential;
import org.knime.credentials.base.CredentialType;
import org.knime.credentials.base.CredentialTypeRegistry;
import org.knime.credentials.base.NoOpCredentialSerializer;

/**
 * Credential for simple string access token which are not JWTs.
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 */
public final class AccessTokenCredential implements Credential, BearerTokenCredentialValue {
    /**
     *
     * The serializer class
     */
    public static class Serializer extends NoOpCredentialSerializer<AccessTokenCredential> {
    }

    /**
     * Credential type.
     */
    static final CredentialType TYPE = CredentialTypeRegistry.getCredentialType("knime.AccessTokenCredential");

    private final String m_accessToken;

    private final String m_refreshToken;

    private final Instant m_expiresAfter;

    private final String m_tokenType;

    /**
     * @param accessToken
     * @param refreshToken
     * @param expiresAfter
     * @param tokenType
     */
    public AccessTokenCredential(final String accessToken, final String refreshToken, final Instant expiresAfter,
            final String tokenType) {

        if (StringUtils.isBlank(accessToken)) {
            throw new IllegalArgumentException("Access token must not be blank");
        }

        if (StringUtils.isBlank(tokenType)) {
            throw new IllegalArgumentException("Token type must not be blank");
        }

        m_accessToken = accessToken;
        m_refreshToken = refreshToken;
        m_expiresAfter = expiresAfter;
        m_tokenType = tokenType;
    }

    /**
     * Returns the access token, which is refreshed if necessary (hence the
     * {@link IOException}).
     *
     * @return The access token.
     * @throws IOException
     *             May be thrown during token refresh.
     */
    public String getAccessToken() throws IOException {
        // TODO: implement refresh
        return m_accessToken;
    }

    /**
     * @return the optional refresh token.
     */
    public Optional<String> getRefreshToken() {
        return Optional.ofNullable(m_refreshToken);
    }

    /**
     * @return the optional expiry time of the access token.
     */
    public Optional<Instant> getExpiresAfter() {
        return Optional.ofNullable(m_expiresAfter);
    }

    /**
     * @return the tokenType
     */
    public String getTokenType() {
        return m_tokenType;
    }

    @Override
    public String getBearerToken() throws IOException {
        return getAccessToken();
    }

    @Override
    public CredentialType getType() {
        return TYPE;
    }

    @Override
    public String[][] describe() {
        List<String[]> list = new ArrayList<>();
        list.add(new String[] { "Access token", obfuscate(m_accessToken) });
        list.add(new String[] { "Access token type", m_tokenType });
        if (m_expiresAfter != null) {
            list.add(new String[] { "Expires after",
                    m_expiresAfter.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.RFC_1123_DATE_TIME) });
        }
        if (m_refreshToken != null) {
            list.add(new String[] { "Refresh token", obfuscate(m_refreshToken) });
        }
        return list.toArray(new String[0][0]);
    }

    private static String obfuscate(final String toObfuscate) {
        return toObfuscate.substring(0, 2) + "*".repeat(toObfuscate.length() - 2);
    }
}
