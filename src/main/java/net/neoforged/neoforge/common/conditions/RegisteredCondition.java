/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public record RegisteredCondition<T>(ResourceKey<T> registryKey) implements ICondition {
    public static final MapCodec<RegisteredCondition<?>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Identifier.CODEC.optionalFieldOf("registry", Registries.ITEM.identifier()).forGetter(condition -> condition.registryKey().registry()),
                    Identifier.CODEC.fieldOf("value").forGetter(condition -> condition.registryKey().identifier()))
            .apply(instance, RegisteredCondition::new));

    private RegisteredCondition(Identifier registryType, Identifier registryName) {
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
