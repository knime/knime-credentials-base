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
 *   2024-01-25 (bjoern): created
 */
package org.knime.credentials.base.secretstore;

import org.apache.commons.lang3.StringUtils;
import org.knime.credentials.base.CredentialTypeRegistry;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue.ValueType;

/**
 * <p>
 * This class is part of the KNIME Hub Secret Store integration, which most
 * importantly provides the Secret Retriever node. See
 * {@link CredentialTypeRegistry#getSecretConsumableParser(String)} for
 * informational context.
 * </p>
 *
 * Utility class to more easily implement {@link SecretConsumableParser}s.
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 * @since 5.2.1
 */
public final class ParserUtil {

    private ParserUtil() {
    }

    /**
     * Retrieves a string field from a {@link JsonObject}, throwing descriptive
     * error messages if not present.
     *
     * @param json
     *            A JSON object.
     * @param fieldName
     *            The field for which to retrieve the value.
     * @return the field value
     * @throws UnparseableSecretConsumableException
     *             when the field is missing, has the wrong type or is blank.
     */
    public static String getStringField(final JsonObject json, final String fieldName)
            throws UnparseableSecretConsumableException {
        return getStringField(json, fieldName, false);
    }

    /**
     * Retrieves a string field from a {@link JsonObject}, throwing descriptive
     * error messages if not present.
     *
     * @param json
     *            A JSON object.
     * @param fieldName
     *            The field for which to retrieve the value.
     * @param allowBlank
     *            Whether to allow a blank value or not.
     * @return the field value
     * @throws UnparseableSecretConsumableException
     *             when the field is missing, has the wrong type or is blank (if not
     *             allowed).
     */
    public static String getStringField(final JsonObject json, final String fieldName, final boolean allowBlank)
            throws UnparseableSecretConsumableException {

        if (!json.containsKey(fieldName)) {
            throw new UnparseableSecretConsumableException("Invalid data (field '%s' is missing)".formatted(fieldName));
        }

        if (json.get(fieldName).getValueType() != ValueType.STRING) {
            throw new UnparseableSecretConsumableException(
                    "Invalid data (field '%s' is expected to be a string)".formatted(fieldName));
        }

        final var value = json.getString(fieldName);

        if (!allowBlank && StringUtils.isBlank(value)) {
            throw new UnparseableSecretConsumableException(
                    "Invalid data (field '%s' must not be blank)".formatted(fieldName));
        }

        return value;
    }
}
