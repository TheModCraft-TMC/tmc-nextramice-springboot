package net.themodcraft.nextramice;


import net.themodcraft.nextramice.providers.AuthenticationClaimProvider;
import net.themodcraft.nextramice.providers.AuthorityProvider;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class NextramiceOidcAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger log = LoggerFactory.getLogger(NextramiceOidcAuthenticationConverter.class);

    private final AuthorityProvider authorityProvider;
    private final List<AuthenticationClaimProvider> claimProviders;

    public NextramiceOidcAuthenticationConverter(
            AuthorityProvider authorityProvider,
            List<AuthenticationClaimProvider> claimProviders
    ) {
        this.authorityProvider = authorityProvider;
        this.claimProviders = List.copyOf(claimProviders);
    }

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        String principalName = getPrincipalName(jwt);

        if (StringUtils.isBlank(principalName)) {
            log.error("No supported principal claim found in token");
            return null;
        }

        Map<String, Object> claims = new LinkedHashMap<>();
        for (AuthenticationClaimProvider provider : claimProviders) {
            Map<String, Object> providedClaims = provider.provideClaims(jwt);
            if (providedClaims == null) {
                continue;
            }
            for (Map.Entry<String, Object> entry : providedClaims.entrySet()) {
                if (entry.getValue() != null) {
                    claims.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return new NextramiceJwtAuthenticationToken(
                jwt,
                authorityProvider.getAuthorities(principalName),
                claims,
                principalName
        );
    }

    public String getPrincipalName(Jwt jwt) {
        return StringUtils.firstNonBlank(jwt.getClaimAsString("email"), jwt.getClaimAsString("unique_name"), jwt.getClaimAsString("upn"));
    }
}
