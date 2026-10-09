# Nextramice documentation

Nextramice is a Spring Boot starter that turns a JWT already validated by Spring
Security into an application-specific authentication token. It lets an
application resolve its own authorities, add derived claims, and consume those
claims through a typed view.

## Start here

1. [Install and connect Nextramice](getting-started.md).
2. Read the [configuration and request flow](configuration.md).
3. Add application [authorities and claims](customization.md).
4. Optionally generate [typed claim getters](typed-claim-views.md).
5. Review the [security boundary and production checklist](security.md).

## Current scope

The current starter provides:

- Spring Boot auto-configuration for `NextramiceOidcAuthenticationConverter`;
- integration with Spring Security's JWT resource-server support;
- application-defined authority resolution;
- ordered application-defined claim providers;
- `NextramiceJwtAuthenticationToken` with an additional typed claim view;
- an annotation processor that can generate claim getter declarations.

It does not currently provide browser login, sessions, OAuth client
registration, token issuance, user registration, password login, or a
backend-for-frontend. Those features are design work, not released behavior.

## Requirements

- Java 25
- Spring Boot 3.5.x
- Maven for the documented build setup
- A JWT issuer and decoder configuration supported by Spring Security

Nextramice is currently a snapshot project. Until it is published to a package
repository, build it locally with `mvn install` before adding the dependency to
another local Maven project.
