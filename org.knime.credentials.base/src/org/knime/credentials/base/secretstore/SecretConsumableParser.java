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
 *   2024-01-24 (bjoern): created
 */
package org.knime.credentials.base.secretstore;

import org.knime.credentials.base.Credential;
import org.knime.credentials.base.CredentialTypeRegistry;

import jakarta.json.JsonObject;

/**
 * <p>
 * This interface is part of the KNIME Hub Secret Store integration, which most
 * importantly provides the Secret Retriever node. See
 * {@link CredentialTypeRegistry#getSecretConsumableParser(String)} for
 * informational context.
 * </p>
 *
 * <p>
 * Functional interface to implement a parser from a Secret Store consumable to
 * a {@link Credential}. Implementations should use {@link ParserUtil} to
 * generate good error messages.
 * </p>
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 * @param <T>
 *            The type of {@link Credential} created by this parser.
 * @since 5.2.1
 */
@FunctionalInterface
public interface SecretConsumableParser<T extends Credential> {

    /**
     * Parses a Secret Store consumable into a {@link Credential}. Implementations
     * should use {@link ParserUtil} to generate good error messages.
     *
     * <p>
     * The Secret Retriever calls the Secret Store
     * /secret-store/secrets/:secretId/consume endpoint, which returns a JSON
     * response. Inside that response is a field called "secret" whose value is
     * called a "consumable". Only this consumable is then passed to this method
     * here.
     *
     * @param consumable
     *            A {@link JsonObject} with the "consumable" ( the "secret" field of
     *            the consume endpoint response).
     * @return a {@link Credential} that has been parsed from the consumable.
     * @throws UnparseableSecretConsumableException
     *             when the consumable could not be parsed successfully, e.g. fields
     *             were missing.
     */
    T parse(JsonObject consumable) throws UnparseableSecretConsumableException;
}
