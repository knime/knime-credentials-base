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

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.credentials.base.oauth2.base.Sections.ScopesSection;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.WidgetGroup;
import org.knime.node.parameters.array.ArrayWidget;
import org.knime.node.parameters.array.ArrayWidget.ElementLayout;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.persistence.Persistable;
import org.knime.node.parameters.persistence.Persistor;

/**
 * Implementation of {@link WidgetGroup} to specify a list of scopes.
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 */
@SuppressWarnings("restriction")
public class ScopeSettings implements WidgetGroup, Persistable {

    /**
     * Although a scope is just a string, we have to create a custom class,
     * otherwise the @ArrayWidget does not display correctly.
     */
    static final class Scope implements WidgetGroup {
        @Widget(title = "Scope", description = "")
        String m_scope;

        Scope(final String scope) {
            m_scope = scope;
        }

        Scope() {
        }
    }

    @Widget(title = "Scopes", description = "The list of scopes to request for the access token.")
    @ArrayWidget(elementLayout = ElementLayout.HORIZONTAL_SINGLE_LINE, addButtonText = "Add scope")
    @Layout(ScopesSection.class)
    @Persistor(ScopeArrayPersistor.class)
    Scope[] m_scopes = new Scope[0];

    /**
     * @throws InvalidSettingsException
     *             when no scopes are specified, or one of the specified scopes are
     *             invalid.
     */
    public void validate() throws InvalidSettingsException {
        if (m_scopes == null) {
            return;
        }

        var pos = 1;
        for (final var scope : m_scopes) {
            if (StringUtils.isBlank(scope.m_scope)) {
                throw new InvalidSettingsException("Please remove blank scope at position " + pos);
            }

            pos++;
        }
    }

    /**
     * @return a string that contains all scopes, separated by spaces.
     */
    public String toScopeString() {
        return String.join(" ", Arrays.stream(m_scopes).map(s -> s.m_scope.trim()).toList());
    }

    private static final class ScopeArrayPersistor implements NodeParametersPersistor<Scope[]> {

        static final String CONFIG_KEY = "scopes";

        @Override
        public Scope[] load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return Arrays.stream(settings.getStringArray(CONFIG_KEY))//
                    .map(Scope::new)//
                    .toArray(Scope[]::new);
        }

        @Override
        public void save(final Scope[] obj, final NodeSettingsWO settings) {
            var stringArray = Arrays.stream(obj).map(s -> s.m_scope).toArray(String[]::new);
            settings.addStringArray(CONFIG_KEY, stringArray);
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][] { { CONFIG_KEY } };
        }
    }
}
