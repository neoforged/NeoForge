/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

public interface IBlockEntityRendererExtension<T extends BlockEntity> {
    /**
     * Return an {@link AABB} that controls the visible scope of this {@link BlockEntityRenderer}.
     * Defaults to the unit cube at the given position. {@link AABB#INFINITE} can be used to declare the BER
     * should be visible everywhere.
     *
     * @return an appropriately sized {@link AABB} for the {@link BlockEntityRenderer}
     */
    default AABB getRenderBoundingBox(T blockEntity) {
        return new AABB(blockEntity.getBlockPos());
    }
}
