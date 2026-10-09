package net.themodcraft.nextramice.processor.render;

import net.themodcraft.nextramice.processor.model.ClaimSpec;

import java.util.Map;

public final class GeneratedInterfaceRenderer {

    public String render(Map<String, ClaimSpec> claimsMap) {
        StringBuilder sb = new StringBuilder();

        sb.append("package net.themodcraft.nextramice.interfaces;\n\n");
        sb.append("public interface GeneratedAdditionalClaims extends AdditionalClaims {\n");

        for (ClaimSpec claim : claimsMap.values()) {
            String methodName = "get" + Character.toUpperCase(claim.key().charAt(0)) + claim.key().substring(1);
            sb.append("    ").append(claim.javaTypeFqn()).append(" ").append(methodName).append("();\n");
        }

        sb.append("}\n");
        return sb.toString();
    }
}