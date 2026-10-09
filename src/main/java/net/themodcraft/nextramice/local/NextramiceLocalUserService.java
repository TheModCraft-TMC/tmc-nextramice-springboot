package net.themodcraft.nextramice.local;

import org.springframework.security.core.userdetails.UserDetailsService;

@FunctionalInterface
public interface NextramiceLocalUserService extends UserDetailsService {

    @Override
    NextramiceLocalUser loadUserByUsername(String username);
}
