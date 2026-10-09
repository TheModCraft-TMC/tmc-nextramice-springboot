# Typed claim-view generation

Nextramice contains an annotation processor that generates a typed claim-view
interface from explicit `@GenerateClaimView` metadata. It uses standard
annotation-processing APIs and is tested with Eclipse ECJ; it does not inspect
compiler-specific method-body syntax.

## Example

```java
import net.themodcraft.nextramice.interfaces.GenerateClaimView;

@GenerateClaimView(
        name = "TenantClaims",
        claims = {
                @ClaimDefinition(name = "tenantId", type = String.class),
                @ClaimDefinition(name = "displayName", type = String.class)
        }
)
Map<String, Object> provideClaims(Jwt jwt) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("tenantId", jwt.getClaimAsString("tenant_id"));
    claims.put("displayName", jwt.getClaimAsString("name"));
    return claims;
}
```

The intended generated shape is:

```java
package example.security;

public interface TenantClaims extends AdditionalClaims {
    String getTenantId();
    String getDisplayName();
}
```

At runtime, the claim proxy translates `getTenantId()` to the `tenantId` map key.
Only JavaBean-style methods beginning with `get` are supported.

```java
TenantClaims claims = authentication.getClaims(TenantClaims.class);
String tenantId = claims.getTenantId();
```

## Maven compiler setup

Java 23 and newer require annotation processors to be enabled explicitly. Add
the starter to the consuming project's processor path:

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
                <version>0.1.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

Generated sources normally appear below `target/generated-sources/annotations`.

## Eclipse ECJ

Nextramice's normal Maven build uses the Eclipse compiler through
`plexus-compiler-eclipse`. Consumers do not have to use ECJ: the generated-view
processor uses only `javax.annotation.processing` and `javax.lang.model` APIs and
works with compatible ECJ and javac builds.

For a Maven 3 consumer that also wants ECJ, configure the compiler plugin with
`compilerId` set to `eclipse` and add `plexus-compiler-eclipse` as a plugin
dependency. The Nextramice build itself is the maintained reference setup.

## Current limitations

- The processor targets Java 25.
- Claim names must be valid lower-camel-case Java property names.
- Types must be expressible as class literals.
- Two annotated methods cannot generate the same fully qualified interface.
- Renaming a map key changes its generated method and is an API change for code
  that calls the getter.

Plain `AuthenticationClaimProvider` implementations remain usable without
annotation processing.
