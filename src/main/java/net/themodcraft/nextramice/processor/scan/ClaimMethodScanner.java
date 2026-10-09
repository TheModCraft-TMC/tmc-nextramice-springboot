package net.themodcraft.nextramice.processor.scan;

import net.themodcraft.nextramice.processor.model.ClaimSpec;
import net.themodcraft.nextramice.processor.type.TypeResolver;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ClaimMethodScanner {

    private final Messager messager;
    private final TypeResolver typeResolver;

    public ClaimMethodScanner(Messager messager, TypeResolver typeResolver) {
        this.messager = messager;
        this.typeResolver = typeResolver;
    }

    public Map<String, ClaimSpec> scan(TreePath methodPath) {
        Map<String, ClaimSpec> claims = new LinkedHashMap<>();

        new TreePathScanner<Void, Void>() {
            @Override
            public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
                String methodSelect = node.getMethodSelect().toString();
                messager.printMessage(Diagnostic.Kind.NOTE, "Found method call: " + methodSelect);

                if (methodSelect.endsWith("put")) {
                    List<? extends ExpressionTree> args = node.getArguments();
                    if (args.size() == 2) {
                        String key = stripQuotes(args.get(0).toString());
                        String valueType = typeResolver.resolve(args.get(1), getCurrentPath());
                        claims.put(key, new ClaimSpec(key, valueType));
                    }
                }
                return super.visitMethodInvocation(node, unused);
            }
        }.scan(methodPath, null);

        return claims;
    }

    private static String stripQuotes(String text) {
        return text == null ? "" : text.replace("\"", "");
    }
}