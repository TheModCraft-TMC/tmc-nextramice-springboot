package net.themodcraft.nextramice.local.model;

public record NextramiceRegistrationResult(Status status, String userId) {

    public enum Status {
        COMPLETE,
        VERIFICATION_REQUIRED,
        APPROVAL_REQUIRED
    }
}
