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
 *   2024-01-26 (bjoern): created
 */
package org.knime.credentials.base.secretstore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.knime.credentials.base.Credential;
import org.knime.credentials.base.CredentialTypeRegistry;

/**
 * <p>
 * This interface is part of the KNIME Hub Secret Store integration, which most
 * importantly provides the Secret Retriever node. See
 * {@link CredentialTypeRegistry#getSecretConsumableParser(String)} for
 * informational context.
 * </p>
 *
 * <p>
 * This provider class holds one or more {@link SecretConsumableParser}s. If one
 * or more {@link SecretConsumableParser}s should be registered, a
 * CredentialType extension should create a subclass with a zero-argument
 * default constructor. The subclass can then be added to the extension in the
 * plugin.xml.
 * </p>
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 * @param <T>
 *            The type of {@link Credential} created by the parsers in this
 *            provider.
 * @since 5.2.1
 */
public class SecretConsumableParserProvider<T extends Credential> {

    /**
     * A map from Secret Store secret types to parsers.
     */
    protected Map<String, SecretConsumableParser<T>> m_parsers = new HashMap<>();

    /**
     * Constructor that sets a single secret type to parser mapping.
     *
     * @param secretType
     *            The Secret Store secret type to
     * @param parser
     */
    protected SecretConsumableParserProvider(final String secretType, final SecretConsumableParser<T> parser) {
        m_parsers.put(secretType, parser);
    }

    /**
     * Constructor that sets a map of secret type to parser mappings.
     *
     * @param parsers
     */
    protected SecretConsumableParserProvider(final Map<String, SecretConsumableParser<T>> parsers) {
        m_parsers.putAll(parsers);
    }

    /**
     *
     * @return a map that maps secret types to parsers.
     */
    public Map<String, SecretConsumableParser<T>> parsers() {
        return Collections.unmodifiableMap(m_parsers);
    }

    /**
     *
     * @param secretType
     *            A Secret Store secret type.
     * @return a parser for the given secret type, or null, if none was found.
     */
    public SecretConsumableParser<T> parser(final String secretType) {
        return parsers().get(secretType);
    }

    /**
     * @return the secret types for which a parser was configured.
     */
    public Set<String> secretTypes() {
        return parsers().keySet();
    }
}
