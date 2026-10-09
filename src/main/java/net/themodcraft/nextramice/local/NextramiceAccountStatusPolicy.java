package net.themodcraft.nextramice.local;

import org.springframework.security.core.AuthenticationException;

/** Adds application-specific account eligibility checks after Spring's standard checks. */
@FunctionalInterface
public interface NextramiceAccountStatusPolicy {

    void check(NextramiceLocalUser user) throws AuthenticationException;
}
