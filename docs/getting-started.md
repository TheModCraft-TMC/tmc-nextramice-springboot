# Getting started

## 1. Build the snapshot locally

Clone the repository and install it into your local Maven repository:

```bash
git clone https://github.com/TheModCraft-TMC/tmc-nextramice-springboot.git
cd tmc-nextramice-springboot
mvn verify
mvn install
```

## 2. Add the dependency

```xml
<dependency>
    <groupId>net.themodcraft</groupId>
    <artifactId>nextramice-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

The starter includes the Spring Security resource-server classes used by its
converter. A consuming application still owns its web security policy.

## 3. Configure the JWT issuer

For an OpenID Connect provider that exposes discovery metadata:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://identity.example.test/application/o/my-api/
```

Spring Security uses this configuration to discover keys and validate incoming
JWTs. Use the exact issuer value supplied by your identity provider.

## 4. Connect the converter

```java
package example.security;

import net.themodcraft.nextramice.NextramiceOidcAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfiguration {

    @Bean
    SecurityFilterChain applicationSecurity(
            HttpSecurity http,
            NextramiceOidcAuthenticationConverter converter
    ) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(converter)))
                .build();
    }
}
```

Do not disable CSRF solely because this example is a resource server. Choose a
CSRF policy based on how the complete application authenticates requests. See
the [security guide](security.md).

## 5. Call the API

Send a bearer token issued for the API:

```bash
curl --fail \
  --header "Authorization: Bearer ${ACCESS_TOKEN}" \
  https://api.example.test/me
```

The token must include at least one supported principal claim. Nextramice checks
these claims in order:

1. `email`
2. `unique_name`
3. `upn`

If none contains a non-blank value, the converter cannot create an
authentication token. Principal selection will become configurable in a future
release; applications using providers that only supply `sub` currently need to
map a supported claim before using this converter.

## 6. Read the authenticated principal

```java
import net.themodcraft.nextramice.NextramiceJwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
class CurrentUserController {

    @GetMapping("/me")
    Map<String, Object> me(NextramiceJwtAuthenticationToken authentication) {
        return Map.of(
                "name", authentication.getName(),
                "authorities", authentication.getAuthorities()
        );
    }
}
```

Avoid returning the complete JWT or unrestricted claim map from a public API.
