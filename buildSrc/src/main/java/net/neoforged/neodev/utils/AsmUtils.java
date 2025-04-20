package net.neoforged.neodev.utils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public final class AsmUtils {
    private AsmUtils() {}

    public static void visitAllClasses(File jarFile, ClassVisitor visitor, int parsingOptions) throws IOException {
        try (var zip = new ZipFile(jarFile)) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                var next = entries.nextElement();
                if (next.isDirectory() || !next.getName().endsWith(".class")) continue;

                try (var in = zip.getInputStream(next)) {
                    var reader = new ClassReader(in);
                    reader.accept(visitor, parsingOptions);
                }
            }
        }
    }
}
