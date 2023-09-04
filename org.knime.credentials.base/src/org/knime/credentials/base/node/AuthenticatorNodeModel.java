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
 *   2023-06-13 (bjoern): created
 */
package org.knime.credentials.base.node;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.impl.WebUINodeModel;
import org.knime.credentials.base.Credential;
import org.knime.credentials.base.CredentialCache;
import org.knime.credentials.base.CredentialPortObject;
import org.knime.credentials.base.CredentialPortObjectSpec;
import org.knime.credentials.base.CredentialType;

/**
 * Base class that should be used by all Authenticator nodes that output a
 * {@link CredentialPortObject}. This parent class provides common behavior to
 * all Authenticator nodes:
 * <ul>
 * <li>Register the {@link Credential} in an in-memory cache, so that it never
 * gets stored with the port object.</li>
 * <li>Clear the {@link Credential} from the in-memory cache, whenever the node
 * is reset or the workflow is disposed.
 * </ul>
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 * @param <T>
 *            The concrete settings class to use.
 */
@SuppressWarnings("restriction")
public abstract class AuthenticatorNodeModel<T extends DefaultNodeSettings> extends WebUINodeModel<T> {

    private UUID m_credentialCacheKey;

    /**
     * Constructor.
     *
     * @param configuration
     *            The {@link WebUINodeConfiguration} to use.
     * @param settingsClass
     *            The concrete settings class to use.
     */
    protected AuthenticatorNodeModel(final WebUINodeConfiguration configuration, final Class<T> settingsClass) {
        super(configuration, settingsClass);
    }

    /**
     * Default implementation that validates th@Override e settings and creates a
     * {@link CredentialPortObjectSpec} with a null {@link CredentialType}.
     * Subclasses can choose to override this method when they know the resulting
     * {@link CredentialType} already during the configure() phase.
     */
    @Override
    protected final PortObjectSpec[] configure(final PortObjectSpec[] inSpecs, final T modelSettings)
            throws InvalidSettingsException {

        m_credentialCacheKey = null;
        validateOnConfigure(inSpecs, modelSettings);
        return new PortObjectSpec[] { createSpecInConfigure(inSpecs, modelSettings) };
    }

    /**
     * Called during configure() to create the port object spec with. The default
     * implementation creates a {@link CredentialPortObjectSpec} with a null
     * {@link CredentialType}. Subclasses can choose to override this method when
     * they know the resulting {@link CredentialType} already during the configure()
     * phase.
     *
     * @param inSpecs
     *            the input {@link PortObjectSpec PortObjectSpecs}
     * @param modelSettings
     *            the current model settings
     * @return a newly created {@link CredentialPortObjectSpec}.
     */
    @SuppressWarnings("static-method")
    protected CredentialPortObjectSpec createSpecInConfigure(final PortObjectSpec[] inSpecs,
            final T modelSettings) {
        return new CredentialPortObjectSpec();
    }

    /**
     * Invoked during configure() to validate the given settings.
     *
     * @param inSpecs
     *            the input {@link PortObjectSpec PortObjectSpecs}
     * @param settings
     *            The settings to validate.
     * @throws InvalidSettingsException
     *             if any of the settings are invalid.
     */
    protected abstract void validateOnConfigure(PortObjectSpec[] inSpecs, final T settings)
            throws InvalidSettingsException;

    /**
     * Default execute() implementation that invokes
     * {@link #createCredential(PortObject[], ExecutionContext, DefaultNodeSettings)}
     * to create a {@link Credential}.
     */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec, final T settings)
            throws Exception {

        validateOnExecute(inObjects, settings);
        var credential = createCredential(inObjects, exec, settings);
        m_credentialCacheKey = CredentialCache.store(credential);
        return new PortObject[] {
                new CredentialPortObject(new CredentialPortObjectSpec(credential.getType(), m_credentialCacheKey)) };
    }

    /**
     * Invoked during execute() to validate the given settings. Subclasses can
     * override this method to add more validation logic during execute().
     *
     * @param inObjects
     *            the input {@link PortObject PortObjects}
     * @param settings
     *            The settings to validate.
     * @throws InvalidSettingsException
     *             if any of the settings are invalid.
     */
    protected void validateOnExecute(final PortObject[] inObjects, final T settings) throws InvalidSettingsException {
    }

    /**
     * Subclass must implement this method to create a new {@link Credential} during
     * execute().
     *
     * @param inObjects
     *            the input {@link PortObject PortObjects}
     * @param exec
     *            the current {@link ExecutionContext}
     * @param settings
     *            the current model settings
     * @return a newly created {@link Credential}.
     * @throws Exception
     *             if something went wrong while creating the credential.
     */
    protected abstract Credential createCredential(PortObject[] inObjects, ExecutionContext exec, T settings)
            throws Exception; // NOSONAR this is on purpose

    @Override
    protected final void onDispose() {
        onReset();
        onDisposeInternal();
    }

    /**
     * Subclasses can override this method to do additional cleanup during
     * {@link #onDispose()}.
     */
    protected void onDisposeInternal() {
        // do nothing
    }

    @Override
    protected void onReset() {
        if (m_credentialCacheKey != null) {
            CredentialCache.delete(m_credentialCacheKey);
            m_credentialCacheKey = null;
        }
    }

    @Override
    protected void onLoadInternals(final File nodeInternDir, final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {

        setWarningMessage("Credential not available anymore. Please re-execute this node.");
    }
}
