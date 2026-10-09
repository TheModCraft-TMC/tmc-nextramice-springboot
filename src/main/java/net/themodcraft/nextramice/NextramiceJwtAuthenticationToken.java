package net.themodcraft.nextramice;

import net.themodcraft.nextramice.interfaces.GeneratedAdditionalClaims;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.Objects;

@Getter
@EqualsAndHashCode(callSuper = true)
public class NextramiceJwtAuthenticationToken extends JwtAuthenticationToken {

    private final GeneratedAdditionalClaims claims;

    public NextramiceJwtAuthenticationToken(
            Jwt jwt,
            Collection<? extends GrantedAuthority> authorities,
            GeneratedAdditionalClaims claims,
            String principalName
    ) {
        super(jwt, authorities, Objects.requireNonNull(principalName));
        this.claims = Objects.requireNonNull(claims);
    }
}
