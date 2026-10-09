package net.themodcraft.nextramice;

import net.themodcraft.nextramice.providers.AuthorityProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class NextramiceAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(NextramiceAutoConfiguration.class));

    @Test
    void suppliesDefaultBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AuthorityProvider.class);
            assertThat(context).hasSingleBean(NextramiceOidcAuthenticationConverter.class);
        });
    }

    @Test
    void preservesConsumerAuthorityProvider() {
        AuthorityProvider customProvider = principal -> java.util.List.of();

        contextRunner
                .withBean(AuthorityProvider.class, () -> customProvider)
                .run(context -> assertThat(context.getBean(AuthorityProvider.class))
                        .isSameAs(customProvider));
    }
}
