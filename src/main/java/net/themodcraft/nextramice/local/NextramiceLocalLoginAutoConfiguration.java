package net.themodcraft.nextramice.local;

import net.themodcraft.nextramice.bff.NextramiceBffAutoConfiguration;
import net.themodcraft.nextramice.bff.NextramiceSessionService;
import net.themodcraft.nextramice.configuration.condition.ConditionalOnNextramiceBff;
import net.themodcraft.nextramice.local.internal.AnnotatedLocalUserService;
import net.themodcraft.nextramice.local.internal.AnnotatedRegistrationService;
import net.themodcraft.nextramice.local.web.NextramiceLoginController;
import net.themodcraft.nextramice.local.web.NextramiceRegistrationController;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@AutoConfiguration(after = NextramiceBffAutoConfiguration.class)
@ConditionalOnNextramiceBff
@ConditionalOnProperty(prefix = "nextramice.local-login", name = "enabled", havingValue = "true")
public class NextramiceLocalLoginAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    NextramiceLocalUserService nextramiceLocalUserService(ListableBeanFactory beanFactory) {
        return new AnnotatedLocalUserService(beanFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    PasswordEncoder nextramicePasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    SessionAuthenticationStrategy nextramiceSessionAuthenticationStrategy() {
        return new ChangeSessionIdAuthenticationStrategy();
    }

    @Bean
    @ConditionalOnMissingBean
    SecurityContextRepository nextramiceSecurityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    NextramiceLocalAuthenticationService nextramiceLocalAuthenticationService(
            NextramiceLocalUserService users,
            PasswordEncoder passwordEncoder,
            SessionAuthenticationStrategy sessionStrategy,
            SecurityContextRepository securityContextRepository
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(passwordEncoder);
        return new NextramiceLocalAuthenticationService(
                new ProviderManager(provider),
                sessionStrategy,
                securityContextRepository
        );
    }

    @Bean
    @ConditionalOnMissingBean
    NextramiceLoginController nextramiceLoginController(
            NextramiceLocalAuthenticationService authenticator,
            NextramiceSessionService sessions
    ) {
        return new NextramiceLoginController(authenticator, sessions);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(
            prefix = "nextramice.local-login.registration",
            name = "enabled",
            havingValue = "true"
    )
    static class RegistrationConfiguration {

        @Bean
        @ConditionalOnMissingBean
        NextramiceRegistrationService nextramiceRegistrationService(ListableBeanFactory beanFactory) {
            return new AnnotatedRegistrationService(beanFactory);
        }

        @Bean
        @ConditionalOnMissingBean
        NextramiceRegistrationController nextramiceRegistrationController(
                NextramiceRegistrationService registrations
        ) {
            return new NextramiceRegistrationController(registrations);
        }
    }
}
