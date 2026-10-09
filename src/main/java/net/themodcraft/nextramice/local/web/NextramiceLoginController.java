package net.themodcraft.nextramice.local.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.themodcraft.nextramice.bff.NextramiceSessionService;
import net.themodcraft.nextramice.bff.model.NextramiceSession;
import net.themodcraft.nextramice.local.NextramiceLocalAuthenticationService;
import net.themodcraft.nextramice.local.model.NextramiceLoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("${nextramice.bff.base-path:/nextramice}")
public class NextramiceLoginController {

    private final NextramiceLocalAuthenticationService authenticator;
    private final NextramiceSessionService sessions;

    public NextramiceLoginController(
            NextramiceLocalAuthenticationService authenticator,
            NextramiceSessionService sessions
    ) {
        this.authenticator = authenticator;
        this.sessions = sessions;
    }

    @PostMapping("/login")
    public NextramiceSession login(
            @RequestBody NextramiceLoginRequest credentials,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (credentials.username() == null || credentials.username().isBlank()
                || credentials.password() == null || credentials.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        }
        try {
            Authentication authentication = authenticator.authenticate(
                    credentials.username(), credentials.password(), request, response
            );
            return sessions.create(authentication);
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        }
    }
}
