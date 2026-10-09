package net.themodcraft.nextramice;

import net.themodcraft.nextramice.bff.NextramiceBffAutoConfiguration;
import net.themodcraft.nextramice.bff.NextramiceBffSecurityConfigurer;
import net.themodcraft.nextramice.bff.NextramiceSessionService;
import net.themodcraft.nextramice.bff.web.NextramiceCsrfController;
import net.themodcraft.nextramice.bff.web.NextramiceProviderController;
import net.themodcraft.nextramice.bff.web.NextramiceSessionController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class NextramiceBffAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    NextramiceAutoConfiguration.class,
                    NextramiceBffAutoConfiguration.class
            ));

    @Test
    void doesNotCreateBffBeansInDefaultResourceServerMode() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(NextramiceSessionService.class);
            assertThat(context).doesNotHaveBean(NextramiceSessionController.class);
        });
    }

    @Test
    void createsBffBeansInBffMode() {
        contextRunner
                .withPropertyValues("nextramice.web.mode=bff")
                .run(context -> {
                    assertThat(context).hasSingleBean(NextramiceSessionService.class);
                    assertThat(context).hasSingleBean(NextramiceSessionController.class);
                    assertThat(context).hasSingleBean(NextramiceCsrfController.class);
                    assertThat(context).hasSingleBean(NextramiceProviderController.class);
                    assertThat(context).hasSingleBean(NextramiceBffSecurityConfigurer.class);
                });
    }

    @Test
    void createsBffBeansInHybridMode() {
        contextRunner
                .withPropertyValues("nextramice.web.mode=hybrid")
                .run(context -> assertThat(context).hasSingleBean(NextramiceSessionService.class));
    }
}
