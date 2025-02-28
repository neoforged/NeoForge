/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record RegisteredCondition<T>(ResourceKey<T> registryKey) implements ICondition {
    public static final MapCodec<RegisteredCondition<?>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(ResourceLocation.CODEC.optionalFieldOf("registry", Registries.ITEM.location()).forGetter(condition -> condition.registryKey().registry()),
                    ResourceLocation.CODEC.fieldOf("value").forGetter(condition -> condition.registryKey().location()))
            .apply(instance, RegisteredCondition::new));

    private RegisteredCondition(ResourceLocation registryType, ResourceLocation registryName) {
        this(ResourceKey.create(ResourceKey.createRegistryKey(registryType), registryName));
    }

    @Override
    public boolean test(IContext context) {
        return context.registryAccess().holder(registryKey).map(Holder::isBound).orElse(false);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
