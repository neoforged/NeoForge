/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.fml.ModList;

public record ModLoadedCondition(String modid) implements ICondition {
    public static Codec<ModLoadedCondition> CODEC = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            Codec.STRING.fieldOf("modid").forGetter(ModLoadedCondition::modid))
                    .apply(builder, ModLoadedCondition::new));

    @Override
    public boolean test(IContext context) {
        return ModList.get().isLoaded(modid);
    }

    @Override
    public Codec<? extends ICondition> codec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "mod_loaded(\"" + modid + "\")";
    }
}
