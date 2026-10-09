package net.themodcraft.nextramice;

import net.themodcraft.nextramice.bff.NextramiceBffAutoConfiguration;
import net.themodcraft.nextramice.local.NextramiceLocalLoginAutoConfiguration;
import net.themodcraft.nextramice.local.NextramiceLocalAuthenticationService;
import net.themodcraft.nextramice.local.NextramiceRegistrationService;
import net.themodcraft.nextramice.local.NextramiceLocalUser;
import net.themodcraft.nextramice.local.NextramiceLocalUserService;
import net.themodcraft.nextramice.local.annotation.NextramiceLogin;
import net.themodcraft.nextramice.local.annotation.NextramiceRegister;
import net.themodcraft.nextramice.local.model.NextramiceRegistrationRequest;
import net.themodcraft.nextramice.local.model.NextramiceRegistrationResult;
import net.themodcraft.nextramice.local.web.NextramiceLoginController;
import net.themodcraft.nextramice.local.web.NextramiceRegistrationController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NextramiceLocalLoginAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    NextramiceAutoConfiguration.class,
                    NextramiceBffAutoConfiguration.class,
                    NextramiceLocalLoginAutoConfiguration.class
            ));

    @Test
    void localLoginIsDisabledByDefault() {
        contextRunner
                .withPropertyValues("nextramice.web.mode=bff")
                .run(context -> assertThat(context).doesNotHaveBean(NextramiceLoginController.class));
    }

    @Test
    void adaptsAnnotatedLoginMethodWhenEnabled() {
        contextRunner
                .withUserConfiguration(AnnotatedLoginConfiguration.class)
                .withPropertyValues(
                        "nextramice.web.mode=bff",
                        "nextramice.local-login.enabled=true"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(NextramiceLoginController.class);
                    NextramiceLocalUserService users = context.getBean(NextramiceLocalUserService.class);
                    assertThat(users.loadUserByUsername("mouse@example.test").getId())
                            .isEqualTo("local-1");
                });
    }

    @Test
    void authenticatesPasswordAndSavesSecurityContextInSession() {
        contextRunner
                .withUserConfiguration(AnnotatedLoginConfiguration.class)
                .withPropertyValues(
                        "nextramice.web.mode=bff",
                        "nextramice.local-login.enabled=true"
                )
                .run(context -> {
                    MockHttpServletRequest request = new MockHttpServletRequest();
                    MockHttpServletResponse response = new MockHttpServletResponse();
                    NextramiceLocalAuthenticationService authenticator =
                            context.getBean(NextramiceLocalAuthenticationService.class);

                    authenticator.authenticate(
                            "mouse@example.test",
                            "password",
                            request,
                            response
                    );

                    SecurityContextRepository repository =
                            context.getBean(SecurityContextRepository.class);
                    SecurityContext saved = repository.loadDeferredContext(request).get();
                    assertThat(saved.getAuthentication()).isNotNull();
                    assertThat(saved.getAuthentication().getName()).isEqualTo("mouse@example.test");
                    assertThat(request.getSession(false)).isNotNull();
                });
    }

    @Test
    void registrationRequiresItsOwnEnableFlag() {
        contextRunner
                .withUserConfiguration(AnnotatedLoginConfiguration.class)
                .withPropertyValues(
                        "nextramice.web.mode=bff",
                        "nextramice.local-login.enabled=true"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(NextramiceRegistrationService.class);
                    assertThat(context).doesNotHaveBean(NextramiceRegistrationController.class);
                });
    }

    @Test
    void adaptsAnnotatedRegistrationMethodWhenExplicitlyEnabled() {
        contextRunner
                .withUserConfiguration(AnnotatedLoginConfiguration.class)
                .withPropertyValues(
                        "nextramice.web.mode=bff",
                        "nextramice.local-login.enabled=true",
                        "nextramice.local-login.registration.enabled=true"
                )
                .run(context -> {
                    NextramiceRegistrationService registrations =
                            context.getBean(NextramiceRegistrationService.class);
                    NextramiceRegistrationResult result = registrations.register(
                            new NextramiceRegistrationRequest(
                                    "new@example.test",
                                    "password",
                                    java.util.Map.of()
                            )
                    );
                    assertThat(result.status()).isEqualTo(
                            NextramiceRegistrationResult.Status.VERIFICATION_REQUIRED
                    );
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class AnnotatedLoginConfiguration {

        @Bean
        LoginHandlers loginHandlers() {
            return new LoginHandlers();
        }
    }

    static class LoginHandlers {

        @NextramiceLogin
        public NextramiceLocalUser loadUser(String username) {
            return NextramiceLocalUser.enabled(
                    "local-1",
                    username,
                    "Mouse User",
                    "{noop}password",
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
        }

        @NextramiceRegister
        public NextramiceRegistrationResult register(NextramiceRegistrationRequest request) {
            return new NextramiceRegistrationResult(
                    NextramiceRegistrationResult.Status.VERIFICATION_REQUIRED,
                    "local-2"
            );
        }
    }
}
