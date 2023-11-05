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
 *   2023-04-03 (Alexander Bondaletov, Redfield SE): created
 */
package org.knime.credentials.base;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;

/**
 * Specification for the {@link CredentialPortObject}.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
public class CredentialPortObjectSpec extends AbstractSimplePortObjectSpec {
    /**
     * Serializer class.
     */
    public static final class Serializer extends AbstractSimplePortObjectSpecSerializer<CredentialPortObjectSpec> {
    }

    private static final String KEY_TYPE = "type";

    private static final String KEY_CACHE_ID = "cacheId";

    private CredentialType m_credentialType;

    private UUID m_cacheId;

    /**
     * Creates new instance.
     */
    public CredentialPortObjectSpec() {
        this(null, null);
    }

    /**
     * Creates a new instance. During node mode configure() it is okay to call this
     * method with one or both arguments null. During
     *
     * @param credentialType
     *            The credential type. May be null, but then cacheId must also be
     *            null.
     * @param cacheId
     *            The cache id. May be null, if currently unknown.
     */
    public CredentialPortObjectSpec(final CredentialType credentialType, final UUID cacheId) {
        if (credentialType == null && cacheId != null) {
            throw new IllegalArgumentException("If cacheId is given, then the credential type must be known");
        }

        m_credentialType = credentialType;
        m_cacheId = cacheId;
    }

    /**
     * Sets the cache ID. Subclasses are only supposed to invoke this when loading
     * legacy ports.
     *
     * @param cacheID
     */
    protected void setCacheId(final UUID cacheID) {
        m_cacheId = cacheID;
    }

    /**
     * Sets the credential type. Subclasses are only supposed to invoke this when
     * loading legacy ports.
     *
     * @param credentialType
     */
    protected void setCredentialType(final CredentialType credentialType) {
        m_credentialType = credentialType;
    }

    /**
     * @return the optional credentialType of the {@link Credential} the port object
     *         provides access to, which can be empty if currently unknown.
     */
    public Optional<CredentialType> getCredentialType() {
        return Optional.ofNullable(m_credentialType);
    }

    /**
     * @param <T>
     *            The credential class.
     * @param credentialClass
     *            The credential class.
     * @return The credential stored in the credential cache.
     */
    public <T extends Credential> Optional<T> getCredential(final Class<T> credentialClass) {
        return CredentialCache.get(m_cacheId);
    }

    /**
     * @return a new {@link CredentialRef} that references the same (cached)
     *         credential.
     */
    public CredentialRef toRef() {
        return Optional.ofNullable(m_cacheId)//
                .map(CredentialRef::new)//
                .orElse(new CredentialRef());
    }

    @Override
    protected void save(final ModelContentWO model) {
        model.addString(KEY_TYPE, getCredentialType().map(CredentialType::getId).orElse(null));
        model.addString(KEY_CACHE_ID, m_cacheId != null ? m_cacheId.toString() : null);
    }

    @Override
    protected void load(final ModelContentRO model) throws InvalidSettingsException {
        m_credentialType = Optional.ofNullable(model.getString(KEY_TYPE, null))//
                .map(CredentialTypeRegistry::getCredentialType)//
                .orElse(null);

        // the cacheId was moved from PortObject to PortObjectSpec with AP 5.2,
        // but it is also possible for it to be null
        m_cacheId = Optional.ofNullable(model.getString(KEY_CACHE_ID, null))//
                .map(UUID::fromString)//
                .orElse(null);
    }

    @Override
    public boolean equals(final Object ospec) {
        if (ospec == null) {
            return false;
        }

        if (this == ospec) {
            return true;
        }

        if (ospec.getClass() != getClass()) {
            return false;
        }

        final var spec = (CredentialPortObjectSpec) ospec;
        return Objects.equals(m_credentialType, spec.m_credentialType) && Objects.equals(m_cacheId, spec.m_cacheId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_credentialType, m_cacheId);
    }
}
