package net.themodcraft.nextramice;

import net.themodcraft.nextramice.factories.ClaimProxyFactory;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimProxyFactoryTest {

    @Test
    void exposesMapEntriesAsTypedGetterMethods() {
        TestClaims claims = ClaimProxyFactory.create(
                TestClaims.class,
                Map.of("tenantId", "mice", "loginCount", 7)
        );

        assertThat(claims.getTenantId()).isEqualTo("mice");
        assertThat(claims.getLoginCount()).isEqualTo(7);
        assertThat(claims.toString()).contains("TestClaims", "tenantId=mice");
    }

    @Test
    void rejectsUnsupportedInterfaceMethods() {
        TestClaims claims = ClaimProxyFactory.create(TestClaims.class, Map.of());

        assertThatThrownBy(claims::tenantId)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("tenantId");
    }

    private interface TestClaims {
        String getTenantId();

        int getLoginCount();

        String tenantId();
    }
}
