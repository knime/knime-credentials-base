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
 *   2025-05-21 (bjoern): created
 */
package org.knime.credentials.base.oauth.api;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.knime.credentials.base.Credential;
import org.knime.credentials.base.CredentialPortViewData;
import org.knime.credentials.base.CredentialType;
import org.knime.credentials.base.CredentialTypeRegistry;
import org.knime.credentials.base.NoOpCredentialSerializer;

/**
 * {@link Credential} implementation that can fetch OAuth2 access tokens ad-hoc
 * for a given set of scopes. This class should only be referenced by
 * authenticator nodes. for situations. Code that wants to consume the resulting
 * access token should use the {@link AccessTokenWithScopesAccessor} interface.
 *
 * <p>
 * This credential is useful for authenticator nodes that hold some credentials
 * which themselves can be used to acquire access tokens ad-hoc for a given set
 * of scopes. The token fetching logic must be encapsulated in the provided
 * token fetcher function.
 * </p>
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 */
public class AccessTokenWithScopesCredential implements Credential, AccessTokenWithScopesAccessor {

    /**
     * Credential type.
     */
    public static final CredentialType TYPE = CredentialTypeRegistry
            .getCredentialType("knime.AccessTokenWithScopeCredential");

    /**
     * The serializer class
     */
    public static class Serializer extends NoOpCredentialSerializer<AccessTokenCredential> {
    }

    private final Map<Set<String>, AccessTokenAccessor> m_cachedTokens = new HashMap<>();

    private final Function<Set<String>, AccessTokenAccessor> m_tokenFecher;

    /**
     * Constructor.
     *
     * @param tokenFetcher
     *            Creates a new {@link AccessTokenAccessor} for a given set of
     *            requested scopes. The token fetcher is expected to throw an
     *            {@link UncheckedIOException} if the token could not be fetched.
     */
    public AccessTokenWithScopesCredential(final Function<Set<String>, AccessTokenAccessor> tokenFetcher) {
        m_tokenFecher = tokenFetcher;
    }

    @Override
    public synchronized AccessTokenAccessor getAccessTokenWithScopes(final Set<String> scopes) throws IOException {

        AccessTokenAccessor token = m_cachedTokens.get(scopes);
        if (token == null) {
            try {
                token = m_tokenFecher.apply(scopes);
                m_cachedTokens.put(scopes, token);
            } catch (UncheckedIOException e) { // NOSONAR this is just a wrapper
                throw e.getCause();
            }
        }
        return token;
    }

    @Override
    public CredentialType getType() {
        return TYPE;
    }

    @Override
    public CredentialPortViewData describe() {
        final var sections = new LinkedList<CredentialPortViewData.Section>();

        final var cachedScopes = new LinkedList<String[]>();
        cachedScopes.add(new String[] { "Access token scopes", "Expires after" });

        synchronized(this) {
            cachedScopes.addAll(m_cachedTokens.keySet()//
                    .stream()//
                    .map(s -> new String[] { //
                            s.toString(), //
                            m_cachedTokens.get(s).getExpiresAfter().map(//
                                    i -> i.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.RFC_1123_DATE_TIME))
                                    .orElse("n/a") })//
                    .toList());
        }

        sections.add(
                new CredentialPortViewData.Section(//
                        "Cached OAuth2 access tokens", //
                        cachedScopes.toArray(String[][]::new)));

        return new CredentialPortViewData(sections);
    }
}
