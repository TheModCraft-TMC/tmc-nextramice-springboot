package net.themodcraft.nextramice.local;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Runs after primary credential verification but before a browser session is established.
 * Applications can use this hook to enforce a completed MFA or step-up challenge.
 */
@FunctionalInterface
public interface NextramiceAuthenticationCompletionPolicy {

    void verify(Authentication authentication, HttpServletRequest request)
            throws AuthenticationException;
}
