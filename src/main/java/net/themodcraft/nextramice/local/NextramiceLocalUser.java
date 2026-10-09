package net.themodcraft.nextramice.local;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class NextramiceLocalUser implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String username;
    private final String displayName;
    private final String password;
    private final List<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;

    public NextramiceLocalUser(
            String id,
            String username,
            String displayName,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            boolean enabled,
            boolean accountNonExpired,
            boolean accountNonLocked,
            boolean credentialsNonExpired
    ) {
        this.id = Objects.requireNonNull(id);
        this.username = Objects.requireNonNull(username);
        this.displayName = displayName;
        this.password = Objects.requireNonNull(password);
        this.authorities = List.copyOf(authorities);
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public static NextramiceLocalUser enabled(
            String id,
            String username,
            String displayName,
            String password,
            Collection<? extends GrantedAuthority> authorities
    ) {
        return new NextramiceLocalUser(
                id, username, displayName, password, authorities, true, true, true, true
        );
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "NextramiceLocalUser{id='%s', username='%s', password=[REDACTED]}"
                .formatted(id, username);
    }
}
