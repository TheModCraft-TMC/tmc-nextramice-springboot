package net.themodcraft.nextramice.processor.render;

import net.themodcraft.nextramice.processor.model.ClaimSpec;

import java.util.List;

public final class GeneratedInterfaceRenderer {

    public String render(String packageName, String interfaceName, List<ClaimSpec> claims) {
        StringBuilder sb = new StringBuilder();

        sb.append("package ").append(packageName).append(";\n\n");
        sb.append("public interface ").append(interfaceName)
                .append(" extends net.themodcraft.nextramice.interfaces.AdditionalClaims {\n");

        for (ClaimSpec claim : claims) {
            String methodName = "get" + Character.toUpperCase(claim.key().charAt(0)) + claim.key().substring(1);
            sb.append("    ").append(claim.javaTypeFqn()).append(" ").append(methodName).append("();\n");
        }

        sb.append("}\n");
        return sb.toString();
    }
}
