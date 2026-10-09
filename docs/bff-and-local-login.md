# BFF mode and internal login

This is the first snapshot implementation of the Nextramice
backend-for-frontend contract. It keeps authentication state on the Spring
backend and exposes a small browser-facing API.

## Enable BFF mode

```yaml
nextramice:
  web:
    mode: bff
  bff:
    base-path: /nextramice
```

Available endpoints:

| Method and path | Purpose |
| --- | --- |
| `GET /nextramice/session` | Return the normalized current session |
| `GET /nextramice/csrf` | Return CSRF header, parameter, and token values |
| `GET /nextramice/providers` | List configured OAuth client registrations |
| `POST /nextramice/logout` | Spring Security logout URL configured by the helper |
| `POST /nextramice/login` | Internal login when explicitly enabled |
| `POST /nextramice/register` | Registration when separately enabled |

The session endpoint returns `authenticated: false` for an anonymous request.
OIDC, bearer, and local authentication all map to the same response structure.

## Apply the BFF security configurer

Nextramice does not replace the application's complete security chain. Apply its
configurer, then define the application's own authorization rules:

```java
@Configuration
@EnableWebSecurity
class SecurityConfiguration {

    @Bean
    SecurityFilterChain browserSecurity(
            HttpSecurity http,
            NextramiceBffSecurityConfigurer nextramice
    ) throws Exception {
        nextramice.configure(http);
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated())
                .build();
    }
}
```

The configurer keeps session creation on demand, permits the authentication
contract endpoints, configures logout, and enables `oauth2Login` when client
registrations exist. It does not disable CSRF.

## Configure an OIDC provider

Use Spring Security's standard client properties. For example:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          authentik:
            provider: authentik
            client-id: ${OIDC_CLIENT_ID}
            client-secret: ${OIDC_CLIENT_SECRET}
            scope: openid,profile,email
        provider:
          authentik:
            issuer-uri: https://identity.example.test/application/o/nextramice/
```

Start login through the URL returned by `GET /nextramice/providers`, normally
`/oauth2/authorization/{registrationId}`.

## Enable internal login

```yaml
nextramice:
  web:
    mode: bff
  local-login:
    enabled: true
```

Provide a typed service:

```java
@Bean
NextramiceLocalUserService localUsers(UserRepository users) {
    return username -> users.findForLogin(username)
            .map(user -> NextramiceLocalUser.enabled(
                    user.id(),
                    user.username(),
                    user.displayName(),
                    user.encodedPassword(),
                    user.authorities()
            ))
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
}
```

The password value returned by the service must already be encoded. By default,
Nextramice uses Spring Security's delegating password encoder, so stored values
include an algorithm prefix such as `{bcrypt}`.

Alternatively, annotate exactly one public Spring-bean method:

```java
@NextramiceLogin
public NextramiceLocalUser loadUser(String normalizedUsername) {
    return users.findForAuthentication(normalizedUsername);
}
```

The annotation does not map a controller. It adapts the method to
`NextramiceLocalUserService`; Spring Security still performs password matching.

Login request:

```http
POST /nextramice/login
Content-Type: application/json
X-CSRF-TOKEN: ...

{
  "username": "mouse@example.test",
  "password": "user-entered-password"
}
```

Successful login rotates the session identifier, saves the security context,
and returns the normalized session. Failures use a generic `401` response.

## Enable registration

Registration is disabled separately:

```yaml
nextramice:
  local-login:
    enabled: true
    registration:
      enabled: true
```

Provide `NextramiceRegistrationService` or one annotated method:

```java
@NextramiceRegister
public NextramiceRegistrationResult register(
        NextramiceRegistrationRequest request
) {
    // Validate, encode the password, persist, and start verification.
    return new NextramiceRegistrationResult(
            NextramiceRegistrationResult.Status.VERIFICATION_REQUIRED,
            createdUserId
    );
}
```

The application owns duplicate-account handling, password policy, password
encoding, persistence, verification, consent, rate limiting, CAPTCHA, and audit
events. Nextramice does not automatically log in a newly registered user.

## Hybrid mode

Use `hybrid` when one application hosts browser routes and bearer-token API
routes:

```yaml
nextramice:
  web:
    mode: hybrid
    bff-matcher: /app/**
    resource-server-matcher: /api/**
```

The current snapshot exposes these matcher properties but does not generate the
two chains. Define two ordered `SecurityFilterChain` beans and apply the BFF
configurer only to the browser chain. Matcher-overlap validation is planned but
not implemented yet.

## Current limitations

- The framework-neutral TypeScript client is included, but React/Next.js,
  Angular, and Vue adapters are not yet included.
- Session storage uses the consuming application's Spring Security/Spring
  Session configuration.
- OAuth token refresh and provider logout behavior are currently delegated to
  Spring Security defaults.
- Session claims for OAuth login are intentionally empty until an explicit
  allowlist/mapper API is added.
- Rate limiting, account lock policy, MFA, password reset, and verification
  delivery remain application responsibilities.
