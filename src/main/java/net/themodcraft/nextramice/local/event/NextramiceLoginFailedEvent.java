package net.themodcraft.nextramice.local.event;

public record NextramiceLoginFailedEvent(
        String username,
        String remoteAddress,
        String failureType
) {
}
