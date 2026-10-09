package net.themodcraft.nextramice;

import net.themodcraft.nextramice.providers.AuthenticationClaimProvider;
import net.themodcraft.nextramice.providers.AuthorityProvider;
import net.themodcraft.nextramice.providers.DefaultAuthorityProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
public class NextramiceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NextramiceOidcAuthenticationConverter nextramiceOidcAuthenticationConverter(
            AuthorityProvider authorityProvider,
            List<AuthenticationClaimProvider> claimProviders
    ) {
        return new NextramiceOidcAuthenticationConverter(authorityProvider, claimProviders);
    }

    @Bean
    @ConditionalOnMissingBean(AuthorityProvider.class)
    public AuthorityProvider defaultAuthorityProvider() {
        return new DefaultAuthorityProvider();
    }
}
