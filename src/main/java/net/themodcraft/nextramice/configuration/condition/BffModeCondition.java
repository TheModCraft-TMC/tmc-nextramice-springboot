package net.themodcraft.nextramice.configuration.condition;

import net.themodcraft.nextramice.configuration.NextramiceMode;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public final class BffModeCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        NextramiceMode mode = Binder.get(context.getEnvironment())
                .bind("nextramice.web.mode", NextramiceMode.class)
                .orElse(NextramiceMode.RESOURCE_SERVER);
        boolean matches = mode == NextramiceMode.BFF || mode == NextramiceMode.HYBRID;
        return new ConditionOutcome(matches, "Nextramice web mode is " + mode);
    }
}
