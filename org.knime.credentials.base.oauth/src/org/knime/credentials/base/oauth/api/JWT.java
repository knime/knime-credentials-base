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
 *   2023-04-17 (Alexander Bondaletov, Redfield SE): created
 */
package org.knime.credentials.base.oauth.api;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

/**
 * JWT token.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
public class JWT {
    private static final String SCOPE_CLAIM = "scope";
    private static final Pattern SCOPE_SEPARATOR_PATTERN = Pattern.compile("(?U)\\s+");

    private final String m_token;
    private final JWTClaimsSet m_parsed;

    /**
     * @param token
     *            The token as string.
     * @throws ParseException
     */
    public JWT(final String token) throws ParseException {
        m_token = token;
        m_parsed = JWTParser.parse(token).getJWTClaimsSet();
    }

    /**
     * @return The map containing all of the claims.
     */
    public Map<String, Object> getAllClaims() {
        return m_parsed.getClaims();
    }

    /**
     * @return The optional holding the expiration time.
     */
    public Optional<Instant> getExpirationTime(){
        return Optional.ofNullable(m_parsed.getExpirationTime()).map(Date::toInstant);
    }

    /**
     * @return The optional holding the issuedAt time.
     */
    public Optional<Instant> getIssuedAt() {
        return Optional.ofNullable(m_parsed.getIssueTime()).map(Date::toInstant);
    }

    /**
     * @return The optional holding the issuer.
     */
    public Optional<String> getIssuer() {
        return Optional.ofNullable(m_parsed.getIssuer());
    }

    /**
     * @return The optional holding the subject.
     */
    public Optional<String> getSubject() {
        return Optional.ofNullable(m_parsed.getSubject());
    }

    /**
     * @return The optional holding the scopes.
     */
    public Optional<List<String>> getScopes() {
        try {
            var scopesStr = m_parsed.getStringClaim(SCOPE_CLAIM);

            if (scopesStr != null) {
                return Optional.of(List.of(SCOPE_SEPARATOR_PATTERN.split(scopesStr)));
            }
        } catch (ParseException ex) {
            // ignore
        }

        return Optional.empty();
    }

    /**
     * @return The token as a string.
     */
    public String asString() {
        return m_token;
    }
}
