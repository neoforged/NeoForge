/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public record Strippable(Block strippedStage) {
    public static final Codec<Strippable> STRIPPED_STAGE_CODEC = BuiltInRegistries.BLOCK.byNameCodec().xmap(Strippable::new, Strippable::strippedStage);

    public static final Codec<Strippable> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(inst -> inst.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("stripped_stage")
                            .forGetter(o -> o.strippedStage))
                    .apply(inst, Strippable::new)),
            STRIPPED_STAGE_CODEC);
}
