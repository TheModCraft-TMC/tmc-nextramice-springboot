package net.themodcraft.nextramice.bff;

import net.themodcraft.nextramice.configuration.NextramiceProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

public class NextramiceBffSecurityConfigurer {

    private final NextramiceProperties properties;
    private final ObjectProvider<ClientRegistrationRepository> registrations;

    public NextramiceBffSecurityConfigurer(
            NextramiceProperties properties,
            ObjectProvider<ClientRegistrationRepository> registrations
    ) {
        this.properties = properties;
        this.registrations = registrations;
    }

    public void configure(HttpSecurity http) throws Exception {
        String basePath = properties.getBff().getBasePath();
        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                basePath + "/session",
                                basePath + "/csrf",
                                basePath + "/providers",
                                basePath + "/login",
                                basePath + "/register",
                                "/oauth2/authorization/**",
                                "/login/oauth2/code/**"
                        ).permitAll())
                .logout(logout -> logout.logoutUrl(basePath + "/logout"));

        if (registrations.getIfAvailable() != null) {
            http.oauth2Login(Customizer.withDefaults());
        }
    }
}
