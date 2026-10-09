# Authorities and additional claims

## Supply application authorities

Without an application bean, Nextramice authenticates a valid principal with an
empty authority list. Define `AuthorityProvider` to load roles or permissions:

```java
import net.themodcraft.nextramice.providers.AuthorityProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Bean
AuthorityProvider applicationAuthorities(UserRepository users) {
    return principalName -> users.findRoles(principalName).stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .toList();
}
```

This provider is invoked during authentication. Keep the lookup bounded and
predictable. If it calls a database or remote service, define the desired
timeout, outage, and caching behavior in the consuming application.

Use the resulting authorities normally:

```java
@PreAuthorize("hasRole('ADMIN')")
void adminOperation() {
}
```

Method security also requires the consuming application to enable it, for
example with `@EnableMethodSecurity`.

## Add derived claims

An `AuthenticationClaimProvider` receives the validated Spring `Jwt` and returns
application-specific values:

```java
import net.themodcraft.nextramice.providers.AuthenticationClaimProvider;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@Bean
AuthenticationClaimProvider tenantClaims(TenantDirectory tenants) {
    return jwt -> {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", tenants.resolveTenant(jwt.getSubject()));
        claims.put("displayName", jwt.getClaimAsString("name"));
        return claims;
    };
}
```

Multiple providers can contribute claims. Give them explicit Spring ordering if
duplicate keys are possible.

## Read additional claims

```java
@GetMapping("/profile")
Profile profile(NextramiceJwtAuthenticationToken authentication) {
    var claims = authentication.getClaims();
    return new Profile(authentication.getName(), claims.getDisplayName());
}
```

The available getter declarations come from `GeneratedAdditionalClaims`. See
[typed claim views](typed-claim-views.md) before relying on generated methods.

## Decide what belongs in a claim

Good additional claims are small, request-relevant, and safe to retain for the
lifetime of the authentication object—for example a tenant ID, display setting,
or stable application user ID.

Avoid placing these values in the claim view:

- passwords, password hashes, API keys, or refresh tokens;
- large domain objects or persistence entities;
- values that require immediate revocation unless every request revalidates
  them;
- unrestricted copies of provider claims.

Authorities should contain authorization facts used by Spring Security. Profile
or presentation data should remain claims. Server-side authorization decisions
must not depend on a browser's copy of either value.
