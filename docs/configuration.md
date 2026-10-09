# Configuration and authentication flow

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
