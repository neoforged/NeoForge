/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.NeoForgeMod;

public class NoneStructureModifier implements StructureModifier {
    public static final NoneStructureModifier INSTANCE = new NoneStructureModifier();

    @Override
    public void modify(Holder<Structure> structure, Phase phase, ModifiableStructureInfo.StructureInfo.Builder builder) {
        // NOOP - intended for datapack makers who want to disable a structure modifier
    }

    @Override
    public MapCodec<? extends StructureModifier> codec() {
        return NeoForgeMod.NONE_STRUCTURE_MODIFIER_TYPE.get();
    }
}
