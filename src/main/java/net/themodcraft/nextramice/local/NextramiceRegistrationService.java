package net.themodcraft.nextramice.local;

import net.themodcraft.nextramice.local.model.NextramiceRegistrationRequest;
import net.themodcraft.nextramice.local.model.NextramiceRegistrationResult;

@FunctionalInterface
public interface NextramiceRegistrationService {
    NextramiceRegistrationResult register(NextramiceRegistrationRequest request);
}
