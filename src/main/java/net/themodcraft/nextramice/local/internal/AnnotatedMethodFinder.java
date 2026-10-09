package net.themodcraft.nextramice.local.internal;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

final class AnnotatedMethodFinder {

    private AnnotatedMethodFinder() {
    }

    static FoundMethod findSingle(
            ListableBeanFactory beanFactory,
            Class<? extends Annotation> annotation,
            Predicate<Method> signature
    ) {
        List<FoundMethod> found = new ArrayList<>();
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            Class<?> type = beanFactory.getType(beanName, false);
            if (type == null) {
                continue;
            }
            ReflectionUtils.doWithMethods(type, method -> {
                if (AnnotatedElementUtils.hasAnnotation(method, annotation)) {
                    if (!Modifier.isPublic(method.getModifiers())
                            || Modifier.isStatic(method.getModifiers())
                            || !signature.test(method)) {
                        throw new IllegalStateException("Invalid @" + annotation.getSimpleName()
                                + " method signature: " + type.getName() + "#" + method.getName());
                    }
                    found.add(new FoundMethod(beanName, method));
                }
            });
        }

        if (found.size() != 1) {
            throw new IllegalStateException("Expected exactly one @" + annotation.getSimpleName()
                    + " method, found " + found.size());
        }
        return found.getFirst();
    }

    record FoundMethod(String beanName, Method method) {
        Object invoke(ListableBeanFactory beanFactory, Object argument) {
            Object bean = beanFactory.getBean(beanName);
            Method invocable = AopUtils.selectInvocableMethod(method, bean.getClass());
            ReflectionUtils.makeAccessible(invocable);
            return ReflectionUtils.invokeMethod(invocable, bean, argument);
        }
    }
}
