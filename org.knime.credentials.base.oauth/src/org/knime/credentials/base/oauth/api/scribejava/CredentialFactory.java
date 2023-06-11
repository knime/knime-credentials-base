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
 *   2023-06-07 (bjoern): created
 */
package org.knime.credentials.base.oauth.api.scribejava;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.knime.credentials.base.Credential;
import org.knime.credentials.base.oauth.api.AccessTokenCredential;
import org.knime.credentials.base.oauth.api.JWTCredential;

import com.github.scribejava.apis.openid.OpenIdOAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

/**
 * Factory class to create a {@link Credential} from a scribejava access token.
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 */
public class CredentialFactory {

    /**
     * Creates a new {@link Credential} from the given scribejava access token.
     *
     * @param scribeToken
     *            The scribejava access token.
     * @param serviceSupplier
     *            A supplier the creates a new (open) {@link OAuth20Service} for
     *            token refresh.
     * @return a newly created {@link Credential}
     */
    public static Credential fromScribeToken(final OAuth2AccessToken scribeToken, final Supplier<OAuth20Service> serviceSupplier) {
        var accessToken = scribeToken.getAccessToken();
        var idToken = scribeToken instanceof OpenIdOAuth2AccessToken
                ? ((OpenIdOAuth2AccessToken) scribeToken).getOpenIdToken()
                : null;
        var refreshToken = scribeToken.getRefreshToken();
        var expiresAfter = Optional.ofNullable(scribeToken.getExpiresIn())//
                .map(secs -> Instant.now().plusSeconds(secs))//
                .orElse(null);
        var tokenType = scribeToken.getTokenType();

        try {
            return new JWTCredential(accessToken, //
                    tokenType, //
                    expiresAfter,
                    idToken, //
                    refreshToken,//
                    createTokenRefresher(serviceSupplier));
        } catch (ParseException ignored) {
            return new AccessTokenCredential(accessToken, //
                    refreshToken, //
                    expiresAfter, //
                    tokenType, //
                    createTokenRefresher(serviceSupplier));
        }
    }

    private static <T extends Credential> Function<String, T> createTokenRefresher(
            final Supplier<OAuth20Service> serviceSupplier) {

        return refreshToken -> {
            try (var service = serviceSupplier.get()) {
                var scribeToken = service.refreshAccessToken(refreshToken);
                return (T) fromScribeToken(scribeToken, serviceSupplier);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e.getCause());
            }
        };
    }
}
