package net.themodcraft.nextramice.bff;

import net.themodcraft.nextramice.configuration.NextramiceProperties;
import net.themodcraft.nextramice.configuration.NextramiceMode;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

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
        configureMatcher(http, basePath);
        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .addFilterBefore(
                        new NextramiceAbsoluteSessionTimeoutFilter(
                                properties.getBff().getSession().getAbsoluteTimeout()),
                        SecurityContextHolderFilter.class)
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
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                matcher(basePath + "/**")))
                .logout(logout -> {
                    logout.logoutUrl(basePath + "/logout");
                    ClientRegistrationRepository repository = registrations.getIfAvailable();
                    if (properties.getBff().isProviderLogout() && repository != null) {
                        OidcClientInitiatedLogoutSuccessHandler handler =
                                new OidcClientInitiatedLogoutSuccessHandler(repository);
                        handler.setPostLogoutRedirectUri("{baseUrl}"
                                + properties.getBff().getPostLogoutRedirect());
                        logout.logoutSuccessHandler(handler);
                    } else {
                        logout.logoutSuccessHandler(
                                new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT));
                    }
                });

        if (!properties.getBff().getAllowedOrigins().isEmpty()) {
            CorsConfiguration cors = new CorsConfiguration();
            cors.setAllowedOrigins(List.copyOf(properties.getBff().getAllowedOrigins()));
            cors.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
            cors.setAllowedHeaders(List.of(
                    "Accept", "Content-Type", "X-CSRF-TOKEN", "X-XSRF-TOKEN"));
            cors.setAllowCredentials(true);
            cors.setMaxAge(3600L);
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration(basePath + "/**", cors);
            http.cors(configuration -> configuration.configurationSource(source));
        }

        if (registrations.getIfAvailable() != null) {
            SimpleUrlAuthenticationSuccessHandler success = new SimpleUrlAuthenticationSuccessHandler();
            success.setDefaultTargetUrl(properties.getBff().getPostLoginRedirect());
            success.setAlwaysUseDefaultTargetUrl(true);
            http.oauth2Login(login -> login.successHandler(success));
        }
    }

    private void configureMatcher(HttpSecurity http, String basePath) {
        if (properties.getWeb().getMode() != NextramiceMode.HYBRID) {
            return;
        }
        List<RequestMatcher> matchers = new ArrayList<>();
        matchers.add(matcher(properties.getWeb().getBffMatcher()));
        matchers.add(matcher(basePath + "/**"));
        matchers.add(matcher("/oauth2/authorization/**"));
        matchers.add(matcher("/login/oauth2/code/**"));
        http.securityMatcher(new OrRequestMatcher(matchers));
    }

    private static RequestMatcher matcher(String pattern) {
        return PathPatternRequestMatcher.withDefaults().matcher(pattern);
    }
}
