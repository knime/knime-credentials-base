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
 *   2023-04-06 (Alexander Bondaletov, Redfield SE): created
 */
package org.knime.credentials.base;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

/**
 * Port object providing access to a {@link Credential} stored in the in-memory
 * storage.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
public class CredentialPortObject extends AbstractSimplePortObject {
    private static final String KEY_CACHE_ID = "cacheId";

    /**
     * Serializer class
     */
    public static final class Serializer extends AbstractSimplePortObjectSerializer<CredentialPortObject> {
    }

    /**
     * The type of this port.
     */
    @SuppressWarnings("hiding")
    public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(CredentialPortObject.class);

    private CredentialPortObjectSpec m_spec;
    private UUID m_cacheId;

    /**
     * Public constructor (only for deserialization).
     */
    public CredentialPortObject() {
        m_spec = null;
        m_cacheId = null;
    }

    /**
     * Creates new instance with a given spec.
     *
     * @param spec
     *            The spec.
     * @param cacheId
     *            The cache id.
     *
     */
    public CredentialPortObject(final CredentialPortObjectSpec spec, final UUID cacheId) {
        if (spec.getCredentialType().isEmpty()) {
            throw new IllegalArgumentException("Credential type of spec must be known");
        }
        m_spec = spec;
        m_cacheId = cacheId;
    }

    /**
     * @return the credentialType of the credentials the port object provides access
     *         to.
     */
    public CredentialType getCredentialType() {
        return m_spec.getCredentialType().orElseThrow();
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

    @Override
    public String getSummary() {
        return String.format("Credential (%s)", getCredentialType().getName());
    }

    @Override
    public PortObjectSpec getSpec() {
        return m_spec;
    }

    @Override
    protected void save(final ModelContentWO model, final ExecutionMonitor exec) throws CanceledExecutionException {
        model.addString(KEY_CACHE_ID, m_cacheId.toString());
    }

    @Override
    protected void load(final ModelContentRO model, final PortObjectSpec spec, final ExecutionMonitor exec)
            throws InvalidSettingsException, CanceledExecutionException {
        m_spec = (CredentialPortObjectSpec) spec;
        m_cacheId = UUID.fromString(model.getString(KEY_CACHE_ID));
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final var other = (CredentialPortObject) obj;
        return Objects.equals(m_spec, other.m_spec) && Objects.equals(m_cacheId, other.m_cacheId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_spec, m_cacheId);
    }
}
