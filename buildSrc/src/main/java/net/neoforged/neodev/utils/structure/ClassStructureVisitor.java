package net.neoforged.neodev.utils.structure;

import org.apache.commons.lang3.mutable.MutableInt;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

public final class ClassStructureVisitor extends ClassVisitor {
    private final Map<String, ClassInfo> classes;
    ClassInfo current;

    private ClassStructureVisitor(Map<String, ClassInfo> classes) {
        super(Opcodes.ASM9);
        this.classes = classes;
    }

    public static Map<String, ClassInfo> readJar(File file) throws IOException {
        var map = new HashMap<String, ClassInfo>();
        var visitor = new ClassStructureVisitor(map);
        try (var zip = new ZipFile(file)) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                var next = entries.nextElement();
                if (next.isDirectory() || !next.getName().endsWith(".class")) continue;

                try (var in = zip.getInputStream(next)) {
                    var reader = new ClassReader(in);
                    reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                }
            }
        }
        return map;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        current = getClass(name);
        current.access().setValue(access);
        if (superName != null) {
            current.parents().add(getClass(superName));
        }
        for (String iface : interfaces) {
            current.parents().add(getClass(iface));
        }
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        if (name.equals(current.name())) {
            current.access().setValue(access);
        }
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        current.fields().add(new FieldInfo(name, getClass(Type.getType(descriptor).getInternalName()), access));
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        current.addMethod(name, descriptor, access);
        return null;
    }

    private ClassInfo getClass(String name) {
        var existing = classes.get(name);
        if (existing != null) return existing;
        existing = new ClassInfo(name, new MutableInt(0), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        classes.put(name, existing);
        return existing;
    }
}
