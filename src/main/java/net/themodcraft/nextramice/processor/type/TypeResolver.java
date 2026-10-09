package net.themodcraft.nextramice.processor.type;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.stream.Collectors;

public final class TypeResolver {

    private final Trees trees;

    public TypeResolver(Trees trees) {
        this.trees = trees;
    }

    public String resolve(ExpressionTree expr, TreePath invocationPath) {
        if (expr == null || invocationPath == null || trees == null) {
            return "java.lang.Object";
        }

        String resolved = resolveFromTypeMirror(expr, invocationPath);
        if (!"java.lang.Object".equals(resolved)) {
            return resolved;
        }

        resolved = resolveFromIdentifierVariable(expr, invocationPath);
        if (!"java.lang.Object".equals(resolved)) {
            return resolved;
        }

        resolved = resolveFromVariableNameScan(expr, invocationPath);
        if (!"java.lang.Object".equals(resolved)) {
            return resolved;
        }

        resolved = resolveFromMethodInvocation(expr, invocationPath);
        if (!"java.lang.Object".equals(resolved)) {
            return resolved;
        }

        resolved = resolveFromStringHeuristics(expr);
        if (!"java.lang.Object".equals(resolved)) {
            return resolved;
        }

        return "java.lang.Object";
    }

    private String resolveFromTypeMirror(ExpressionTree expr, TreePath invocationPath) {
        TypeMirror tm = trees.getTypeMirror(new TreePath(invocationPath, expr));
        if (tm != null && tm.getKind() != TypeKind.ERROR) {
            String resolved = toTypeName(tm);
            if (!"java.lang.Object".equals(resolved)) {
                return resolved;
            }
        }
        return "java.lang.Object";
    }

    private String resolveFromIdentifierVariable(ExpressionTree expr, TreePath invocationPath) {
        if (expr instanceof IdentifierTree id) {
            Element el = trees.getElement(new TreePath(invocationPath, id));
            if (el instanceof VariableElement ve) {
                TypeMirror asType = ve.asType();
                if (asType != null && asType.getKind() != TypeKind.ERROR) {
                    String resolved = toTypeName(asType);
                    if (!"java.lang.Object".equals(resolved)) {
                        return resolved;
                    }
                }
            }
        }
        return "java.lang.Object";
    }

    private String resolveFromVariableNameScan(ExpressionTree expr, TreePath invocationPath) {
        String exprStr = expr.toString();
        if (!exprStr.contains("(") && !exprStr.contains(".")) {
            String fromVarScan = findVariableType(exprStr.trim(), invocationPath);
            if (!"java.lang.Object".equals(fromVarScan)) {
                return fromVarScan;
            }
        }
        return "java.lang.Object";
    }

    private String resolveFromMethodInvocation(ExpressionTree expr, TreePath invocationPath) {
        if (expr instanceof MethodInvocationTree mit) {
            Element el = trees.getElement(new TreePath(invocationPath, mit.getMethodSelect()));
            if (el instanceof ExecutableElement ee) {
                TypeMirror rt = ee.getReturnType();
                if (rt.getKind() != TypeKind.ERROR) {
                    String resolved = toTypeName(rt);
                    if (!"java.lang.Object".equals(resolved)) {
                        return resolved;
                    }
                }
            }
        }
        return "java.lang.Object";
    }

    private String resolveFromStringHeuristics(ExpressionTree expr) {
        String exprStr = expr.toString();
        if (exprStr.contains("getClaimAsString") || exprStr.contains("firstNonBlank")) {
            return "java.lang.String";
        }
        return "java.lang.Object";
    }

    private String findVariableType(String varName, TreePath methodPath) {
        final String[] foundType = {"java.lang.Object"};

        if (varName == null || varName.isBlank() || methodPath == null || trees == null) {
            return foundType[0];
        }

        new TreePathScanner<Void, Void>() {
            @Override
            public Void visitVariable(VariableTree node, Void unused) {
                if (node != null && node.getName() != null && node.getName().contentEquals(varName)) {
                    TreePath varPath = getCurrentPath();
                    if (varPath != null && node.getType() != null) {
                        TreePath typePath = new TreePath(varPath, node.getType());
                        TypeMirror tm = trees.getTypeMirror(typePath);
                        if (tm != null) {
                            foundType[0] = toTypeName(tm);
                        }
                    }
                }
                return super.visitVariable(node, unused);
            }
        }.scan(methodPath, null);

        return foundType[0];
    }

    private String toTypeName(TypeMirror tm) {
        if (tm.getKind().isPrimitive()) {
            return tm.toString();
        }

        if (tm instanceof DeclaredType dt) {
            TypeElement el = (TypeElement) dt.asElement();
            String raw = el.getQualifiedName().toString();

            if (!dt.getTypeArguments().isEmpty()) {
                String args = dt.getTypeArguments().stream().map(this::toTypeName).collect(Collectors.joining(", "));
                return raw + "<" + args + ">";
            }
            return raw;
        }

        return tm.toString();
    }
}