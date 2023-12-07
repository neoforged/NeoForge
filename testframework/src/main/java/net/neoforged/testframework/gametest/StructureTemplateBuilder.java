/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.gametest;

import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.testframework.impl.ReflectionUtils;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StructureTemplateBuilder implements TemplateBuilderHelper<StructureTemplateBuilder> {
    private static final FieldHandle<StructureTemplate, List<StructureTemplate.Palette>> PALETTES = FieldHandle.getFor(StructureTemplate.class, "palettes");
    private static final FieldHandle<StructureTemplate, List<StructureTemplate.StructureEntityInfo>> ENTITY_INFO_LIST = FieldHandle.getFor(StructureTemplate.class, "entityInfoList");
    private static final FieldHandle<StructureTemplate, Vec3i> SIZE = FieldHandle.getFor(StructureTemplate.class, "size");
    private static final MethodHandle PALETTE_CONSTRUCTOR = ReflectionUtils.constructor(StructureTemplate.Palette.class, MethodType.methodType(void.class, List.class));

    private final Vec3i size;
    private final Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks = new LinkedHashMap<>();
    private final List<StructureTemplate.StructureEntityInfo> entities = new ArrayList<>();

    private StructureTemplateBuilder(Vec3i size) {
        this.size = size;
    }

    public static StructureTemplateBuilder withSize(int length, int height, int width) {
        return new StructureTemplateBuilder(new Vec3i(length, height, width)).fill(0, 0, 0, length, height, width, Blocks.AIR.defaultBlockState());
    }

    public static StructureTemplate empty(int length, int height, int width) {
        return withSize(length, height, width).build();
    }

    public StructureTemplateBuilder fill(int x, int y, int z, int length, int height, int width, BlockState state) {
        return fill(x, y, z, length, height, width, state, null);
    }

    public StructureTemplateBuilder fill(int x, int y, int z, int length, int height, int width, BlockState state, @Nullable CompoundTag nbt) {
        for (int x1 = x; x1 < length; x1++) {
            for (int y1 = y; y1 < height; y1++) {
                for (int z1 = z; z1 < width; z1++) {
                    set(x1, y1, z1, state, nbt);
                }
            }
        }
        return this;
    }

    public StructureTemplateBuilder set(int x, int y, int z, BlockState state) {
        return set(x, y, z, state, null);
    }

    @Override
    public StructureTemplateBuilder set(int x, int y, int z, BlockState state, @Nullable CompoundTag nbt) {
        blocks.put(new BlockPos(x, y, z), new StructureTemplate.StructureBlockInfo(new BlockPos(x, y, z), state, nbt));
        return this;
    }

    public StructureTemplate build() {
        final StructureTemplate template = new StructureTemplate();
        SIZE.set(template, size);
        ENTITY_INFO_LIST.set(template, entities);
        try {
            Comparator<StructureTemplate.StructureBlockInfo> comparator = Comparator.<StructureTemplate.StructureBlockInfo>comparingInt((block) -> block.pos().getY())
                    .thenComparingInt((block) -> block.pos().getX())
                    .thenComparingInt((block) -> block.pos().getZ());

            final List<StructureTemplate.StructureBlockInfo> infos = Lists.newArrayList();
            infos.addAll(blocks.values());
            infos.sort(comparator);
            PALETTES.set(template, List.of((StructureTemplate.Palette) PALETTE_CONSTRUCTOR.invokeExact(infos)));
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        return template;
    }

    public static Supplier<StructureTemplate> lazy(int length, int height, int width, UnaryOperator<StructureTemplateBuilder> consumer) {
        return Suppliers.memoize(() -> consumer.apply(withSize(length, height, width)).build());
    }

    private interface FieldHandle<I, T> {
        T get(I instance);

        void set(I instance, T value);

        @SuppressWarnings("unchecked")
        static <I, T> FieldHandle<I, T> getFor(Class<I> clazz, String fieldName) {
            final Field field = ReflectionUtils.getField(clazz, fieldName);
            final MethodHandle handle = ReflectionUtils.fieldHandle(field);
            return new FieldHandle<>() {
                @Override
                public T get(I instance) {
                    try {
                        return (T) handle.invokeExact(instance);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void set(I instance, T value) {
                    ObfuscationReflectionHelper.setPrivateValue(clazz, instance, value, fieldName);
                }
            };
        }
    }
}
