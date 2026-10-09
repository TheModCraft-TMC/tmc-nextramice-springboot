package net.themodcraft.nextramice.local;

/** Persists a new adaptive password hash after Spring determines that an upgrade is needed. */
@FunctionalInterface
public interface NextramicePasswordUpgradeService {

    NextramiceLocalUser updatePassword(NextramiceLocalUser user, String newEncodedPassword);
}
