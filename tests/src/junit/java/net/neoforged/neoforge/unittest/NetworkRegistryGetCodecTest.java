/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import java.io.IOException;
import java.io.InputStream;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

// Regression coverage for https://github.com/neoforged/NeoForge/issues/3652
public class NetworkRegistryGetCodecTest {
    @Test
    void getCodecAreturnOrdinalThreeIsRegistrationCodec() {
        try (InputStream classBytes = NetworkRegistry.class.getResourceAsStream("/" + NetworkRegistry.class.getName().replace('.', '/') + ".class")) {
            if (classBytes == null) {
                throw new IllegalStateException("Cannot access NetworkRegistry.class bytes");
            }

            GetCodecClassVisitor visitor = new GetCodecClassVisitor();
            new ClassReader(classBytes).accept(visitor, 0);
            Assertions.assertEquals(5, visitor.areturnCount);
            Assertions.assertTrue(visitor.ordinalThreeIsCodecReturn);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final class GetCodecClassVisitor extends ClassVisitor {
        private static final String METHOD_DESC = Type.getMethodDescriptor(
                Type.getType(StreamCodec.class),
                Type.getType(Identifier.class),
                Type.getType(ConnectionProtocol.class),
                Type.getType(PacketFlow.class));

        private int areturnCount;
        private boolean ordinalThreeIsCodecReturn;
        private boolean methodFound;

        GetCodecClassVisitor() {
            super(Opcodes.ASM9);
        }

        @Override
        @Nullable
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if (name.equals("getCodec") && descriptor.equals(METHOD_DESC)) {
                methodFound = true;
                return new MethodVisitor(Opcodes.ASM9) {
                    private boolean codecCalled;

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        codecCalled = name.equals("codec");
                    }

                    @Override
                    public void visitInsn(int opcode) {
                        if (opcode == Opcodes.ARETURN) {
                            if (areturnCount == 3) {
                                ordinalThreeIsCodecReturn = codecCalled;
                            }
                            areturnCount++;
                        }
                    }
                };
            }
            return null;
        }

        @Override
        public void visitEnd() {
            if (!methodFound) {
                throw new IllegalStateException("No getCodec(Identifier, ConnectionProtocol, PacketFlow) method in NetworkRegistry");
            }
        }
    }
}
