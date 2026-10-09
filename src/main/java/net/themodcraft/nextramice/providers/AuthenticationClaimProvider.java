package net.themodcraft.nextramice.providers;


import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;

public interface AuthenticationClaimProvider {
    Map<String, Object> provideClaims(Jwt jwt);
}