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
package org.knime.credentials.base.oauth2.clientcredentials;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.node.parameters.NodeParameters;
import org.knime.credentials.base.oauth.api.scribejava.CustomOAuth2ServiceBuilder;
import org.knime.credentials.base.oauth2.base.ConfidentialAppSettings;
import org.knime.credentials.base.oauth2.base.OAuth2AuthenticatorSettings;
import org.knime.credentials.base.oauth2.base.ScopeSettings;
import org.knime.credentials.base.oauth2.base.Sections.ScopesSection;
import org.knime.node.parameters.Advanced;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.array.ArrayWidget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.HorizontalLayout;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.credentials.base.oauth2.base.TokenEndpointSettings;

import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * The node settings for the OAuth2 Authenticator (Client Credentials) node.
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 */
@SuppressWarnings("restriction")
final class OAuth2AuthenticatorClientCredsSettings implements OAuth2AuthenticatorSettings {

    /**
     * A section for (optional) additional request fields.
     */
    @Section(title = "Additional request fields")
    @Advanced
    @After(ScopesSection.class)
    public interface AdditionalFieldsSection {
    }

    TokenEndpointSettings m_service = new TokenEndpointSettings();

    ConfidentialAppSettings m_app = new ConfidentialAppSettings();

    ScopeSettings m_scopes = new ScopeSettings();

    static final class AdditionalRequestField implements NodeParameters {
        @HorizontalLayout
        interface NameAndValue {
        }

        @Widget(title = "Name", description = "Name of the additional request body field.")
        @Layout(NameAndValue.class)
        String m_name;

        @Widget(title = "Value", description = "Value of the additional request body field.")
        @Layout(NameAndValue.class)
        String m_value;

        @Override
        public void validate() throws InvalidSettingsException {
            if (StringUtils.isBlank(m_name) || StringUtils.isBlank(m_value)) {
                throw new InvalidSettingsException("Please specify name and value for each additional request field");
            }
        }
    }

    @Widget(title = "Additional request fields", //
            description = "Allows to add request body fields (key and value) to the token endpoint request.", //
            advanced = true)
    @ArrayWidget(elementLayout = ArrayWidget.ElementLayout.HORIZONTAL_SINGLE_LINE, addButtonText = "Add request field")
    @Layout(AdditionalFieldsSection.class)
    AdditionalRequestField[] m_additionalRequestFields = new AdditionalRequestField[0];

    @Override
    public OAuth20Service createService(final CredentialsProvider credsProvider) {
        final var api = m_service.createApi();

        var builder = new CustomOAuth2ServiceBuilder(m_app.login(credsProvider))//
                .apiSecret(m_app.secret(credsProvider));

        Arrays.stream(m_additionalRequestFields)//
                .forEach(field -> builder.additionalRequestBodyField(field.m_name, field.m_value));

        return builder.build(api);
    }
}
