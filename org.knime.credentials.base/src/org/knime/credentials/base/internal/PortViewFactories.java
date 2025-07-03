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
 *   2023-04-11 (Alexander Bondaletov, Redfield SE): created
 */
package org.knime.credentials.base.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.knime.core.webui.data.InitialDataService;
import org.knime.core.webui.data.RpcDataService;
import org.knime.core.webui.node.port.PortSpecViewFactory;
import org.knime.core.webui.node.port.PortView;
import org.knime.core.webui.node.port.PortViewFactory;
import org.knime.core.webui.node.port.PortViewManager;
import org.knime.core.webui.node.port.PortViewManager.PortViewDescriptor;
import org.knime.core.webui.page.Page;
import org.knime.credentials.base.Credential;
import org.knime.credentials.base.CredentialPortObject;
import org.knime.credentials.base.CredentialPortObjectSpec;

/**
 * {@link PortViewFactory} for the {@link CredentialPortObject}.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
@SuppressWarnings("restriction")
public final class PortViewFactories {

    private PortViewFactories() {
    }

    /**
     * Registers the views with the {@link PortViewManager}.
     */
    public static void register() {
        final var portSpecViewFactory = //
                (PortSpecViewFactory<CredentialPortObjectSpec>) PortViewFactories::createPortSpecView;
        final var portViewFactory = (PortViewFactory<CredentialPortObject>) PortViewFactories::createPortView;
        // AP-24150: Accessing `CredentialPortObject.class` does not trigger static
        // initializers, which is important here because we don't want to load
        // `PortType`s in the plugin activator.
        PortViewManager.registerPortViews(CredentialPortObject.class, //
                List.of(new PortViewDescriptor("Credential", portSpecViewFactory), //
                        new PortViewDescriptor("Credential", portViewFactory)), //
                List.of(0), //
                List.of(1));
    }

    private static PortView createPortView(final CredentialPortObject portObject) {
        return new PortView() {
            @Override
            public Page getPage() {
                return Page.create().fromString(() -> createHtmlContent(portObject)).relativePath("index.html");
            }

            @SuppressWarnings("unchecked")
            @Override
            public Optional<InitialDataService<?>> createInitialDataService() {
                return Optional.empty();
            }

            @Override
            public Optional<RpcDataService> createRpcDataService() {
                return Optional.empty();
            }
        };
    }

    private static String createHtmlContent(final CredentialPortObject portObject) {
        final var content = portObject.getCredential(Credential.class) //
                .map(PortViewFactories::renderPortViewData) //
                .orElse("Nothing to display.");
        return createHtmlPage(content);
    }

    private static String createHtmlPage(final String content) {
        final var sb = new StringBuilder();
        sb.append("<html><head><style>\n");
        try (var in = PortViewFactories.class.getClassLoader().getResourceAsStream("table.css")) {
            sb.append(String.join("\n", IOUtils.readLines(in, StandardCharsets.UTF_8)));
        } catch (IOException ex) { // NOSONAR ignore, should always work
        }
        sb.append("</style></head><body>\n");
        sb.append(content);
        sb.append("</body></html>\n");
        return sb.toString();
    }

    private static String renderPortViewData(final Credential cred) {
        final var sb = new StringBuilder();

        for (var section : cred.describe().sections()) {
            sb.append(String.format("<h4>%s</h4>%n", section.title()));

            sb.append("<table>\n");

            final var columns = section.columns();

            // render first row as table header
            if (columns.length >= 1) {
                sb.append("<tr>\n");
                sb.append(Arrays.stream(columns[0])//
                        .map(h -> String.format("<th>%s</th>%n", h))//
                        .collect(Collectors.joining()));
                sb.append("</tr>\n");
            }

            for (var i = 1; i < columns.length; i++) {
                sb.append("<tr>\n");
                sb.append(Arrays.stream(columns[i])//
                        .map(h -> String.format("<td>%s</td>%n", h))//
                        .collect(Collectors.joining()));
                sb.append("</tr>\n");
            }
            sb.append("</table>\n");
        }

        return sb.toString();
    }

    /**
     * @param pos
     *            The port object spec.
     */
    private static PortView createPortSpecView(final CredentialPortObjectSpec pos) {
        return new PortView() {
            @Override
            public Page getPage() {
                final var content = pos.getCredentialType()//
                        .map(type -> String.format("Credential (%s)", type.getName()))//
                        .orElse("Credential");
                return Page.create().fromString(() -> createHtmlPage(content)).relativePath("index.html");
            }

            @SuppressWarnings("unchecked")
            @Override
            public Optional<InitialDataService<?>> createInitialDataService() {
                return Optional.empty();
            }

            @Override
            public Optional<RpcDataService> createRpcDataService() {
                return Optional.empty();
            }
        };
    }
}
