package net.themodcraft.nextramice.processor.io;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

public final class GeneratedInterfaceWriter {

    public void write(
            Filer filer,
            String targetFqn,
            CharSequence content,
            Element originatingElement
    ) throws IOException {
        JavaFileObject sourceFile = filer.createSourceFile(targetFqn, originatingElement);
        try (Writer writer = sourceFile.openWriter()) {
            writer.write(content.toString());
        }
    }
}
