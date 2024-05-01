/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.potion;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

public class PotionFluidTypeClientExtensions implements IClientFluidTypeExtensions {
    public static final ResourceLocation TEXTURE_STILL = new ResourceLocation(NeoForgeVersion.MOD_ID, "block/potion_still");
    public static final ResourceLocation TEXTURE_FLOWING = new ResourceLocation(NeoForgeVersion.MOD_ID, "block/potion_flow");

    @Override
    public int getTintColor() {
        return 0xFF385DC6; // PotionContents.BASE_POTION_COLOR
    }

    @Override
    public int getTintColor(FluidStack stack) {
        var color = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor();
        return FastColor.ARGB32.opaque(color);
    }

    @Override
    public ResourceLocation getStillTexture() {
        return TEXTURE_STILL;
    }

    @Override
    public ResourceLocation getFlowingTexture() {
        // even though we dont register a flowing potion fluid
        // we still provide a flowing texture for rendering
        // tanks / ui often use the flowing texture rather than the still texture
        return TEXTURE_FLOWING;
    }
}
