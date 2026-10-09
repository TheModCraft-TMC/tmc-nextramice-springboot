package net.themodcraft.nextramice.processor;

import net.themodcraft.nextramice.interfaces.GenerateClaimView;
import net.themodcraft.nextramice.processor.io.GeneratedInterfaceWriter;
import net.themodcraft.nextramice.processor.render.GeneratedInterfaceRenderer;
import net.themodcraft.nextramice.processor.scan.ClaimMethodScanner;
import net.themodcraft.nextramice.processor.support.ProcessorEnvironmentSupport;
import net.themodcraft.nextramice.processor.type.TypeResolver;
import com.google.auto.service.AutoService;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_25)
@SupportedAnnotationTypes("net.themodcraft.nextramice.interfaces.GenerateClaimView")
public class ClaimViewProcessor extends AbstractProcessor {

    private Trees trees;
    private GeneratedInterfaceRenderer renderer;
    private GeneratedInterfaceWriter writer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.trees = ProcessorEnvironmentSupport.initTrees(processingEnv);
        this.renderer = new GeneratedInterfaceRenderer();
        this.writer = new GeneratedInterfaceWriter();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateClaimView.class)) {
            if (!(element instanceof ExecutableElement method)) {
                continue;
            }

            try {
                TreePath path = trees.getPath(method);
                if (path == null || !(path.getLeaf() instanceof MethodTree)) {
                    continue;
                }

                TypeResolver typeResolver = new TypeResolver(trees);
                ClaimMethodScanner scanner = new ClaimMethodScanner(processingEnv.getMessager(), typeResolver);

                var claims = scanner.scan(path);
                String source = renderer.render(claims);
                writer.write(processingEnv.getFiler(), source);

            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error: " + e.getMessage(), element);
            }
        }

        return true;
    }
}