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
 *   2023-04-13 (Alexander Bondaletov, Redfield SE): created
 */
package org.knime.credentials.base.oauth2.password;

import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.webui.node.dialog.defaultdialog.layout.After;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Before;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Section;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ValueSwitchWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.ValueReference;
import org.knime.credentials.base.node.UsernamePasswordSettings;
import org.knime.credentials.base.oauth2.base.ConfidentialAppSettings;
import org.knime.credentials.base.oauth2.base.OAuth2AuthenticatorSettings;
import org.knime.credentials.base.oauth2.base.PublicAppSettings;
import org.knime.credentials.base.oauth2.base.ScopeSettings;
import org.knime.credentials.base.oauth2.base.Sections.AppSection;
import org.knime.credentials.base.oauth2.base.Sections.ScopesSection;
import org.knime.credentials.base.oauth2.base.TokenEndpointSettings;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * Node settings for the OAuth2 Authenticator (Password) node.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
@SuppressWarnings("restriction")
final class OAuth2AuthenticatorPasswordSettings implements OAuth2AuthenticatorSettings {

    @Section(title = "Username and Password")
    @After(AppSection.class)
    @Before(ScopesSection.class)
    interface UsernamePasswordSection {
    }

    TokenEndpointSettings m_service = new TokenEndpointSettings();

    @Widget(title = "Client/App type", description = CLIENT_TYPE_DESCRIPTION)
    @ValueSwitchWidget
    @Layout(AppSection.TypeChooser.class)
    @ValueReference(AppTypeRef.class)
    AppType m_appType = AppType.PUBLIC;

    PublicAppSettings m_publicApp = new PublicAppSettings();

    ConfidentialAppSettings m_confidentialApp = new ConfidentialAppSettings();

    @Layout(UsernamePasswordSection.class)
    UsernamePasswordSettings m_usernamePassword = new UsernamePasswordSettings();

    ScopeSettings m_scopes = new ScopeSettings();

    @Override
    public OAuth20Service createService(final CredentialsProvider credsProvider) {
        final var api = m_service.createApi();

        if (m_appType == AppType.PUBLIC) {
            return new ServiceBuilder(m_publicApp.m_appId).build(api);
        } else {
            return new ServiceBuilder(m_confidentialApp.login(credsProvider))//
                    .apiSecret(m_confidentialApp.secret(credsProvider))//
                    .build(api);
        }
    }
}
