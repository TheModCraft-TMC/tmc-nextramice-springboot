package net.themodcraft.nextramice.local;

import org.springframework.security.core.AuthenticationException;

/** Replaceable rate-limit, lockout, and abuse-protection hook. */
public interface NextramiceLoginAttemptPolicy {

    default void beforeAuthentication(NextramiceLoginAttemptContext context)
            throws AuthenticationException {
    }

    default void authenticationSucceeded(NextramiceLoginAttemptContext context) {
    }

    default void authenticationFailed(
            NextramiceLoginAttemptContext context,
            AuthenticationException failure
    ) {
    }
}
