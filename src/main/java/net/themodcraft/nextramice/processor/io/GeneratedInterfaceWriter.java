package net.themodcraft.nextramice.processor.io;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

public final class GeneratedInterfaceWriter {

    private static final String TARGET_FQN = "net.themodcraft.nextramice.interfaces.GeneratedAdditionalClaims";

    public void write(Filer filer, CharSequence content) throws IOException {
        try {
            JavaFileObject sourceFile = filer.createSourceFile(TARGET_FQN);
            try (Writer writer = sourceFile.openWriter()) {
                writer.write(content.toString());
            }
        } catch (FilerException _) {
            // Already generated in this round/run.
        }
    }
}