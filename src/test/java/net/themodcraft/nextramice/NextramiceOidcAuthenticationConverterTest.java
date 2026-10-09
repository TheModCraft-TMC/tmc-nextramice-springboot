package net.themodcraft.nextramice;

import net.themodcraft.nextramice.providers.AuthenticationClaimProvider;
import net.themodcraft.nextramice.providers.AuthorityProvider;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class NextramiceOidcAuthenticationConverterTest {

    @Test
    void createsTokenWithAuthoritiesAndAdditionalClaims() {
        AtomicReference<String> authorityLookup = new AtomicReference<>();
        AuthorityProvider authorityProvider = principalName -> {
            authorityLookup.set(principalName);
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        };
        List<AuthenticationClaimProvider> claimProviders = List.of(
                jwt -> Map.of("tenant", "first"),
                jwt -> Map.of("tenant", "replacement", "locale", "de-CH")
        );
        NextramiceOidcAuthenticationConverter converter =
                new NextramiceOidcAuthenticationConverter(authorityProvider, claimProviders);

        NextramiceJwtAuthenticationToken token =
                (NextramiceJwtAuthenticationToken) converter.convert(jwt(Map.of("email", "mouse@example.test")));

        assertThat(token).isNotNull();
        assertThat(token.getName()).isEqualTo("mouse@example.test");
        assertThat(token.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        assertThat(token.getClaims().toString()).contains("tenant=replacement", "locale=de-CH");
        assertThat(authorityLookup).hasValue("mouse@example.test");
    }

    @Test
    void usesFallbackPrincipalClaimConsistently() {
        NextramiceOidcAuthenticationConverter converter =
                new NextramiceOidcAuthenticationConverter(principal -> List.of(), List.of());

        NextramiceJwtAuthenticationToken token =
                (NextramiceJwtAuthenticationToken) converter.convert(jwt(Map.of("upn", "fallback@example.test")));

        assertThat(token).isNotNull();
        assertThat(token.getName()).isEqualTo("fallback@example.test");
    }

    @Test
    void rejectsTokenWithoutSupportedPrincipalClaim() {
        NextramiceOidcAuthenticationConverter converter =
                new NextramiceOidcAuthenticationConverter(principal -> List.of(), List.of());

        assertThat(converter.convert(jwt(Map.of("sub", "123")))).isNull();
    }

    private static Jwt jwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claims(values -> values.putAll(claims))
                .build();
    }
}
