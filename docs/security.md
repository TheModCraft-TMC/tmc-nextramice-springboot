# Security model

## Trust boundary

Nextramice receives a `Jwt` only after Spring Security's `JwtDecoder` has
accepted it. The consuming application is responsible for configuring:

- the trusted issuer and signing keys;
- accepted algorithms;
- expiry and not-before validation;
- the intended audience validation policy;
- transport security and proxy/header handling;
- endpoint authorization rules.

Never parse an arbitrary bearer token and call the Nextramice converter directly
as a substitute for validation.

## Token type

Configure the application to accept access tokens intended for its API. Do not
use an ID token as an API bearer token merely because both happen to be JWTs.

## Principal and identity

The current converter chooses `email`, `unique_name`, then `upn`. Email addresses
can change and may not be suitable as permanent database identifiers. If an
application stores an external identity, retain the validated issuer and subject
as the durable key and treat email as profile data.

## Authority loading

`AuthorityProvider` is security-sensitive application code:

- deny access when required authority data cannot be established;
- normalize role names in one place;
- avoid accepting arbitrary role strings from untrusted application input;
- define cache invalidation for revoked permissions;
- monitor dependency latency without logging tokens or sensitive user records.

## Logging and API responses

Do not log raw JWTs, bearer headers, provider secrets, or unrestricted claim
maps. Avoid returning the complete `Jwt` or authentication object from a
controller. Create explicit response DTOs containing only fields the caller may
see.

## CSRF and browser use

A stateless API that accepts bearer tokens exclusively through the
`Authorization` header has different CSRF exposure from a cookie-authenticated
browser application. If the application also uses cookies, form login, OAuth
login, or sessions, keep CSRF protection enabled for those routes and design
separate security chains deliberately.

In BFF mode, Spring Security stores the authenticated context and authorized
OAuth client on the server side. A frontend should use the session cookie and
CSRF endpoint; it must not copy access or refresh tokens into local storage.

Applications still own cookie settings and production session persistence. Set
cookies to `HttpOnly`, `Secure`, and an appropriate `SameSite` value, and use
Spring Session with a shared store when sessions must survive instance changes.

## Production checklist

- Use HTTPS for every non-local request.
- Pin the exact trusted issuer and validate the API audience.
- Keep framework and identity-provider dependencies patched.
- Permit only the algorithms required by the issuer.
- Define behavior for signing-key rotation and identity-provider outages.
- Return `401` for missing/invalid authentication and `403` for insufficient
  authority without leaking internal details.
- Test tokens with wrong issuer, wrong audience, expired timestamps, missing
  principal claims, and unexpected role/claim types.
- Keep secrets in a secret manager or deployment environment, never source
  control.
- Review every additional claim before exposing it through an endpoint.
