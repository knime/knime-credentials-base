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
 *   2023-05-25 (Alexander Bondaletov, Redfield SE): created
 */
package org.knime.credentials.base.oauth2.authcode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import com.github.scribejava.apis.AWeberApi;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.nimbusds.jose.util.StandardCharset;

/**
 * The class generates enum containing all of the supported OAuth 2.0 services
 * from scrive-java library. Is is done by scanning the package containing api
 * classes.
 *
 * In order to execute this class scribe-java library has to be present in the
 * classpath.
 *
 * It is possible to exclude some classes from being added. It is also possible
 * to provide custom title for a particular class.
 *
 * @author Alexander Bondaletov, Redfield SE
 */
class GenerateStandardServices {

    private static final Class<?> API_CLASS = AWeberApi.class;
    private static final String API_PACKAGE = API_CLASS.getPackageName();
    private static final String API_PACKAGE_PATH = API_PACKAGE.replace('.', '/');

    private static final String ENUM_NAME = "StandardService";

    private static final Set<String> EXCLUDED_APIS = Set.of("FrappeApi", "BoxApi20");

    private static final Map<String, String> CUSTOM_TITLES = Map.of( //
            "HiOrgServerApi20", "HiOrg-Server", //
            "LiveApi", "Microsoft Live", //
            "MicrosoftAzureActiveDirectory20Api", "Microsoft Azure AD v2.0", //
            "MicrosoftAzureActiveDirectoryApi", "Microsoft Azure AD v1.0", //
            "TheThingsNetworkV1StagingApi", "The Things Network (v1-staging)", //
            "TheThingsNetworkV2PreviewApi", "The Things Network (v2-preview)");

    /**
     * main method.
     *
     * @param args
     * @throws ClassNotFoundException
     * @throws Throwable
     */
    public static void main(final String[] args) throws Throwable {
        var classes = getClasses();
        var imports = getImports(classes);
        var enums = getEnums(classes);

        var currentPath = GenerateStandardServices.class.getPackageName().replace('.', '/');

        var template = Files.readString(Paths.get(GenerateStandardServices.class.getClassLoader()
                .getResource(currentPath + "/StandardService.java.template").toURI()));

        var source = template //
                .replace("<imports>", imports) //
                .replace("<enums>", enums) //
                .replace("<enum-name>", ENUM_NAME);

        final var path = Paths.get("src", currentPath, ENUM_NAME + ".java");
        try (final var writer = Files.newBufferedWriter(path, StandardCharset.UTF_8)) {
            writer.write(source);
        }
    }

    private static final String getImports(final List<String> classes) {
        final var sb = new StringBuilder();
        for (String c : classes) {
            sb.append("import ").append(API_PACKAGE).append(".").append(c).append(";\n");
        }
        return sb.toString();
    }

    private static final String getEnums(final List<String> classes) {
        final var sb = new StringBuilder();
        Iterator<String> it = classes.iterator();
        while (it.hasNext()) {
            String className = it.next();
            sb.append("    /**\n     * ").append(className).append(" service.\n     */\n");
            sb.append("    @Label(\"").append(toTitle(className)).append("\")\n");
            sb.append("    ").append(className.toUpperCase(Locale.ENGLISH)).append("(").append(className)
                    .append(".instance())");

            if (it.hasNext()) {
                sb.append(",\n");
            } else {
                sb.append(";\n");
            }
        }
        return sb.toString();
    }

    private static String toTitle(final String className) {
        if (CUSTOM_TITLES.containsKey(className)) {
            return CUSTOM_TITLES.get(className);
        } else {
            return className.replace("Api", "") //
                    .replace("API", "") //
                    .replace("20", "") //
                    .replace("2", "");
        }
    }

    private static final Pattern CLASS_PATTERN = Pattern.compile(API_PACKAGE_PATH + "/([^/$]+).class");

    private static List<String> getClasses() throws IOException, ClassNotFoundException {
        String filePath = API_CLASS.getClassLoader().getResource(API_CLASS.getName().replace('.', '/') + ".class")
                .getFile();
        // extract '<file-path>' from 'file:<file-path>!<inner-path>' string
        filePath = filePath.substring(5, filePath.indexOf('!'));

        try (final var jarFile = new JarFile(filePath)) {
            return findDefaultApi20Classes(jarFile);
        }
    }

    private static List<String> findDefaultApi20Classes(final JarFile f) throws ClassNotFoundException {
        final var classes = new ArrayList<String>();

        final var entries = f.entries();
        while (entries.hasMoreElements()) {
            final var m = CLASS_PATTERN.matcher(entries.nextElement().getName());
            if (!m.matches()) {
                continue;
            }

            final var className = m.group(1);
            if (DefaultApi20.class.isAssignableFrom(Class.forName(API_PACKAGE + "." + className))
                    && !EXCLUDED_APIS.contains(className)) {
                classes.add(m.group(1));
            }
        }

        return classes;
    }
}
