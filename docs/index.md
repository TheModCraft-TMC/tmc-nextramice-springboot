# Nextramice documentation

Nextramice is a Spring Boot security starter supporting validated JWT resource
servers and opt-in server-side browser authentication. It provides one
normalized session representation for external OIDC and internal login.

## Start here

1. [Install and connect Nextramice](getting-started.md).
2. Read the [configuration and request flow](configuration.md).
3. Configure [BFF mode and internal login](bff-and-local-login.md), if needed.
4. Use the framework-neutral [browser client](browser-client.md).
5. Add application [authorities and claims](customization.md).
6. Optionally generate [typed claim getters](typed-claim-views.md).
7. Review the [security boundary and production checklist](security.md).

## Current scope

The current starter provides:

- Spring Boot auto-configuration for `NextramiceOidcAuthenticationConverter`;
- integration with Spring Security's JWT resource-server support;
- application-defined authority resolution;
- ordered application-defined claim providers;
- `NextramiceJwtAuthenticationToken` with an additional typed claim view;
- an annotation processor that can generate claim getter declarations;
- `resource-server`, `bff`, and `hybrid` configuration modes;
- session, CSRF, and provider-discovery endpoints in BFF-capable modes;
- a Spring Security BFF configurer;
- internal password authentication with session-fixation protection;
- typed services or annotations for application-owned login and registration.
- a framework-neutral TypeScript client with credentials and CSRF handling.

The React/Next.js, Angular, and Vue adapters are not implemented yet. The current
BFF and TypeScript client support is the first vertical slice and should be
treated as snapshot functionality while its HTTP contract is stabilized.

## Requirements

- Java 25
- Spring Boot 3.5.x
- Maven for the documented build setup
- A JWT issuer and decoder configuration supported by Spring Security

Nextramice is currently a snapshot project. Until it is published to a package
repository, build it locally with `mvn install` before adding the dependency to
another local Maven project.
