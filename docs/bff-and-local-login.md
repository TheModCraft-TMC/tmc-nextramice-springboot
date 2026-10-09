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
registrations exist. It does not disable CSRF. Local logout returns `204`; when
`nextramice.bff.provider-logout=true`, it uses Spring Security's OIDC
RP-initiated logout handler and redirects to the validated local
`post-logout-redirect`.

When application code needs a provider access token, inject
`NextramiceAuthorizedClientService`. It uses Spring's authorized-client
repository, refreshes within the configured skew, and serializes refreshes per
browser session and client registration. Configure Spring Session JDBC or Redis
in the consuming application when authorized clients and sessions must survive
instance replacement.

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

### Login hardening hooks

Applications can provide the following beans without replacing the login
controller:

- `NextramiceLoginAttemptPolicy` implements rate limiting, abuse detection, and
  success/failure counters;
- `NextramiceAccountStatusPolicy` adds application-specific lock, tenant, or
  eligibility checks after Spring's standard enabled/locked checks;
- `NextramicePasswordUpgradeService` persists a newly encoded adaptive hash
  when Spring's `PasswordEncoder` requests an upgrade;
- `NextramiceAuthenticationCompletionPolicy` enforces completed MFA or step-up
  state after password verification and before session creation.
- `NextramiceAccountLinkService` can map a validated external `(issuer,
  subject)` pair to an application-owned user ID. It is never called with email
  as an identity key.

For example, an MFA gate can reject primary authentication without creating a
session:

```java
@Bean
NextramiceAuthenticationCompletionPolicy requireMfa(MfaChallenges challenges) {
    return (authentication, request) -> {
        if (!challenges.isComplete(authentication.getName(), request)) {
            throw new InsufficientAuthenticationException("Additional authentication required");
        }
    };
}
```

The application owns the challenge protocol (TOTP, WebAuthn, recovery code, or
another mechanism). Nextramice guarantees that completion policies run before
session rotation and security-context persistence.

Every attempt also publishes a `NextramiceLoginSucceededEvent` or
`NextramiceLoginFailedEvent`. Failure events contain only the exception type,
never the submitted password or exception message.

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
encoding, persistence, verification, consent, registration abuse controls, and
CAPTCHA. Nextramice does not automatically log in a newly registered user.

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

Define two ordered `SecurityFilterChain` beans and apply the BFF configurer only
to the browser chain. The configurer scopes that chain to the BFF and OAuth
paths. Startup fails when the configured BFF and resource-server patterns
overlap. The consuming application remains responsible for the separate bearer
chain because it owns JWT issuer, decoder, and API authorization policy.

## Current limitations

- Frontend clients and stores are maintained in the separate
  `tmc-nextramice-nodePackages` project.
- Session storage uses the consuming application's Spring Security/Spring
  Session configuration.
- Provider tokens are refreshed on demand through
  `NextramiceAuthorizedClientService`; applications should use that service for
  outbound provider calls.
- Provider-specific claim and logout profiles are planned as separate optional
  dependencies. The core starter remains standards-based.
- Nextramice supplies login hardening hooks, while the application supplies the
  actual rate-limit store, MFA protocol, password-reset flow, and verification
  delivery.
