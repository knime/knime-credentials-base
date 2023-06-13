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
package org.knime.credentials.base.oauth2.base;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.port.PortObject;
import org.knime.core.webui.node.impl.WebUINodeConfiguration;
import org.knime.credentials.base.Credential;
import org.knime.credentials.base.node.AuthenticatorNodeModel;
import org.knime.credentials.base.oauth.api.scribejava.CredentialFactory;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * Node model base class that implements common behavior for all OAuth2
 * Authenticator nodes.
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 * @param <T>
 *            The concrete settings class to use.
 */
@SuppressWarnings("restriction")
public abstract class OAuth2AuthenticatorNodeModel<T extends OAuth2AuthenticatorSettings>
        extends AuthenticatorNodeModel<T> {

    /**
     * Constructor.
     *
     * @param configuration
     *            The {@link WebUINodeConfiguration} to use.
     * @param settingsClass
     *            The concrete settings class to use.
     */
    protected OAuth2AuthenticatorNodeModel(final WebUINodeConfiguration configuration, final Class<T> settingsClass) {
        super(configuration, settingsClass);
    }

    @Override
    protected Credential createCredential(final PortObject[] inObjects, final ExecutionContext exec, final T settings)
            throws Exception {

        try (var service = settings.createService()) {
            var scribeJavaToken = fetchOAuth2AccessToken(settings, service);
            return CredentialFactory.fromScribeToken(scribeJavaToken, settings::createService);
        }
    }

    /**
     * Subclasses must implement this method to fetch a {@link OAuth2AccessToken}
     * using the scribejava library.
     *
     * @param settings
     * @param service
     * @return the scribejava {@link OAuth2AccessToken}
     * @throws Exception
     */
    protected abstract OAuth2AccessToken fetchOAuth2AccessToken(final T settings, final OAuth20Service service)
            throws Exception;
}
