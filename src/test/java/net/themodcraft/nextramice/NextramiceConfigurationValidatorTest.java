package net.themodcraft.nextramice;

import net.themodcraft.nextramice.configuration.NextramiceConfigurationValidator;
import net.themodcraft.nextramice.configuration.NextramiceMode;
import net.themodcraft.nextramice.configuration.NextramiceProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NextramiceConfigurationValidatorTest {

    @Test
    void rejectsOverlappingHybridMatchers() {
        NextramiceProperties properties = new NextramiceProperties();
        properties.getWeb().setMode(NextramiceMode.HYBRID);
        properties.getWeb().setBffMatcher("/api/**");
        properties.getWeb().setResourceServerMatcher("/api/admin/**");

        assertThatThrownBy(() -> new NextramiceConfigurationValidator(properties)
                .afterPropertiesSet())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must not overlap");
    }

    @Test
    void rejectsExternalRedirectTargets() {
        NextramiceProperties properties = new NextramiceProperties();
        properties.getBff().setPostLoginRedirect("https://evil.example");

        assertThatThrownBy(() -> new NextramiceConfigurationValidator(properties)
                .afterPropertiesSet())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("local absolute path");
    }

    @Test
    void rejectsInsecureSameSiteNoneCookie() {
        NextramiceProperties properties = new NextramiceProperties();
        properties.getBff().getSession().getCookie()
                .setSameSite(NextramiceProperties.Bff.SameSite.NONE);
        properties.getBff().getSession().getCookie().setSecure(false);

        assertThatThrownBy(() -> new NextramiceConfigurationValidator(properties)
                .afterPropertiesSet())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SameSite=None requires");
    }
}
