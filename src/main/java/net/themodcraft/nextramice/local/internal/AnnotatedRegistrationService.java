package net.themodcraft.nextramice.local.internal;

import net.themodcraft.nextramice.local.NextramiceRegistrationService;
import net.themodcraft.nextramice.local.annotation.NextramiceRegister;
import net.themodcraft.nextramice.local.model.NextramiceRegistrationRequest;
import net.themodcraft.nextramice.local.model.NextramiceRegistrationResult;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;

import java.lang.reflect.Method;

public final class AnnotatedRegistrationService
        implements NextramiceRegistrationService, SmartInitializingSingleton {

    private final ListableBeanFactory beanFactory;
    private AnnotatedMethodFinder.FoundMethod method;

    public AnnotatedRegistrationService(ListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterSingletonsInstantiated() {
        method = AnnotatedMethodFinder.findSingle(
                beanFactory,
                NextramiceRegister.class,
                AnnotatedRegistrationService::isValidSignature
        );
    }

    @Override
    public NextramiceRegistrationResult register(NextramiceRegistrationRequest request) {
        if (method == null) {
            throw new IllegalStateException("Nextramice registration method has not been initialized");
        }
        return (NextramiceRegistrationResult) method.invoke(beanFactory, request);
    }

    private static boolean isValidSignature(Method method) {
        return method.getParameterCount() == 1
                && method.getParameterTypes()[0] == NextramiceRegistrationRequest.class
                && NextramiceRegistrationResult.class.isAssignableFrom(method.getReturnType());
    }
}
