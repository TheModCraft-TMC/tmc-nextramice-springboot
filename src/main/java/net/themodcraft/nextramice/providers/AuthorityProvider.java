package net.themodcraft.nextramice.providers;

import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface AuthorityProvider {
    Collection<? extends GrantedAuthority> getAuthorities(@NotNull String email);
}