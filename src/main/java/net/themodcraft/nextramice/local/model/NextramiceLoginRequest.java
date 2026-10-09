package net.themodcraft.nextramice.local.model;

public record NextramiceLoginRequest(String username, String password) {

    @Override
    public String toString() {
        return "NextramiceLoginRequest{username='%s', password=[REDACTED]}".formatted(username);
    }
}
