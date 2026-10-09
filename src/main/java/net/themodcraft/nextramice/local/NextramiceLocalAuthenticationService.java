package net.themodcraft.nextramice.local;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.context.ApplicationEventPublisher;
import net.themodcraft.nextramice.local.event.NextramiceLoginFailedEvent;
import net.themodcraft.nextramice.local.event.NextramiceLoginSucceededEvent;

import java.util.List;

public class NextramiceLocalAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final SessionAuthenticationStrategy sessionStrategy;
    private final SecurityContextRepository securityContextRepository;
    private final NextramiceLoginAttemptPolicy attemptPolicy;
    private final List<NextramiceAuthenticationCompletionPolicy> completionPolicies;
    private final ApplicationEventPublisher events;

    public NextramiceLocalAuthenticationService(
            AuthenticationManager authenticationManager,
            SessionAuthenticationStrategy sessionStrategy,
            SecurityContextRepository securityContextRepository,
            NextramiceLoginAttemptPolicy attemptPolicy,
            List<NextramiceAuthenticationCompletionPolicy> completionPolicies,
            ApplicationEventPublisher events
    ) {
        this.authenticationManager = authenticationManager;
        this.sessionStrategy = sessionStrategy;
        this.securityContextRepository = securityContextRepository;
        this.attemptPolicy = attemptPolicy;
        this.completionPolicies = List.copyOf(completionPolicies);
        this.events = events;
    }

    public Authentication authenticate(
            String username,
            String password,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        NextramiceLoginAttemptContext attempt = new NextramiceLoginAttemptContext(
                username,
                request.getRemoteAddr()
        );
        try {
            attemptPolicy.beforeAuthentication(attempt);
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, password)
            );
            for (NextramiceAuthenticationCompletionPolicy policy : completionPolicies) {
                policy.verify(authentication, request);
            }
            sessionStrategy.onAuthentication(authentication, request, response);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);
            attemptPolicy.authenticationSucceeded(attempt);
            events.publishEvent(new NextramiceLoginSucceededEvent(username, request.getRemoteAddr()));
            return authentication;
        } catch (AuthenticationException failure) {
            attemptPolicy.authenticationFailed(attempt, failure);
            events.publishEvent(new NextramiceLoginFailedEvent(
                    username,
                    request.getRemoteAddr(),
                    failure.getClass().getSimpleName()
            ));
            throw failure;
        }
    }
}
