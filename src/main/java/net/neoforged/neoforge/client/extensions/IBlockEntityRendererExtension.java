/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

public interface IBlockEntityRendererExtension<T extends BlockEntity> {
    AABB INFINITE_EXTENT_AABB = new net.minecraft.world.phys.AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    /**
     * Return an {@link AABB} that controls the visible scope of this {@link BlockEntityRenderer}.
     * Defaults to the unit cube at the given position.
     *
     * @return an appropriately sized {@link AABB} for the {@link BlockEntityRenderer}
     */
    default AABB getRenderBoundingBox(T blockEntity) {
        return new AABB(blockEntity.getBlockPos());
    }
}
