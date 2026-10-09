package net.themodcraft.nextramice.local.web;

import net.themodcraft.nextramice.local.NextramiceRegistrationService;
import net.themodcraft.nextramice.local.model.NextramiceRegistrationRequest;
import net.themodcraft.nextramice.local.model.NextramiceRegistrationResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("${nextramice.bff.base-path:/nextramice}")
public class NextramiceRegistrationController {

    private final NextramiceRegistrationService registrations;

    public NextramiceRegistrationController(NextramiceRegistrationService registrations) {
        this.registrations = registrations;
    }

    @PostMapping("/register")
    public NextramiceRegistrationResult register(
            @RequestBody NextramiceRegistrationRequest request
    ) {
        if (request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid registration request");
        }
        return registrations.register(request);
    }
}
