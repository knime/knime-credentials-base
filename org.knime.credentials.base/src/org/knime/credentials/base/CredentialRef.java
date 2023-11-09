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
 *   Nov 1, 2023 (bjoern): created
 */
package org.knime.credentials.base;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;
import org.knime.core.node.config.base.ConfigBaseRO;
import org.knime.core.node.config.base.ConfigBaseWO;

/**
 * A {@link CredentialRef} is similar to a {@link CredentialPortObjectSpec} in
 * the sense it holds a reference to a {@link Credential}. What's different is
 * that a {@link CredentialRef} has public methods for loading/saving the
 * reference and can be more naturally composed into other classes that save
 * themselves into port settings.
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 */
public class CredentialRef {

    private static final String KEY_CACHE_ID = "cacheId";

    /**
     * The UUID under which to look up a {@link Credential} in
     * {@link CredentialCache}.
     */
    private UUID m_cacheId;

    /**
     * Constructor for ser(de). Since this constructor will initialize the cache ID
     * to a random value, it can also be used to create an unresolvable
     * {@link CredentialRef}.
     */
    public CredentialRef() {
        m_cacheId = UUID.randomUUID();
    }

    /**
     * Constructor that creates a reference to the {@link Credential} with the given
     * cacheID.
     *
     * @param cacheId
     *            The cacheId of the {@link Credential} to reference.
     */
    public CredentialRef(final UUID cacheId) {
        m_cacheId = Objects.requireNonNull(cacheId, "Cache ID must not be null");
    }

    /**
     * Constructor that puts the given {@link Credential} into the cache and then
     * references it
     *
     * @param cred
     *            The credential to cache and then reference.
     */
    public CredentialRef(final Credential cred) {
        Objects.requireNonNull(cred, "Credential must not be null");
        m_cacheId = CredentialCache.store(cred);
    }

    /**
     * @param <C>
     *            The credential class.
     * @param credentialClass
     *            The credential class.
     * @return The credential stored in the credential cache.
     */
    public <C extends Credential> Optional<C> getCredential(final Class<C> credentialClass) {
        return CredentialCache.get(m_cacheId);
    }

    /**
     * Returns the referenced {@link Credential}.
     *
     * @param <T>
     *            The {@link Credential} subclass to return.
     * @param credentialClass
     *            The {@link Credential} subclass to return.
     * @return the referenced {@link Credential}.
     * @throws NoSuchCredentialException
     *             if the referenced credential is not present (anymore), or is not
     *             of the requested type.
     *
     */
    public <T extends Credential> T resolveCredential(final Class<T> credentialClass) throws NoSuchCredentialException {
        return CredentialPortObjectSpec.resolve(getCredential(Credential.class), credentialClass);
    }

    /**
     * Returns the referenced {@link Credential} in the shape of the given accessor
     * interface.
     *
     * @param <T>
     *            The {@link CredentialAccessor} interface to use.
     * @param accessorClass
     *            Class object of the {@link CredentialAccessor} interface to use.
     * @return the referenced {@link Credential} casted to the given accessor
     *         interface.
     * @throws NoSuchCredentialException
     *             if the referenced credential is not present (anymore), or cannot
     *             be casted to the given accessor interface.
     */
    public <T extends CredentialAccessor> T toAccessor(final Class<T> accessorClass)
            throws NoSuchCredentialException {

        return CredentialPortObjectSpec.resolve(getCredential(Credential.class), accessorClass);
    }

    /**
     * @return true if the referenced {@link Credential} can be retrieved, false
     *         otherwise.
     */
    public boolean isPresent() {
        return CredentialCache.get(m_cacheId).isPresent();
    }

    /**
     * Removes the cached {@link Credential} so that it cannot be retrieved anymore.
     */
    public void dispose() {
        CredentialCache.delete(m_cacheId);
    }

    /**
     * @return the optional {@link CredentialType} of the referenced
     *         {@link Credential}. Empty if the {@link Credential} does not exist
     *         anymore.
     */
    public Optional<CredentialType> getType() {
        return CredentialCache.get(m_cacheId).map(Credential::getType);
    }

    /**
     * Saves the reference to the given {@link ConfigWO}.
     *
     * @param config
     */
    public void save(final ConfigBaseWO config) {
        config.addString(KEY_CACHE_ID, m_cacheId.toString());
    }

    /**
     * Initializes the reference from the given {@link ConfigRO}.
     *
     * @param config
     */
    public void load(final ConfigBaseRO config) {
        m_cacheId = UUID.fromString(config.getString(KEY_CACHE_ID, null));
    }
}
