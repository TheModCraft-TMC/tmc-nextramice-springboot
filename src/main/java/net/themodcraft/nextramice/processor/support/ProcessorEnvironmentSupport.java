package net.themodcraft.nextramice.processor.support;

import com.sun.source.util.Trees;

import javax.annotation.processing.ProcessingEnvironment;

public final class ProcessorEnvironmentSupport {

    private ProcessorEnvironmentSupport() {
    }

    public static Trees initTrees(ProcessingEnvironment processingEnv) {
        ProcessingEnvironment unwrapped = jbUnwrap(ProcessingEnvironment.class, processingEnv);
        return Trees.instance(unwrapped);
    }

    @SuppressWarnings("unchecked")
    public static <T> T jbUnwrap(Class<? extends T> iface, T wrapper) {
        T unwrapped = null;
        try {
            Class<?> apiWrappers = wrapper.getClass().getClassLoader()
                    .loadClass("org.jetbrains.jps.javac.APIWrappers");
            var unwrapMethod = apiWrappers.getDeclaredMethod("unwrap", Class.class, Object.class);
            unwrapped = iface.cast(unwrapMethod.invoke(null, iface, wrapper));
        } catch (Throwable _) {
            // Not running in IntelliJ JPS wrapper.
        }
        return unwrapped != null ? unwrapped : wrapper;
    }
}