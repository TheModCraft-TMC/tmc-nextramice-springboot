package net.themodcraft.nextramice.providers;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public class DefaultAuthorityProvider implements AuthorityProvider {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(String email) {
        return List.of();
    }
}
