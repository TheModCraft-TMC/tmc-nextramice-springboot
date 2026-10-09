# Typed claim-view generation

Nextramice contains an experimental annotation processor that scans a method
marked with `@GenerateClaimView`. It finds two-argument `put` calls and generates
getter declarations in `GeneratedAdditionalClaims`.

## Example

```java
import net.themodcraft.nextramice.interfaces.GenerateClaimView;

@GenerateClaimView
Map<String, Object> provideClaims(Jwt jwt) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("tenantId", jwt.getClaimAsString("tenant_id"));
    claims.put("displayName", jwt.getClaimAsString("name"));
    return claims;
}
```

The intended generated shape is:

```java
package net.themodcraft.nextramice.interfaces;

public interface GeneratedAdditionalClaims extends AdditionalClaims {
    String getTenantId();
    String getDisplayName();
}
```

At runtime, the claim proxy translates `getTenantId()` to the `tenantId` map key.
Only JavaBean-style methods beginning with `get` are supported.

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
                <version>0.0.1-SNAPSHOT</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

Generated sources normally appear below `target/generated-sources/annotations`.

## Current limitations

- The processor targets Java 25.
- It uses the javac tree API and is therefore tied to javac-compatible builds.
- Claim keys must come from source expressions the scanner can interpret.
- Type inference is intentionally limited.
- Multiple annotated methods may attempt to generate the same interface.
- Renaming a map key changes its generated method and is an API change for code
  that calls the getter.

Treat this feature as experimental until these cases have explicit compiler
tests and a stable generation contract. Plain `AuthenticationClaimProvider`
implementations remain usable without annotation processing.
