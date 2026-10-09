package net.themodcraft.nextramice;

import net.themodcraft.nextramice.interfaces.ClaimDefinition;
import net.themodcraft.nextramice.interfaces.GenerateClaimView;

import java.util.Map;

final class ClaimViewProcessorEcjFixture {

    private ClaimViewProcessorEcjFixture() {
    }

    @GenerateClaimView(
            name = "EcjGeneratedClaims",
            claims = {
                    @ClaimDefinition(name = "tenantId", type = String.class),
                    @ClaimDefinition(name = "loginCount", type = Integer.class)
            }
    )
    static Map<String, Object> claims() {
        return Map.of();
    }
}
