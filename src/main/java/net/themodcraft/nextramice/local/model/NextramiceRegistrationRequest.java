package net.themodcraft.nextramice.local.model;

import java.util.Map;

public record NextramiceRegistrationRequest(
        String username,
        String password,
        Map<String, Object> attributes
) {
    public NextramiceRegistrationRequest {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    @Override
    public String toString() {
        return "NextramiceRegistrationRequest{username='%s', password=[REDACTED], attributeKeys=%s}"
                .formatted(username, attributes.keySet());
    }
}
