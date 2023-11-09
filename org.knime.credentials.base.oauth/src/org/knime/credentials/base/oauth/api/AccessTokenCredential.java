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

import static org.knime.credentials.base.CredentialPortViewUtil.obfuscate;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.knime.credentials.base.Credential;
import org.knime.credentials.base.CredentialPortViewData;
import org.knime.credentials.base.CredentialType;
import org.knime.credentials.base.CredentialTypeRegistry;
import org.knime.credentials.base.NoOpCredentialSerializer;

/**
 * {@link Credential} implementation for simple string access tokens, which are
 * not JWTs.
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 */
public class AccessTokenCredential
        implements Credential, AccessTokenAccessor, HttpAuthorizationHeaderCredentialValue {
    /**
     * The serializer class
     */
    public static class Serializer extends NoOpCredentialSerializer<AccessTokenCredential> {
    }

    /**
     * Credential type.
     */
    public static final CredentialType TYPE = CredentialTypeRegistry.getCredentialType("knime.AccessTokenCredential");

    private static final Pattern WHITESPACES_PATTERN = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);

    private String m_accessToken;

    private String m_tokenType;

    private Instant m_expiresAfter;

    private Set<String> m_scopes;

    private Supplier<AccessTokenCredential> m_tokenRefresher;

    /**
     * @param accessToken
     *            The access token.
     * @param expiresAfter
     *            The instant when the access token expires. May be null.
     * @param tokenType
     *            The type of access token, e.g. "bearer".
     *
     * @param tokenRefresher
     *            Function that retrieves a new access token. May be null.
     */
    public AccessTokenCredential(final String accessToken, final Instant expiresAfter, final String tokenType,
            final Supplier<AccessTokenCredential> tokenRefresher) {
        this(accessToken, expiresAfter, tokenType, Set.of(), tokenRefresher);
    }

    /**
     * @param accessToken
     *            The access token.
     * @param expiresAfter
     *            The instant when the access token expires. May be null.
     * @param tokenType
     *            The type of access token, e.g. "bearer".
     * @param scopes
     *            The scopes granted (as a string).
     * @param tokenRefresher
     *            Function that retrieves a new access token. May be null.
     */
    public AccessTokenCredential(final String accessToken, final Instant expiresAfter, final String tokenType,
            final String scopes, final Supplier<AccessTokenCredential> tokenRefresher) {
        this(accessToken, expiresAfter, tokenType,
                scopes == null ? Set.of() : Set.of(WHITESPACES_PATTERN.split(scopes)), tokenRefresher);
    }

    /**
     * @param accessToken
     *            The access token.
     * @param expiresAfter
     *            The instant when the access token expires. May be null.
     * @param tokenType
     *            The type of access token, e.g. "bearer".
     * @param scopes
     *            The scopes granted.
     * @param tokenRefresher
     *            Function that retrieves a new access token. May be null.
     */
    public AccessTokenCredential(final String accessToken, final Instant expiresAfter, final String tokenType,
            final Set<String> scopes, final Supplier<AccessTokenCredential> tokenRefresher) {

        if (StringUtils.isBlank(accessToken)) {
            throw new IllegalArgumentException("Access token must not be blank");
        }

        if (StringUtils.isBlank(tokenType)) {
            throw new IllegalArgumentException("Token type must not be blank");
        }

        m_accessToken = accessToken;
        m_tokenType = tokenType;
        m_expiresAfter = expiresAfter;
        m_scopes = Objects.requireNonNull(scopes, "List of scopes must no be null");
        m_tokenRefresher = tokenRefresher;

        // if the service has provided a refresh token, but we cannot determine when the
        // access token expires, then we will refresh the access token every 60 seconds.
        if (m_tokenRefresher != null && m_expiresAfter == null) {
            m_expiresAfter = Instant.now().plusSeconds(60);
        }
    }

    @Override
    public String getAccessToken() throws IOException {
        refreshTokenIfNeeded();
        return m_accessToken;
    }

    private void refreshTokenIfNeeded() throws IOException {
        if (m_tokenRefresher != null && m_expiresAfter.isBefore(Instant.now())) {

            try {
                final var refreshedCredential = m_tokenRefresher.get();

                if (!m_tokenType.equalsIgnoreCase(refreshedCredential.m_tokenType)) {
                    throw new IOException(
                            String.format("Token type has changed during refresh. Was %s, but has become %s", //
                                    m_tokenType, //
                                    refreshedCredential.m_accessToken));
                }

                m_accessToken = refreshedCredential.m_accessToken;
                m_expiresAfter = refreshedCredential.m_expiresAfter;

                if (refreshedCredential.m_tokenRefresher != null) {
                    m_tokenRefresher = refreshedCredential.m_tokenRefresher;
                }

            } catch (UncheckedIOException e) { // NOSONAR just a wrapper
                throw e.getCause();
            }
        }
    }

    @Override
    public Optional<Instant> getExpiresAfter() {
        return Optional.ofNullable(m_expiresAfter);
    }

    @Override
    public String getTokenType() {
        return m_tokenType;
    }

    @Override
    public String getAuthScheme() {
        return Character.toUpperCase(m_tokenType.charAt(0)) + m_tokenType.substring(1);
    }

    @Override
    public String getAuthParameters() throws IOException {
        return getAccessToken();
    }

    @Override
    public CredentialType getType() {
        return TYPE;
    }

    @Override
    public Set<String> getScopes() {
        return m_scopes;
    }

    @Override
    public CredentialPortViewData describe() {
        final var sections = new LinkedList<CredentialPortViewData.Section>();

        sections.add(new CredentialPortViewData.Section("Access token", new String[][] { //
                { "Property", "Value" }, //
                { "Token", obfuscate(m_accessToken) }, //
                { "Token type", m_tokenType }, //
                { "Expires after", m_expiresAfter != null//
                        ? m_expiresAfter.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.RFC_1123_DATE_TIME)
                        : "n/a" }, //
                { "Is refreshable", Boolean.toString(m_tokenRefresher != null) }, //
                { "Scopes", m_scopes.toString() }
        }));

        return new CredentialPortViewData(sections);
    }
}
