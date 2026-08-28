/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/// A variant of {@link BakedModelWrapper} that re-wraps any derivative {@link BakedModel}s returned by the wrapped model's methods.
///
/// Useful for ensuring wrapper behavior is preserved across transforms and render passes.
public abstract class PropagatingBakedModelWrapper<T extends BakedModel> extends BakedModelWrapper<T> {
    public PropagatingBakedModelWrapper(T originalModel) {
        super(originalModel);
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext cameraTransformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        return rewrap(super.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform));
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
        return super.getRenderPasses(itemStack, fabulous).stream().map(this::rewrap).toList();
    }

    protected abstract BakedModel rewrap(BakedModel model);
}
