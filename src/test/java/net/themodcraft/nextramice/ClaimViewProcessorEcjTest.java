package net.themodcraft.nextramice;

import net.themodcraft.nextramice.factories.ClaimProxyFactory;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ClaimViewProcessorEcjTest {

    @Test
    void generatesTypedClaimInterfaceDuringEcjTestCompilation() {
        EcjGeneratedClaims claims = ClaimProxyFactory.create(
                EcjGeneratedClaims.class,
                Map.of("tenantId", "mice", "loginCount", 4)
        );

        assertThat(claims.getTenantId()).isEqualTo("mice");
        assertThat(claims.getLoginCount()).isEqualTo(4);
        assertThat(claims.asMap()).containsEntry("tenantId", "mice");
    }
}
