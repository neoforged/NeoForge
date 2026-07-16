/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.common.tooltip.ItemTooltipHandler;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

@ExtendWith(EphemeralTestServerProvider.class)
public class ComponentTooltipOrderTest {
    @Test
    void testComponentTooltipOrder() {
        List<DataComponentType<?>> registeredOrder = ItemTooltipHandler.getVanillaAppenderOrder();
        List<DataComponentType<?>> vanillaOrder = collectVanillaOrder();

        Assertions.assertEquals(registeredOrder.size(), vanillaOrder.size(), () -> {
            String vanillaTypeList = vanillaOrder.stream()
                    .filter(component -> !registeredOrder.contains(component))
                    .map(DataComponentType::toString)
                    .collect(Collectors.joining(", "));
            String registeredTypeList = registeredOrder.stream()
                    .filter(component -> !vanillaOrder.contains(component))
                    .map(DataComponentType::toString)
                    .collect(Collectors.joining(", "));
            if (!vanillaTypeList.isEmpty() && !registeredTypeList.isEmpty()) {
                return String.format(Locale.ROOT, "Tooltip data component lists don't match\nMissing Vanilla: %s\nExtra Registered: %s", vanillaTypeList, registeredTypeList);
            } else if (vanillaTypeList.isEmpty()) {
                return String.format(Locale.ROOT, "Registered tooltip data components have the following extra entries: %s", registeredTypeList);
            }
            return String.format(Locale.ROOT, "Registered tooltip data components are missing the following vanilla entries: %s", vanillaTypeList);
        });
        assertOrder(vanillaOrder, registeredOrder);
    }

    private static void assertOrder(List<DataComponentType<?>> vanillaTypes, List<DataComponentType<?>> registeredTypes) {
        for (int i = 0; i < vanillaTypes.size(); i++) {
            DataComponentType<?> type = vanillaTypes.get(i);
            int finalI = i;
            Assertions.assertEquals(i, registeredTypes.indexOf(type), () -> String.format(
                    Locale.ROOT,
                    "Expected '%s' at index %d, got '%s'",
                    type,
                    finalI,
                    registeredTypes.get(finalI)));
        }
    }

    // Inspired by component scraping implementation in https://github.com/FabricMC/fabric-api/pull/4587.
    private static List<DataComponentType<?>> collectVanillaOrder() {
        try (InputStream classBytes = ItemStack.class.getResourceAsStream("/" + ItemStack.class.getName().replace('.', '/') + ".class")) {
            if (classBytes == null) {
                throw new IllegalStateException("Cannot access ItemStack.class bytes");
            }

            ItemStackClassVisitor visitor = new ItemStackClassVisitor();
            new ClassReader(classBytes).accept(visitor, 0);
            return visitor.vanillaTypes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final class ItemStackClassVisitor extends ClassVisitor {
        private static final String TOOLTIP_METHOD_NAME = "addDetailsToTooltipComponents";
        private static final String TOOLTIP_METHOD_DESC = Type.getMethodDescriptor(
                Type.VOID_TYPE,
                Type.getType(Item.TooltipContext.class),
                Type.getType(TooltipDisplay.class),
                Type.getType(Player.class),
                Type.getType(TooltipFlag.class),
                Type.getType(Consumer.class));

        private final List<DataComponentType<?>> vanillaTypes = new ArrayList<>();
        private boolean methodFound = false;

        ItemStackClassVisitor() {
            super(Opcodes.ASM9);
        }

        @Override
        @Nullable
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if (name.equals(TOOLTIP_METHOD_NAME) && descriptor.equals(TOOLTIP_METHOD_DESC)) {
                methodFound = true;
                return new TooltipMethodVisitor(vanillaTypes);
            }
            return null;
        }

        @Override
        public void visitEnd() {
            if (!methodFound) {
                throw new IllegalStateException("No addDetailsToTooltipComponents() method in ItemStack");
            }
            if (vanillaTypes.isEmpty()) {
                throw new IllegalStateException("Found no component types in addDetailsToTooltipComponents() method");
            }
        }
    }

    private static final class TooltipMethodVisitor extends MethodVisitor {
        private static final String ATTRIB_MOD_METHOD_NAME = "addAttributeTooltips";
        private static final String ATTRIB_MOD_METHOD_DESC = Type.getMethodDescriptor(
                Type.VOID_TYPE,
                Type.getType(Consumer.class),
                Type.getType(TooltipDisplay.class),
                Type.getType(Player.class));
        private static final String TYPE_FIELD_OWNER = Type.getInternalName(DataComponents.class);
        private static final String TYPE_FIELD_DESC = Type.getDescriptor(DataComponentType.class);

        private final Set<String> encounteredComponents = new HashSet<>();
        private final List<DataComponentType<?>> vanillaTypes;

        TooltipMethodVisitor(List<DataComponentType<?>> vanillaTypes) {
            super(Opcodes.ASM9);
            this.vanillaTypes = vanillaTypes;
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            if (opcode == Opcodes.GETSTATIC && owner.equals(TYPE_FIELD_OWNER) && descriptor.equals(TYPE_FIELD_DESC)) {
                if (!encounteredComponents.add(name)) {
                    return;
                }

                try {
                    vanillaTypes.add((DataComponentType<?>) DataComponents.class.getField(name).get(null));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            // Attribute modifier tooltips are handled by a separate method
            if (name.equals(ATTRIB_MOD_METHOD_NAME) && descriptor.equals(ATTRIB_MOD_METHOD_DESC)) {
                vanillaTypes.add(DataComponents.ATTRIBUTE_MODIFIERS);
            }
        }
    }
}
