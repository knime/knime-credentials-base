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
 *   2023-04-10 (Alexander Bondaletov, Redfield SE): created
 */
package org.knime.credentials.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory credential cache.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
public final class CredentialCache {

    private final Map<UUID, Credential> m_credentials;

    private static final CredentialCache INSTANCE = new CredentialCache();

    private CredentialCache() {
        m_credentials = new HashMap<>();
    }

    /**
     * Stores given credential in the cache.
     *
     * @param credential
     *            The credential object.
     * @return The cacheId that could be used to retrieve or delete the credential
     *         from the cache.
     */
    public static synchronized UUID store(final Credential credential) {
        final var uuid = UUID.randomUUID();
        INSTANCE.m_credentials.put(uuid, credential);
        return uuid;
    }

    /**
     * @param <T>
     *            The credential class.
     * @param cacheId
     *            The cache id.
     * @return an optional with the credential corresponding to a given id, or an
     *         empty one, if no credential is currently cached under the given
     *         {@link UUID}.
     */
    @SuppressWarnings("unchecked")
    public static synchronized <T extends Credential> Optional<T> get(final UUID cacheId) {
        return Optional.ofNullable((T) INSTANCE.m_credentials.get(cacheId));
    }

    /**
     * Deletes the credential stored under the give id from cache.
     *
     * @param cacheId
     *            The cache id.
     */
    public static synchronized void delete(final UUID cacheId) {
        INSTANCE.m_credentials.remove(cacheId);
    }
}
