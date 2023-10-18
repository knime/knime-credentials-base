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
package org.knime.credentials.base.oauth2.base;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;
import org.knime.core.webui.node.dialog.defaultdialog.layout.LayoutGroup;
import org.knime.core.webui.node.dialog.defaultdialog.rule.Effect;
import org.knime.core.webui.node.dialog.defaultdialog.rule.Effect.EffectType;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Label;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ValueSwitchWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.credentials.base.oauth.api.scribejava.CustomApi20;
import org.knime.credentials.base.oauth2.base.OAuth2AuthenticatorSettings.IsStandardService;
import org.knime.credentials.base.oauth2.base.Sections.ServiceSection;

import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
import com.github.scribejava.core.oauth2.clientauthentication.HttpBasicAuthenticationScheme;
import com.github.scribejava.core.oauth2.clientauthentication.RequestBodyAuthenticationScheme;

/**
 * Implementation of {@link DefaultNodeSettings} to specify configuration for an
 * OAuth2 token endpoint.
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 */
@SuppressWarnings("restriction")
@Effect(signals = IsStandardService.class, type = EffectType.HIDE, ignoreOnMissingSignals = true)
public class TokenEndpointSettings implements DefaultNodeSettings, LayoutGroup {

    /**
     * The URL of the token endpoint.
     */
    @Widget(title = "Token endpoint URL", description = "The token endpoint URL of the OAuth2 service.")
    @Layout(ServiceSection.Custom.Middle.class)
    public String m_tokenUrl;

    /**
     * The HTTP request method to use against the token endpoint.
     */
    @Widget(title = "Token endpoint request method", //
            description = "HTTP method to use when requesting the access token from the token endpoint.", //
            advanced = true)
    @Layout(ServiceSection.Custom.Bottom.class)
    @ValueSwitchWidget
    public HttpRequestMethod m_tokenRequestMethod = HttpRequestMethod.POST;

    /**
     * How to provide app ID and secret to the token endpoint.
     */
    @Widget(title = "Client/App authentication mechanism", //
            description = """
                    How to transfer Client/App ID and secret to the service endpoints. HTTP Basic Auth is the
                    most common mechanism, but some services expect these values to be part of the form-encoded
                    request body.
                        """, //
            advanced = true)
    @Layout(ServiceSection.Custom.Bottom.class)
    @ValueSwitchWidget
    public ClientAuthenticationType m_clientAuthMechanism = ClientAuthenticationType.HTTP_BASIC_AUTH;

    /**
     * Enum for choices on how to provide app ID and secret to the token endpoint.
     */
    public enum ClientAuthenticationType {
        /**
         * Use HTTP Basic Auth.
         */
        @Label("HTTP Basic Auth")
        HTTP_BASIC_AUTH,

        /**
         * Use form-encoded request body fields.
         */
        @Label("Request Body")
        REQUEST_BODY;

        /**
         * @return the corresponding scribejava {@link ClientAuthentication} to use.
         */
        public ClientAuthentication toScribeClientAuthentication() {
            if (this == ClientAuthenticationType.HTTP_BASIC_AUTH) {
                return HttpBasicAuthenticationScheme.instance();
            } else {
                return RequestBodyAuthenticationScheme.instance();
            }
        }
    }

    /**
     * Enum with HTTP request method choices to use against the token endpoint.
     */
    public enum HttpRequestMethod {
        /**
         * Use HTTP POST.
         */
        @Label("POST")
        POST,

        /**
         * Use HTTP GET.
         */
        @Label("GET")
        GET;

        /**
         * @return the corresponding scribejava {@link Verb} to use.
         */
        public Verb toScribeVerb() {
            return Verb.valueOf(toString());
        }
    }

    /**
     * @throws InvalidSettingsException
     *             when one of the settings was invalid.
     */
    public void validate() throws InvalidSettingsException {
        if (StringUtils.isEmpty(m_tokenUrl)) {
            throw new InvalidSettingsException("Please specify the token endpoint URL to use");
        }

        if (m_tokenRequestMethod == null) {
            throw new InvalidSettingsException("Please specify the token endpoint request method to use");
        }

        if (m_clientAuthMechanism == null) {
            throw new InvalidSettingsException("Please specify the client/app authentication mechanism to use");
        }

    }

    /**
     * @return a scribejava {@link CustomApi20} configured from the settings here.
     */
    public CustomApi20 createApi() {
        return new CustomApi20(m_tokenUrl, //
                "", //
                m_tokenRequestMethod.toScribeVerb(), //
                m_clientAuthMechanism.toScribeClientAuthentication());
    }
}
