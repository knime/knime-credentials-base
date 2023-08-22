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

import org.knime.core.webui.node.dialog.defaultdialog.layout.After;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Before;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Section;

/**
 * Specifies Web UI sections for the family of OAuth2 Authenticator nodes. Not
 * every node has to use all the sections.
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 */
@SuppressWarnings("restriction")
public class Sections {

    /**
     * The section for OAuth2 service configuration.
     */
    @Section(title = "OAuth2 Service")
    public interface ServiceSection {

        /**
         * Subsection to place an optional service type chooser.
         */
        @Before(Standard.class)
        @Before(Custom.class)
        interface TypeChooser {
        }

        /**
         * Subsection to place an optional standard service chooser.
         */
        interface Standard {
        }

        /**
         * Subsection for custom service configuration.
         */
        interface Custom {
            interface Top {
            }

            @After(Top.class)
            interface Middle {
            }

            @After(Middle.class)
            interface Bottom {
            }
        }
    }

    /**
     * The section for OAuth2 application configuration.
     */
    @Section(title = "Client/Application")
    @After(ServiceSection.class)
    public interface AppSection {
        /**
         * Subsection to place an optional app type chooser.
         */
        @Before(Public.class)
        @Before(Confidential.class)
        interface TypeChooser {
        }

        /**
         * Subsection to place public app configuration.
         */
        interface Public {
        }

        /**
         * Subsection to place confidential app configuration.
         */
        interface Confidential {
        }

        /**
         * Subsection to place additional app configuration which is displayed at the
         * bottom.
         */
        @After(Public.class)
        @After(Confidential.class)
        interface Bottom {
        }
    }

    /**
     * The section for OAuth2 scopes.
     */
    @Section(title = "Scopes")
    @After(AppSection.class)
    public interface ScopesSection {
    }

    /**
     * The section for OAuth2 scopes.
     */
    @Section
    @After(ScopesSection.class)
    public interface Footer {
    }
}
