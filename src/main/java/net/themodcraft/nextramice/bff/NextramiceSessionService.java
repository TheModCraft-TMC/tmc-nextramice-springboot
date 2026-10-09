package net.themodcraft.nextramice.bff;

import net.themodcraft.nextramice.NextramiceJwtAuthenticationToken;
import net.themodcraft.nextramice.bff.model.NextramicePrincipal;
import net.themodcraft.nextramice.bff.model.NextramiceSession;
import net.themodcraft.nextramice.local.NextramiceLocalUser;
import net.themodcraft.nextramice.configuration.NextramiceProperties;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.net.URL;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class NextramiceSessionService {

    private final Set<String> exposedClaims;
    private final NextramiceAccountLinkService accountLinks;

    public NextramiceSessionService() {
        this.exposedClaims = null;
        this.accountLinks = null;
    }

    public NextramiceSessionService(NextramiceProperties properties) {
        this(properties, null);
    }

    public NextramiceSessionService(Set<String> exposedClaims) {
        this.exposedClaims = Set.copyOf(exposedClaims);
        this.accountLinks = null;
    }

    public NextramiceSessionService(
            NextramiceProperties properties,
            NextramiceAccountLinkService accountLinks
    ) {
        this.exposedClaims = Set.copyOf(properties.getBff().getExposedClaims());
        this.accountLinks = accountLinks;
    }

    public NextramiceSession create(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return NextramiceSession.anonymous();
        }

        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();

        if (authentication instanceof NextramiceJwtAuthenticationToken token) {
            String subject = token.getToken().getSubject();
            URL issuer = token.getToken().getIssuer();
            String id = externalId(issuer == null ? null : issuer.toString(), subject, token.getName());
            String displayName = token.getToken().getClaimAsString("name");
            Map<String, Object> claims = allowlisted(token.getClaims().asMap());
            return new NextramiceSession(
                    true,
                    new NextramicePrincipal(id, token.getName(), displayName),
                    authorities,
                    claims,
                    token.getToken().getExpiresAt(),
                    "bearer",
                    issuer == null ? null : issuer.toString()
            );
        }

        if (authentication instanceof OAuth2AuthenticationToken token) {
            OAuth2User user = token.getPrincipal();
            String issuer = stringAttribute(user, "iss");
            String subject = stringAttribute(user, "sub");
            String username = firstNonBlank(
                    stringAttribute(user, "email"),
                    stringAttribute(user, "preferred_username"),
                    user.getName()
            );
            return new NextramiceSession(
                    true,
                    new NextramicePrincipal(
                            externalId(issuer, subject, user.getName()),
                            username,
                            stringAttribute(user, "name")
                    ),
                    authorities,
                    allowlisted(user.getAttributes()),
                    null,
                    "oidc",
                    token.getAuthorizedClientRegistrationId()
            );
        }

        if (authentication.getPrincipal() instanceof NextramiceLocalUser user) {
            return new NextramiceSession(
                    true,
                    new NextramicePrincipal(user.getId(), user.getUsername(), user.getDisplayName()),
                    authorities,
                    Map.of(),
                    null,
                    "local",
                    "local"
            );
        }

        return new NextramiceSession(
                true,
                new NextramicePrincipal(authentication.getName(), authentication.getName(), null),
                authorities,
                Map.of(),
                null,
                "custom",
                null
        );
    }

    private static String stringAttribute(OAuth2User user, String name) {
        Object value = user.getAttributes().get(name);
        return value instanceof String text && !text.isBlank() ? text : null;
    }

    private String externalId(String issuer, String subject, String fallback) {
        if (issuer != null && !issuer.isBlank() && subject != null && !subject.isBlank()) {
            if (accountLinks != null) {
                String linked = accountLinks.findLocalUserId(issuer, subject).orElse(null);
                if (linked != null && !linked.isBlank()) {
                    return linked;
                }
            }
            return issuer + "|" + subject;
        }
        return subject != null && !subject.isBlank() ? subject : fallback;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private Map<String, Object> allowlisted(Map<String, Object> available) {
        Map<String, Object> result = new LinkedHashMap<>();
        Set<String> names = exposedClaims == null ? available.keySet() : exposedClaims;
        for (String name : names) {
            Object value = available.get(name);
            if (value != null) {
                result.put(name, value);
            }
        }
        return Map.copyOf(result);
    }
}
