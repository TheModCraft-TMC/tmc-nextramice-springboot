package net.themodcraft.nextramice.bff.web;

import net.themodcraft.nextramice.bff.model.NextramiceProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("${nextramice.bff.base-path:/nextramice}")
public class NextramiceProviderController {

    private final ObjectProvider<ClientRegistrationRepository> registrations;

    public NextramiceProviderController(
            ObjectProvider<ClientRegistrationRepository> registrations
    ) {
        this.registrations = registrations;
    }

    @GetMapping("/providers")
    public List<NextramiceProvider> providers() {
        ClientRegistrationRepository repository = registrations.getIfAvailable();
        if (!(repository instanceof Iterable<?> iterable)) {
            return List.of();
        }

        List<NextramiceProvider> providers = new ArrayList<>();
        for (Object value : iterable) {
            if (value instanceof ClientRegistration registration) {
                String id = registration.getRegistrationId();
                providers.add(new NextramiceProvider(
                        id,
                        registration.getClientName(),
                        "/oauth2/authorization/" + id
                ));
            }
        }
        providers.sort(Comparator.comparing(NextramiceProvider::id));
        return List.copyOf(providers);
    }
}
