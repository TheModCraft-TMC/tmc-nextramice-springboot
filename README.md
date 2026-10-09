# Nextramice Spring Boot Starter

Nextramice is a Spring Boot library for converting validated OAuth 2.0 JWTs into
an application-specific authentication token with authorities and typed,
extensible claims.

Coordinates:

```xml
<dependency>
    <groupId>net.themodcraft</groupId>
    <artifactId>nextramice-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

The public Java namespace is `net.themodcraft.nextramice`.

## Documentation

- [Getting started](docs/getting-started.md)
- [Configuration and authentication flow](docs/configuration.md)
- [Authorities and additional claims](docs/customization.md)
- [Typed claim-view generation](docs/typed-claim-views.md)
- [Security model and production checklist](docs/security.md)

The current release is a Spring Security OAuth 2.0 resource-server extension.
It is not yet a backend-for-frontend, login server, or identity provider.

## What it does

When the starter is on the classpath, Spring Boot auto-configures:

- a `NextramiceOidcAuthenticationConverter`;
- an empty default `AuthorityProvider` when the application does not provide one;
- discovery of all application-provided `AuthenticationClaimProvider` beans.

For each validated `Jwt`, the converter:

1. selects the principal from `email`, `unique_name`, then `upn`;
2. asks the configured `AuthorityProvider` for Spring Security authorities;
3. combines additional claims from every `AuthenticationClaimProvider`;
4. exposes those claims through `GeneratedAdditionalClaims`;
5. returns a `NextramiceJwtAuthenticationToken`.

Providers follow Spring's list ordering. If providers return the same claim key,
the later provider wins. Null maps and null claim values are ignored.

The included annotation processor can inspect a method marked with
`@GenerateClaimView`. It finds two-argument `put` calls and generates typed
getter methods on `GeneratedAdditionalClaims` based on each key and value type.

## What it does not do

Nextramice does not:

- configure a `SecurityFilterChain`;
- configure or contact an OAuth/OIDC identity provider;
- create a `JwtDecoder` or validate signatures, issuers, or audiences;
- fetch users, roles, or claims from a database;
- make authorization decisions for application endpoints.

Those responsibilities stay in the consuming application. The converter only
runs after Spring Security has decoded and validated the JWT.

## Connect it to Spring Security

For a servlet resource server, supply the converter to the JWT configuration:

```java
@Configuration
@EnableWebSecurity
class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            NextramiceOidcAuthenticationConverter converter
    ) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
                .build();
    }
}
```

The consuming application must also configure a JWT decoder, normally with:

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://issuer.example.test
```

## Supply authorities

Replace the empty default provider with an application bean:

```java
@Bean
AuthorityProvider authorityProvider(UserRepository users) {
    return email -> users.rolesFor(email).stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .toList();
}
```

## Add authentication claims

Each claim provider returns a map of application-specific values:

```java
@Bean
AuthenticationClaimProvider tenantClaims() {
    return jwt -> Map.of(
            "tenantId", jwt.getClaimAsString("tenant_id"),
            "displayName", jwt.getClaimAsString("name")
    );
}
```

The values are available through
`NextramiceJwtAuthenticationToken#getClaims()`.

## Generate a typed claim view

Mark a method containing the claim-map population logic:

```java
@GenerateClaimView
Map<String, Object> provideClaims(Jwt jwt) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("tenantId", jwt.getClaimAsString("tenant_id"));
    claims.put("displayName", jwt.getClaimAsString("name"));
    return claims;
}
```

The processor generates getters similar to:

```java
public interface GeneratedAdditionalClaims extends AdditionalClaims {
    String getTenantId();
    String getDisplayName();
}
```

On Java 23 and newer, annotation processors must be enabled explicitly. Add the
starter to the consumer's Maven compiler processor path:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <proc>full</proc>
        <annotationProcessorPaths>
            <path>
                <groupId>net.themodcraft</groupId>
                <artifactId>nextramice-spring-boot-starter</artifactId>
                <version>0.0.1-SNAPSHOT</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

The processor uses the javac tree API and this project currently targets Java 25.

## Build

```bash
mvn verify
```
