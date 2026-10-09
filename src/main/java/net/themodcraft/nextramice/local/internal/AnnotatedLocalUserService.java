package net.themodcraft.nextramice.local.internal;

import net.themodcraft.nextramice.local.NextramiceLocalUser;
import net.themodcraft.nextramice.local.NextramiceLocalUserService;
import net.themodcraft.nextramice.local.annotation.NextramiceLogin;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;

import java.lang.reflect.Method;

public final class AnnotatedLocalUserService
        implements NextramiceLocalUserService, SmartInitializingSingleton {

    private final ListableBeanFactory beanFactory;
    private AnnotatedMethodFinder.FoundMethod method;

    public AnnotatedLocalUserService(ListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterSingletonsInstantiated() {
        method = AnnotatedMethodFinder.findSingle(
                beanFactory,
                NextramiceLogin.class,
                AnnotatedLocalUserService::isValidSignature
        );
    }

    @Override
    public NextramiceLocalUser loadUserByUsername(String username) {
        if (method == null) {
            throw new IllegalStateException("Nextramice login method has not been initialized");
        }
        return (NextramiceLocalUser) method.invoke(beanFactory, username);
    }

    private static boolean isValidSignature(Method method) {
        return method.getParameterCount() == 1
                && method.getParameterTypes()[0] == String.class
                && NextramiceLocalUser.class.isAssignableFrom(method.getReturnType());
    }
}
