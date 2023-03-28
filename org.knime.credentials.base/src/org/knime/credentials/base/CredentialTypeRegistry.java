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
 *   2023-03-26 (Alexander Bondaletov, Redfield SE): created
 */
package org.knime.credentials.base;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.knime.core.node.NodeLogger;

/**
 * The class managing registered {@link CredentialType}'s.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
public final class CredentialTypeRegistry {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(CredentialTypeRegistry.class);

    private static final String EXT_POINT_ID = "org.knime.credentials.base.CredentialType";

    private static final CredentialTypeRegistry INSTANCE = new CredentialTypeRegistry();

    private Map<String, CredentialType> m_credentialTypes = new HashMap<>();

    private boolean m_initialized;

    private CredentialTypeRegistry() {
        m_initialized = false;
    }

    private synchronized void ensureInitialized() {
        if (!m_initialized) {
            m_initialized = true;

            final var point = Platform.getExtensionRegistry()//
                    .getExtensionPoint(EXT_POINT_ID);

            Stream.of(point.getExtensions())//
                    .flatMap(ext -> Stream.of(ext.getConfigurationElements()))//
                    .forEach(this::addCredentialType);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void addCredentialType(final IConfigurationElement e) {

        final var declaringExt = e.getDeclaringExtension().getUniqueIdentifier();
        final var id = e.getAttribute("id");

        if (m_credentialTypes.containsKey(id)) {
            LOGGER.error(String.format(
                    "Ignoring extension %s because it tries to register an duplicate credential type with ID %s",
                    declaringExt, //
                    id));
            return;
        }

        try {
            final var name = e.getAttribute("name");
            final var serializer = (CredentialSerializer<T>) e.createExecutableExtension("credentialSerializerClass");

            m_credentialTypes.put(id, //
                    new CredentialType(id, name, serializer.getCredentialClass(), serializer));

        } catch (Throwable ex) { // NOSONAR
            LOGGER.error(String.format("Problems during initialization of credential type with id '%s'.", id), ex);
            LOGGER.error(String.format("Extension %s ignored.", declaringExt));
        }
    }

    /**
     * @return the map containing all of the available credential types.
     */
    public static Map<String, CredentialType> getCredentialTypes() {
        INSTANCE.ensureInitialized();
        return Collections.unmodifiableMap(INSTANCE.m_credentialTypes);
    }

    /**
     * @param id
     *            The unique ID of this credential type.
     * @return the {@link CredentialType} corresponding to the given credential
     *         class
     */
    public static CredentialType getCredentialType(final String id) {
        return getCredentialTypes().get(id);
    }
}
