/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.collector;

import com.google.gson.reflect.TypeToken;
import com.mojang.datafixers.util.Pair;
import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.impl.TestFrameworkInternal;

public record CollectorType<A>(Type type) {
    public static final CollectorType<Test> TESTS = get(new TypeToken<>() {});
    public static final CollectorType<Pair<ResourceLocation, Supplier<StructureTemplate>>> STRUCTURE_TEMPLATES = get(new TypeToken<>() {});
    public static final CollectorType<Pair<OnInit.Stage, Consumer<? super TestFrameworkInternal>>> INIT_LISTENERS = get(new TypeToken<>() {});
    public static final CollectorType<GroupData> GROUP_DATA = get(new TypeToken<>() {});

    public record GroupData(String id, @Nullable Component title, boolean isEnabledByDefault, String[] parents) {}

    private static <Z> CollectorType<Z> get(TypeToken<Z> token) {
        return new CollectorType<>(token.getType());
    }
}
