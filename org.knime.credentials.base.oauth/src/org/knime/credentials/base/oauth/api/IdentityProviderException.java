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
 *   2024-01-24 (bjoern): created
 */
package org.knime.credentials.base.oauth.api;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Exception type used by {@link AccessTokenWithScopesAccessor} to indicate that
 * the identity provider returned an error on the OAuth2 token endpoint. Apart
 * from the exception message it contains fields related to the error as
 * returned by the identity provider
 * (<a href="https://datatracker.ietf.org/doc/html/rfc6749#section-5.2">RFC
 * 6749</a>).
 *
 * @author Jannik Löscher, KNIME GmbH, Konstanz, Germany
 * @since 5.8
 */
public class IdentityProviderException extends IOException {

    private static final long serialVersionUID = 0x4563686f564e3c33L;

    /** Human-readable summary of what went wrong. */
    private final String m_errorSummary;

    /**
     * Request/correlation ID to trace the error at the identity provider. May be
     * {@code null}.
     */
    private final String m_idpCorrelationId;

    /** Machine-readable code describing the error type. May be {@code null}. */
    private final String m_idpResponseCode;

    /**
     * Human-readable description with additional information. May be {@code null}.
     */
    private final String m_idpResponseDescription;

    /**
     * Link to resources providing more information about the error. May be null.
     */
    private final String m_idpResonseUri;

    /**
     * Creates a new instance.
     *
     * @param exceptionMessage
     *            The exception message which shall be displayed to the user
     * @param errorSummary
     *            Human-readable summary of what went wrong.
     * @param idpCorrelationId
     *            Request/correlation ID to trace the error at the identity
     *            provider. May be {@code null}.
     * @param idpResponseCode
     *            Machine-readable code describing the error type. May be
     *            {@code null}.
     * @param idpResponseDescription
     *            Human-readable description with additional information. May be
     *            {@code null}.
     * @param idpResonseUri
     *            link to resources providing more information about the error. May
     *            be {@code null}.
     */
    public IdentityProviderException(final String exceptionMessage, //
            final String errorSummary, //
            final String idpCorrelationId, //
            final String idpResponseCode, //
            final String idpResponseDescription, //
            final String idpResonseUri) {

        super(exceptionMessage);
        this.m_errorSummary = Objects.requireNonNull(errorSummary);
        this.m_idpCorrelationId = idpCorrelationId;
        this.m_idpResponseCode = idpResponseCode;
        this.m_idpResponseDescription = idpResponseDescription;
        this.m_idpResonseUri = idpResonseUri;
    }

    /**
     * @return a human-readable summary of what went wrong.
     */
    public String getErrorSummary() {
        return m_errorSummary;
    }

    /**
     * @return an optional request/correlation ID to trace the error at the identity
     *         provider.
     */
    public Optional<String> getIdpCorrelationId() {
        return Optional.ofNullable(m_idpCorrelationId);
    }

    /**
     * @return an optional machine-readable code describing the error type.
     */
    public Optional<String> getIdpResponseCode() {
        return Optional.ofNullable(m_idpResponseCode);
    }

    /**
     * @return an optional human-readable description with additional information.
     */
    public Optional<String> getIdpResponseDescription() {
        return Optional.ofNullable(m_idpResponseDescription);
    }

    /**
     * @return an optional link to resources providing more information about the
     *         error.
     */
    public Optional<String> getIdpResonseUri() {
        return Optional.ofNullable(m_idpResonseUri);
    }
}
