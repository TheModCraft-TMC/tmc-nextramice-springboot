package net.themodcraft.nextramice.processor;

import net.themodcraft.nextramice.interfaces.ClaimDefinition;
import net.themodcraft.nextramice.interfaces.GenerateClaimView;
import net.themodcraft.nextramice.processor.io.GeneratedInterfaceWriter;
import net.themodcraft.nextramice.processor.model.ClaimSpec;
import net.themodcraft.nextramice.processor.render.GeneratedInterfaceRenderer;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_25)
@SupportedAnnotationTypes("net.themodcraft.nextramice.interfaces.GenerateClaimView")
public class ClaimViewProcessor extends AbstractProcessor {

    private GeneratedInterfaceRenderer renderer;
    private GeneratedInterfaceWriter writer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.renderer = new GeneratedInterfaceRenderer();
        this.writer = new GeneratedInterfaceWriter();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateClaimView.class)) {
            if (!(element instanceof ExecutableElement)) {
                error("@GenerateClaimView can only be used on a method", element);
                continue;
            }

            GenerateClaimView definition = element.getAnnotation(GenerateClaimView.class);
            if (!isSimpleTypeName(definition.name())) {
                error("Claim view name must be a valid Java type name", element);
                continue;
            }

            try {
                List<ClaimSpec> claims = claimSpecs(definition, element);
                PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(element);
                String packageName = packageElement.getQualifiedName().toString();
                String targetFqn = packageName + "." + definition.name();
                String source = renderer.render(packageName, definition.name(), claims);
                writer.write(processingEnv.getFiler(), targetFqn, source, element);
            } catch (IOException exception) {
                error("Unable to generate claim view: " + exception.getMessage(), element);
            }
        }
        return true;
    }

    private List<ClaimSpec> claimSpecs(GenerateClaimView definition, Element element) {
        List<ClaimSpec> claims = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        for (ClaimDefinition claim : definition.claims()) {
            if (!isPropertyName(claim.name())) {
                error("Claim name must be a lower-camel-case Java property name: " + claim.name(), element);
                continue;
            }
            if (!keys.add(claim.name())) {
                error("Duplicate claim name: " + claim.name(), element);
                continue;
            }
            claims.add(new ClaimSpec(claim.name(), typeName(claim)));
        }
        return List.copyOf(claims);
    }

    private static String typeName(ClaimDefinition claim) {
        try {
            return claim.type().getCanonicalName();
        } catch (MirroredTypeException exception) {
            return exception.getTypeMirror().toString();
        }
    }

    private void error(String message, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private static boolean isSimpleTypeName(String value) {
        return value != null
                && SourceVersion.isIdentifier(value)
                && !SourceVersion.isKeyword(value);
    }

    private static boolean isPropertyName(String value) {
        return isSimpleTypeName(value) && Character.isLowerCase(value.charAt(0));
    }
}
