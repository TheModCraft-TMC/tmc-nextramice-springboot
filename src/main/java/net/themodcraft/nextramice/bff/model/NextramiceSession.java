package net.themodcraft.nextramice.bff.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record NextramiceSession(
        boolean authenticated,
        NextramicePrincipal principal,
        List<String> authorities,
        Map<String, Object> claims,
        Instant expiresAt,
        String loginMethod,
        String provider
) {
    public static NextramiceSession anonymous() {
        return new NextramiceSession(false, null, List.of(), Map.of(), null, null, null);
    }
}
