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
 *   2023-06-29 (bjoern): created
 */
package org.knime.credentials.base.node;

import java.util.List;
import java.util.Optional;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.webui.node.dialog.configmapping.ConfigMigration;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Migration;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.NodeSettingsMigration;
import org.knime.core.webui.node.dialog.defaultdialog.setting.credentials.Credentials;
import org.knime.core.webui.node.dialog.defaultdialog.setting.credentials.LegacyCredentials;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;

/**
 * Implementation of {@link CredentialsSettings} to supply the a username and
 * password.
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 */
@SuppressWarnings("restriction")
public class UsernamePasswordSettings
        implements CredentialsSettings {

    @Migration(LegacyCredentialsMigration.class)
    @Widget(title = "Username/Password", //
            description = "Specifies the username and password to use.")
    LegacyCredentials m_usernamePasswordV2 = new LegacyCredentials(new Credentials());

    static final class LegacyCredentialsMigration implements NodeSettingsMigration<LegacyCredentials> {
        private static final String FLOW_VARIABLE_KEY = "flowVariable";

        static LegacyCredentials loadFromLegacy(final NodeSettingsRO settings) throws InvalidSettingsException {
            final var flowVariableName = settings.getString(FLOW_VARIABLE_KEY);
            if (flowVariableName == null) {
                return new LegacyCredentials(new Credentials());
            }
            return new LegacyCredentials(flowVariableName);
        }

        @Override
        public List<ConfigMigration<LegacyCredentials>> getConfigMigrations() {
            return List.of(ConfigMigration.builder(LegacyCredentialsMigration::loadFromLegacy)
                    .withDeprecatedConfigPath(FLOW_VARIABLE_KEY).build());
        }
     }

    /**
     * If a flow variable has been specified, this method validates that a username
     * and password are present in the flow variable. This method should be used
     * during the configure phase, to reduce logspam if the credentials flow
     * variable is not there yet.
     *
     * @param credsProvider
     *            Used to access the flow variable.
     * @throws InvalidSettingsException
     *             when username or password was not present in the flow variable.
     */
    public void validateOnConfigure(final CredentialsProvider credsProvider) throws InvalidSettingsException {
        if (retrieve(credsProvider).isPresent()) {
            validateLogin(credsProvider, "Username is required");
            validateSecret(credsProvider, "Password is required");
        }
    }

    /**
     * This method validates both presence and validity of a credentials flow
     * variable. This method should be used during the execute phase.
     *
     * @param credsProvider
     *            Used to access the flow variable.
     * @throws InvalidSettingsException
     *             when flow variable was not present or invalid.
     */
    public void validateOnExecute(final CredentialsProvider credsProvider) throws InvalidSettingsException {
        validateLogin(credsProvider, "Username is required");
        validateSecret(credsProvider, "Password is required");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Credentials> retrieve(final CredentialsProvider credsProvider) {
        try {
            return Optional.of(m_usernamePasswordV2.toCredentials(credsProvider));
        } catch (IllegalArgumentException e) { // NOSONAR
            return Optional.empty();
        }
    }
}
