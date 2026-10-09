package net.themodcraft.nextramice.interfaces;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface ClaimDefinition {
    String name();

    Class<?> type();
}
