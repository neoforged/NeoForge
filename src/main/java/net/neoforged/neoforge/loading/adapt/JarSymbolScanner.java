/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.loading.adapt;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Scans a mod JAR (or class folder) and collects the set of symbols it references.
 *
 * <p>References are reported in two forms:
 * <ul>
 * <li>{@code owner.name} for method and field references (including invokedynamic bootstrap handles),</li>
 * <li>{@code owner} for type references ({@code NEW}, {@code CHECKCAST}, annotations-free descriptors, etc.).</li>
 * </ul>
 * This is the input for {@link BreakingChangesDatabase} lookups, and the expensive step that is
 * cached per-file by {@link net.neoforged.neoforge.loading.cache.ModIndexCache}.</p>
 */
@ApiStatus.Internal
final class JarSymbolScanner {
    private JarSymbolScanner() {}

    /** Scans a JAR or class directory for referenced symbols. Never throws for a single unreadable class. */
    static Set<String> scan(Path file) {
        Set<String> symbols = new HashSet<>();
        if (Files.isDirectory(file)) {
            try (Stream<Path> stream = Files.walk(file)) {
                stream.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".class")).forEach(p -> scanClass(readAll(p), symbols));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            try (JarFile jar = new JarFile(file.toFile())) {
                jar.stream()
                        .filter(e -> e.getName().endsWith(".class"))
                        .filter(e -> !e.getName().startsWith("META-INF/"))
                        .filter(e -> !e.getName().equals("module-info.class"))
                        .forEach(e -> scanClass(readEntry(jar, e), symbols));
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to scan mod file " + file, e);
            }
        }
        return symbols;
    }

    private static byte[] readEntry(JarFile jar, JarEntry entry) {
        try (InputStream in = jar.getInputStream(entry)) {
            return in.readAllBytes();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    private static byte[] readAll(Path file) {
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            return new byte[0];
        }
    }

    private static void scanClass(byte[] bytes, Set<String> symbols) {
        if (bytes.length == 0) {
            return;
        }
        try {
            ClassReader reader = new ClassReader(bytes);
            reader.accept(new SymbolCollector(symbols), ClassReader.SKIP_FRAMES);
        } catch (RuntimeException e) {
            // A single unreadable class must never break the precheck; the mod loader handles real failures.
        }
    }

    private static final class SymbolCollector extends ClassVisitor {
        private final Set<String> symbols;

        private SymbolCollector(Set<String> symbols) {
            super(Opcodes.ASM9);
            this.symbols = symbols;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            addType(superName);
            if (interfaces != null) {
                for (String iface : interfaces) {
                    addType(iface);
                }
            }
        }

        @Override
        public org.objectweb.asm.FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            addType(Type.getType(descriptor));
            return null;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            Type methodType = Type.getMethodType(descriptor);
            addType(methodType.getReturnType());
            for (Type argument : methodType.getArgumentTypes()) {
                addType(argument);
            }
            return new MethodVisitor(Opcodes.ASM9) {
                @Override
                public void visitTypeInsn(int opcode, String type) {
                    addType(type);
                }

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                    add(owner + "." + name);
                    add(owner);
                    addType(Type.getType(descriptor));
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    add(owner + "." + name);
                    add(owner);
                    addType(Type.getMethodType(descriptor).getReturnType());
                }

                @Override
                public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                    add(bootstrapMethodHandle.getOwner() + "." + bootstrapMethodHandle.getName());
                    for (Object argument : bootstrapMethodArguments) {
                        if (argument instanceof Handle handle) {
                            add(handle.getOwner() + "." + handle.getName());
                        } else if (argument instanceof Type type) {
                            addType(type);
                        }
                    }
                }

                @Override
                public void visitLdcInsn(Object value) {
                    if (value instanceof Type type) {
                        addType(type);
                    }
                }

                @Override
                public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
                    addType(Type.getType(descriptor));
                }

                @Override
                public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                    addType(type);
                }

                @Override
                public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                    addType(Type.getType(descriptor));
                }
            };
        }

        private void addType(String internalName) {
            if (internalName != null) {
                add(internalName);
            }
        }

        private void addType(Type type) {
            if (type.getSort() == Type.OBJECT) {
                add(type.getInternalName());
            } else if (type.getSort() == Type.ARRAY) {
                addType(type.getElementType());
            } else if (type.getSort() == Type.METHOD) {
                addType(type.getReturnType());
                for (Type argument : type.getArgumentTypes()) {
                    addType(argument);
                }
            }
        }

        private void add(String symbol) {
            symbols.add(symbol);
        }
    }
}
