/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.blockstate;

import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.block.model.Variant;

public interface UnbakedMutator {
    default Variant apply(Variant variant) {
        return apply(new SingleVariant.Unbaked(variant)).variant();
    }

    <T extends BlockStateModel.Unbaked> T apply(T unbaked);
}
