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
 *   2023-03-29 (Alexander Bondaletov, Redfield SE): created
 */
package org.knime.credentials.base;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;
import org.knime.core.node.util.ConvenienceMethods;

/**
 * Interface describing credential serializer used to save and load credential
 * objects.
 *
 * @author Alexander Bondaletov, Redfield SE
 * @param <T>
 *            The credential class.
 */
public interface CredentialSerializer<T extends Credential> {

    /**
     * Stores the credential into the given config object.
     *
     * @param credential
     *            The credential to save.
     * @param config
     *            The config to save into.
     */
    void save(T credential, ConfigWO config);

    /**
     * Loads the credential from the given config.
     *
     * @param config
     *            The config to load credential from.
     * @return Loaded credential object.
     * @throws InvalidSettingsException
     */
    T load(ConfigRO config) throws InvalidSettingsException;

    /**
     * Returns the credential class that this serializer reads and writes. The class
     * is determined from the generic argument.
     *
     * @return a credential object class
     */
    default Class<T> getCredentialClass() {
        return getClassParameterFromInterfaces() //
                .or(this::getClassParameterFromSuperclasses) //
                .orElseGet(this::getClassParameterFromLoadMethod);
    }

    @SuppressWarnings("unchecked")
    private Optional<Class<T>> getClassParameterFromInterfaces() {
        for (Type type : ConvenienceMethods.getAllGenericInterfaces(getClass())) {
            if (!(type instanceof ParameterizedType)) {
                continue;
            }

            var rawType = ((ParameterizedType) type).getRawType();
            var typeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
            if (CredentialSerializer.class == rawType) {
                if (typeArgument instanceof Class) {
                    return Optional.of((Class<T>) typeArgument);
                } else if (typeArgument instanceof ParameterizedType) { // e.g. ImgPlusCell<T>
                    return Optional.of((Class<T>) ((ParameterizedType) typeArgument).getRawType());
                }
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private Optional<Class<T>> getClassParameterFromSuperclasses() {
        for (Type type : ConvenienceMethods.getAllGenericSuperclasses(getClass())) {
            if (!(type instanceof ParameterizedType)) {
                continue;
            }

            var typeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
            if (Credential.class.isAssignableFrom((Class<?>) typeArgument)) {
                if (typeArgument instanceof Class) {
                    return Optional.of((Class<T>) typeArgument);
                } else if (typeArgument instanceof ParameterizedType) {
                    return Optional.of((Class<T>) ((ParameterizedType) typeArgument).getRawType());
                }
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private Class<T> getClassParameterFromLoadMethod() {
        try {
            Class<T> c = (Class<T>) getClass().getMethod("load", ConfigRO.class).getGenericReturnType();
            if (!Credential.class.isAssignableFrom(c) || ((c.getModifiers() & Modifier.ABSTRACT) != 0)) {
                NodeLogger.getLogger(getClass())
                        .coding(getClass().getName()
                                + " does not use generics properly, the type of the created credential " + "is '"
                                + c.getName() + "'. Please fix your implementation by specifying a "
                                + "non-abstract type in the extended CredentialSerializer class.");
                return null;
            } else {
                return c;
            }
        } catch (NoSuchMethodException ex) {
            // this is not possible
            throw new AssertionError("Someone removed the 'load' method from this class");
        }
    }
}
