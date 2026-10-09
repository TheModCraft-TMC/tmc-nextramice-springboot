package net.themodcraft.nextramice.bff;

import net.themodcraft.nextramice.NextramiceAutoConfiguration;
import net.themodcraft.nextramice.bff.web.NextramiceCsrfController;
import net.themodcraft.nextramice.bff.web.NextramiceProviderController;
import net.themodcraft.nextramice.bff.web.NextramiceSessionController;
import net.themodcraft.nextramice.configuration.NextramiceProperties;
import net.themodcraft.nextramice.configuration.condition.ConditionalOnNextramiceBff;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

@AutoConfiguration(after = NextramiceAutoConfiguration.class)
@ConditionalOnNextramiceBff
@ConditionalOnClass({RestController.class, OAuth2AuthenticationToken.class})
public class NextramiceBffAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    NextramiceSessionService nextramiceSessionService() {
        return new NextramiceSessionService();
    }

    @Bean
    @ConditionalOnMissingBean
    NextramiceSessionController nextramiceSessionController(NextramiceSessionService sessions) {
        return new NextramiceSessionController(sessions);
    }

    @Bean
    @ConditionalOnMissingBean
    NextramiceCsrfController nextramiceCsrfController() {
        return new NextramiceCsrfController();
    }

    @Bean
    @ConditionalOnMissingBean
    NextramiceProviderController nextramiceProviderController(
            ObjectProvider<ClientRegistrationRepository> registrations
    ) {
        return new NextramiceProviderController(registrations);
    }

    @Bean
    @ConditionalOnMissingBean
    NextramiceBffSecurityConfigurer nextramiceBffSecurityConfigurer(
            NextramiceProperties properties,
            ObjectProvider<ClientRegistrationRepository> registrations
    ) {
        return new NextramiceBffSecurityConfigurer(properties, registrations);
    }
}
