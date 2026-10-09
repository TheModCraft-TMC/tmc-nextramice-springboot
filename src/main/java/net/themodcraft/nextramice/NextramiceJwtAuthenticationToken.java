package net.themodcraft.nextramice;

import net.themodcraft.nextramice.factories.ClaimProxyFactory;
import net.themodcraft.nextramice.interfaces.AdditionalClaims;
import net.themodcraft.nextramice.interfaces.GeneratedAdditionalClaims;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.Serial;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class NextramiceJwtAuthenticationToken extends JwtAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 1L;

    private final GeneratedAdditionalClaims claims;
    private final Map<String, Object> claimValues;

    public NextramiceJwtAuthenticationToken(
            Jwt jwt,
            Collection<? extends GrantedAuthority> authorities,
            GeneratedAdditionalClaims claims,
            String principalName
    ) {
        this(jwt, authorities, claims.asMap(), principalName);
    }

    public NextramiceJwtAuthenticationToken(
            Jwt jwt,
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> claimValues,
            String principalName
    ) {
        super(jwt, authorities, Objects.requireNonNull(principalName));
        this.claimValues = Map.copyOf(claimValues);
        this.claims = ClaimProxyFactory.create(GeneratedAdditionalClaims.class, this.claimValues);
    }

    public GeneratedAdditionalClaims getClaims() {
        return claims;
    }

    public Map<String, Object> getClaimValues() {
        return claimValues;
    }

    public <T extends AdditionalClaims> T getClaims(Class<T> viewType) {
        Objects.requireNonNull(viewType);
        if (!viewType.isInterface()) {
            throw new IllegalArgumentException("Claim view must be an interface: " + viewType.getName());
        }
        return ClaimProxyFactory.create(viewType, claimValues);
    }
}
