package net.themodcraft.nextramice;


import net.themodcraft.nextramice.factories.ClaimProxyFactory;
import net.themodcraft.nextramice.interfaces.GeneratedAdditionalClaims;
import net.themodcraft.nextramice.providers.AuthenticationClaimProvider;
import net.themodcraft.nextramice.providers.AuthorityProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class NextramiceOidcAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final AuthorityProvider authorityProvider;
    private final List<AuthenticationClaimProvider> claimProviders;

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        String principalName = getPrincipalName(jwt);

        if (StringUtils.isBlank(principalName)) {
            log.error("No supported principal claim found in token");
            return null;
        }

        Map<String, Object> claims = claimProviders.stream()
                .map(provider -> provider.provideClaims(jwt))
                .filter(Objects::nonNull)
                .flatMap(map -> map.entrySet().stream())
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new
                ));
        GeneratedAdditionalClaims claimView = ClaimProxyFactory.create(GeneratedAdditionalClaims.class, claims);
        return new NextramiceJwtAuthenticationToken(
                jwt,
                authorityProvider.getAuthorities(principalName),
                claimView,
                principalName
        );
    }

    public String getPrincipalName(Jwt jwt) {
        return StringUtils.firstNonBlank(jwt.getClaimAsString("email"), jwt.getClaimAsString("unique_name"), jwt.getClaimAsString("upn"));
    }
}
