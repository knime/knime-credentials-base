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
 *   2023-06-20 (bjoern): created
 */
package org.knime.credentials.base.node;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.node.workflow.ICredentials;
import org.knime.core.node.workflow.VariableType.CredentialsType;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.layout.LayoutGroup;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesProvider;

/**
 * A {@link DefaultNodeSettings} implementation for when the user needs to
 * choose a flow variable of type {@link CredentialsType}. For now this is an
 * interface so that the dropdown box can be nicely labeled depending on what
 * the flow variable supplies (username/password, token, ID/Secret, ...).
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 */
@SuppressWarnings("restriction")
public interface CredentialsSettings extends DefaultNodeSettings, LayoutGroup {

    /**
     * A {@link ChoicesProvider} yielding choices for credential flow variables.
     */
    final class CredentialsFlowVarChoicesProvider implements ChoicesProvider {
        @Override
        public String[] choices(final SettingsCreationContext context) {
            return context.getAvailableInputFlowVariables(CredentialsType.INSTANCE)//
                    .keySet()//
                    .toArray(String[]::new);
        }
    }

    /**
     * @return the name of the flow variable to use
     */
    String flowVariableName();

    /**
     * Convenience method to retrieve the contents of a Credentials flow variable.
     *
     * @param credsProvider
     *            The provider from which to retrieve the flow variable contents.
     * @return an (optional) {@link ICredentials} if there was a matching
     *         Credentials flow variable, empty otherwise.
     */
    default Optional<ICredentials> retrieve(final CredentialsProvider credsProvider) {
        try {
            return Optional.of(credsProvider.get(flowVariableName()));
        } catch (IllegalArgumentException e) { // NOSONAR
            return Optional.empty();
        }
    }

    /**
     * @param credsProvider
     *            Provides access to the credential flow variables.
     * @return the login (username) from the flow variable.
     */
    default String login(final CredentialsProvider credsProvider) {
        return retrieve(credsProvider).map(ICredentials::getLogin).orElse("");
    }

    /**
     * @param credsProvider
     *            Provides access to the credential flow variables.
     * @return the secret (password) from the flow variable.
     */
    default String secret(final CredentialsProvider credsProvider) {
        return retrieve(credsProvider).map(ICredentials::getPassword).orElse("");
    }

    /**
     * Validates that a flow variable was specified and that it exists.
     *
     * @param credsProvider
     *            Provides access to the credential flow variables.
     * @throws InvalidSettingsException
     *             when validation failed.
     */
    default void validateFlowVariable(final CredentialsProvider credsProvider) throws InvalidSettingsException {
        if (StringUtils.isBlank(flowVariableName())) {
            throw new InvalidSettingsException("Please specify a credentials flow variable.");
        }

        if (retrieve(credsProvider).isEmpty()) {
            throw new InvalidSettingsException(
                    String.format("The chosen credentials flow variable '%s' does not exist.", flowVariableName()));
        }
    }

    /**
     * Validates that the flow variable is present and that the login/username field
     * is not empty.
     *
     * @param credsProvider
     *            Provides access to the credential flow variables.
     * @param msg
     *            The message to use for the {@link InvalidSettingsException}
     * @throws InvalidSettingsException
     *             when validation failed.
     */
    default void validateLogin(final CredentialsProvider credsProvider, final String msg)
            throws InvalidSettingsException {

        validateFlowVariable(credsProvider);
        retrieve(credsProvider)//
                .map(ICredentials::getLogin)//
                .filter(StringUtils::isNotBlank)//
                .orElseThrow(() -> new InvalidSettingsException(msg)); // NOSONAR this is not free of side effects
    }

    /**
     * Validates that the flow variable is present and that the password/secret
     * field is not empty.
     *
     * @param credsProvider
     *            Provides access to the credential flow variables.
     * @param msg
     *            The message to use for the {@link InvalidSettingsException}
     * @throws InvalidSettingsException
     *             when validation failed.
     */
    default void validateSecret(final CredentialsProvider credsProvider, final String msg)
            throws InvalidSettingsException {

        validateFlowVariable(credsProvider);
        retrieve(credsProvider)//
                .map(ICredentials::getPassword)//
                .filter(StringUtils::isNotBlank)//
                .orElseThrow(() -> new InvalidSettingsException(msg)); // NOSONAR this is not free of side effects
    }
}
