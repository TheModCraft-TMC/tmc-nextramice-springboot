# Configuration and authentication flow

## Select a mode

```yaml
nextramice:
  web:
    mode: resource-server # resource-server | bff | hybrid
```

- `resource-server` preserves the JWT converter behavior and is the default.
- `bff` enables server-side browser session infrastructure.
- `hybrid` enables BFF infrastructure while allowing the application to define
  a separate bearer-token chain.

The mode controls Nextramice's own endpoints and helpers. It does not remove
Spring Security's OAuth client classes: an application in `resource-server`
mode may still configure `oauth2Login` or an `OAuth2AuthorizedClientManager` for
its own web flows.

Nextramice does not create complete `SecurityFilterChain` beans. In BFF mode,
apply `NextramiceBffSecurityConfigurer` to the application's chain. Hybrid
applications must use explicit, non-overlapping security matchers. Nextramice
validates those matchers at startup and scopes the configured BFF chain to the
BFF matcher, Nextramice endpoints, and OAuth callback endpoints.

## Harden the browser session

```yaml
nextramice:
  bff:
    base-path: /nextramice
    post-login-redirect: /
    post-logout-redirect: /
    provider-logout: false
    refresh-skew: 1m
    allowed-origins:
      - https://app.example.test
    exposed-claims:
      - tenantId
    session:
      idle-timeout: 30m
      absolute-timeout: 8h
      cookie:
        name: NEXTRAMICE_SESSION
        path: /
        http-only: true
        secure: true
        same-site: lax
```

Redirect properties accept local absolute paths only. Cross-origin BFF calls
are credentialed and allowed only for the exact configured origins. The
configuration rejects `SameSite=None` unless the cookie is secure.

The idle timeout is applied to the servlet session. An additional filter
enforces the absolute lifetime even while the session remains active. For local
HTTP development, explicitly set `secure: false`; production defaults are
secure.

Only names in `exposed-claims` are copied into browser session responses. The
raw provider token and unrestricted provider attributes are never returned.

## Request flow

For each request carrying a bearer token:

1. Spring Security extracts the token from the `Authorization` header.
2. Spring's configured `JwtDecoder` verifies the token.
3. Spring calls `NextramiceOidcAuthenticationConverter` with the validated
   `Jwt`.
4. Nextramice selects a principal name.
5. The configured `AuthorityProvider` resolves authorities for that principal.
6. Every `AuthenticationClaimProvider` contributes application claims.
7. Nextramice returns `NextramiceJwtAuthenticationToken`.
8. Spring stores that authentication in the request's security context.

Nextramice is downstream of token validation. It does not decode untrusted
tokens itself and does not replace issuer, signature, expiry, or audience
validation.

## Auto-configured beans

`NextramiceAutoConfiguration` supplies:

- a `NextramiceOidcAuthenticationConverter` when the application does not define
  one;
- an empty `DefaultAuthorityProvider` when the application does not define an
  `AuthorityProvider`;
- injection of all discovered `AuthenticationClaimProvider` beans into the
  converter.

Spring Boot loads the auto-configuration from:

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

The starter deliberately does not create a `SecurityFilterChain` or `JwtDecoder`.
This avoids overriding the consuming application's endpoint and issuer policy.

## Principal selection

The converter uses the first non-blank value from:

```text
email -> unique_name -> upn
```

The selected value becomes both `Authentication#getName()` and the value passed
to `AuthorityProvider#getAuthorities`.

## Claim-provider ordering

Claim providers are injected as an ordered Spring list. Use `@Order` or implement
`Ordered` when precedence matters:

```java
@Bean
@Order(10)
AuthenticationClaimProvider baseClaims() {
    return jwt -> Map.of("locale", "en");
}

@Bean
@Order(20)
AuthenticationClaimProvider userPreferences() {
    return jwt -> Map.of("locale", "de-CH");
}
```

When providers emit the same key, the later provider wins. A provider may return
`null`; null maps and entries with null values are ignored. Providers should
normally return an empty map instead, because that contract is clearer.

## Replacing the converter

Applications can provide their own bean:

```java
@Bean
NextramiceOidcAuthenticationConverter customConverter(
        AuthorityProvider authorities,
        List<AuthenticationClaimProvider> claims
) {
    return new NextramiceOidcAuthenticationConverter(authorities, claims);
}
```

Because auto-configuration uses `@ConditionalOnMissingBean`, the application
bean takes precedence.

## Provider setup

Provider-specific issuer URLs, audiences, scopes, and claim mappings belong in
Spring Security and the identity provider. Nextramice currently has no special
adapter for Microsoft Entra ID, Authentik, Google, Keycloak, or Okta.

Consult the identity provider's current official documentation when configuring
issuer and audience validation.

For OAuth client login and internal sessions, continue with the
[BFF and local-login guide](bff-and-local-login.md).
