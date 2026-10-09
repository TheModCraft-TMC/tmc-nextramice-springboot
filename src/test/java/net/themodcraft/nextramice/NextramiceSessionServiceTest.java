package net.themodcraft.nextramice;

import net.themodcraft.nextramice.bff.NextramiceSessionService;
import net.themodcraft.nextramice.bff.model.NextramiceSession;
import net.themodcraft.nextramice.factories.ClaimProxyFactory;
import net.themodcraft.nextramice.interfaces.GeneratedAdditionalClaims;
import net.themodcraft.nextramice.local.NextramiceLocalUser;
import net.themodcraft.nextramice.configuration.NextramiceProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class NextramiceSessionServiceTest {

    private final NextramiceSessionService sessions = new NextramiceSessionService();

    @Test
    void returnsAnonymousSessionWithoutAuthentication() {
        assertThat(sessions.create(null).authenticated()).isFalse();
    }

    @Test
    void mapsBearerAuthenticationAndAdditionalClaims() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("external-123")
                .issuer("https://issuer.example.test")
                .expiresAt(Instant.parse("2030-01-01T00:00:00Z"))
                .claim("email", "mouse@example.test")
                .claim("name", "Mouse User")
                .build();
        GeneratedAdditionalClaims claims = ClaimProxyFactory.create(
                GeneratedAdditionalClaims.class,
                Map.of("tenantId", "mice")
        );
        NextramiceJwtAuthenticationToken authentication = new NextramiceJwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                claims,
                "mouse@example.test"
        );

        NextramiceSession session = sessions.create(authentication);

        assertThat(session.authenticated()).isTrue();
        assertThat(session.principal().id())
                .isEqualTo("https://issuer.example.test|external-123");
        assertThat(session.principal().displayName()).isEqualTo("Mouse User");
        assertThat(session.authorities()).containsExactly("ROLE_USER");
        assertThat(session.claims()).containsEntry("tenantId", "mice");
        assertThat(session.loginMethod()).isEqualTo("bearer");
    }

    @Test
    void mapsLocalAuthenticationToSameSessionContract() {
        NextramiceLocalUser user = NextramiceLocalUser.enabled(
                "local-7",
                "mouse@example.test",
                "Mouse User",
                "{noop}password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        user,
                        null,
                        user.getAuthorities()
                );

        NextramiceSession session = sessions.create(authentication);

        assertThat(session.principal().id()).isEqualTo("local-7");
        assertThat(session.principal().username()).isEqualTo("mouse@example.test");
        assertThat(session.loginMethod()).isEqualTo("local");
        assertThat(session.provider()).isEqualTo("local");
    }

    @Test
    void exposesOnlyConfiguredClaimsInProductionConfiguration() {
        NextramiceProperties properties = new NextramiceProperties();
        properties.getBff().getExposedClaims().add("tenantId");
        NextramiceSessionService configuredSessions = new NextramiceSessionService(properties);
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("external-123")
                .claim("email", "mouse@example.test")
                .build();
        GeneratedAdditionalClaims claims = ClaimProxyFactory.create(
                GeneratedAdditionalClaims.class,
                Map.of("tenantId", "mice", "internalSecret", "hidden")
        );
        NextramiceJwtAuthenticationToken authentication = new NextramiceJwtAuthenticationToken(
                jwt,
                List.of(),
                claims,
                "mouse@example.test"
        );

        assertThat(configuredSessions.create(authentication).claims())
                .containsExactly(Map.entry("tenantId", "mice"));
    }

    @Test
    void linksExternalAccountsOnlyByIssuerAndSubject() {
        NextramiceProperties properties = new NextramiceProperties();
        NextramiceSessionService linkedSessions = new NextramiceSessionService(
                properties,
                (issuer, subject) -> issuer.equals("https://issuer.example.test")
                        && subject.equals("external-123")
                        ? Optional.of("local-42")
                        : Optional.empty()
        );
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("external-123")
                .issuer("https://issuer.example.test")
                .claim("email", "mouse@example.test")
                .build();
        GeneratedAdditionalClaims noClaims = ClaimProxyFactory.create(
                GeneratedAdditionalClaims.class,
                Map.of()
        );
        NextramiceJwtAuthenticationToken authentication = new NextramiceJwtAuthenticationToken(
                jwt,
                List.of(),
                noClaims,
                "mouse@example.test"
        );

        assertThat(linkedSessions.create(authentication).principal().id()).isEqualTo("local-42");
    }
}
