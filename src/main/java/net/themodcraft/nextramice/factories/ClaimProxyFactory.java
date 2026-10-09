package net.themodcraft.nextramice.factories;

import java.lang.reflect.Proxy;
import java.util.Map;

public final class ClaimProxyFactory {

    private ClaimProxyFactory() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<?> type, Map<String, Object> claims) {

        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> {

                    // Handle Object methods
                    switch (method.getName()) {
                        case "toString":
                            return type.getSimpleName() + claims;
                        case "hashCode":
                            return claims.hashCode();
                        case "equals":
                            return proxy == args[0];
                    }

                    String name = method.getName();


                    if (name.startsWith("get")) {
                        String key =
                                Character.toLowerCase(name.charAt(3))
                                        + name.substring(4);

                        return claims.get(key);
                    }

                    throw new UnsupportedOperationException(
                            "Unsupported method: " + name);
                });
    }
}