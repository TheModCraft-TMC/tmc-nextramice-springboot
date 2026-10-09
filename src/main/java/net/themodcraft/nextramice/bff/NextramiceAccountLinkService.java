package net.themodcraft.nextramice.bff;

import java.util.Optional;

/** Resolves a validated external issuer/subject pair to an application-owned identity. */
@FunctionalInterface
public interface NextramiceAccountLinkService {

    Optional<String> findLocalUserId(String issuer, String subject);
}
