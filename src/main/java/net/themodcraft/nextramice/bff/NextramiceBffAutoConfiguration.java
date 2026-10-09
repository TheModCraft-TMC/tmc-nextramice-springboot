package net.themodcraft.nextramice.bff;

import net.themodcraft.nextramice.NextramiceAutoConfiguration;
import net.themodcraft.nextramice.bff.web.NextramiceCsrfController;
import net.themodcraft.nextramice.bff.web.NextramiceProviderController;
import net.themodcraft.nextramice.bff.web.NextramiceSessionController;
import net.themodcraft.nextramice.configuration.NextramiceProperties;
import net.themodcraft.nextramice.configuration.NextramiceConfigurationValidator;
import net.themodcraft.nextramice.configuration.condition.ConditionalOnNextramiceBff;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.bind.annotation.RestController;

@AutoConfiguration(after = NextramiceAutoConfiguration.class)
@ConditionalOnNextramiceBff
@ConditionalOnClass({RestController.class, OAuth2AuthenticationToken.class})
public class NextramiceBffAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    NextramiceSessionService nextramiceSessionService(
            NextramiceProperties properties,
            ObjectProvider<NextramiceAccountLinkService> accountLinks
    ) {
        return new NextramiceSessionService(properties, accountLinks.getIfAvailable());
    }

    @Bean
    NextramiceConfigurationValidator nextramiceConfigurationValidator(
            NextramiceProperties properties
    ) {
        return new NextramiceConfigurationValidator(properties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "nextramiceSessionCookieInitializer")
    ServletContextInitializer nextramiceSessionCookieInitializer(NextramiceProperties properties) {
        return NextramiceSessionCookieConfiguration.initializer(properties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "nextramiceSessionCookieSameSiteSupplier")
    CookieSameSiteSupplier nextramiceSessionCookieSameSiteSupplier(NextramiceProperties properties) {
        return NextramiceSessionCookieConfiguration.sameSiteSupplier(properties);
    }

    @Bean
    @ConditionalOnBean({ClientRegistrationRepository.class, OAuth2AuthorizedClientRepository.class})
    @ConditionalOnMissingBean(OAuth2AuthorizedClientManager.class)
    OAuth2AuthorizedClientManager nextramiceAuthorizedClientManager(
            ClientRegistrationRepository registrations,
            OAuth2AuthorizedClientRepository authorizedClients,
            NextramiceProperties properties
    ) {
        OAuth2AuthorizedClientProvider provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken(refresh -> refresh.clockSkew(properties.getBff().getRefreshSkew()))
                .build();
        DefaultOAuth2AuthorizedClientManager manager =
                new DefaultOAuth2AuthorizedClientManager(registrations, authorizedClients);
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }

    @Bean
    @ConditionalOnBean(OAuth2AuthorizedClientManager.class)
    @ConditionalOnMissingBean
    NextramiceAuthorizedClientService nextramiceAuthorizedClientService(
            OAuth2AuthorizedClientManager manager
    ) {
        return new NextramiceAuthorizedClientService(manager);
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
