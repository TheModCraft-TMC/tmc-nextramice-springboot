package net.themodcraft.nextramice.bff.web;

import net.themodcraft.nextramice.bff.NextramiceSessionService;
import net.themodcraft.nextramice.bff.model.NextramiceSession;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${nextramice.bff.base-path:/nextramice}")
public class NextramiceSessionController {

    private final NextramiceSessionService sessions;

    public NextramiceSessionController(NextramiceSessionService sessions) {
        this.sessions = sessions;
    }

    @GetMapping("/session")
    public NextramiceSession session(Authentication authentication) {
        return sessions.create(authentication);
    }
}
