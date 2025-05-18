/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.blockstate;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.neoforged.neoforge.client.model.block.CompositeBlockModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

/**
 * This builder creates a {@link CompositeBlockModel.Unbaked}.
 */
public class CompositeBlockStateModelBuilder extends CustomBlockStateModelBuilder {
    private final List<BlockStateModel.Unbaked> models = new ArrayList<>();

    /**
     * Add a part model to this composite model
     */
    public void addPartModel(BlockStateModel.Unbaked partModel) {
        this.models.add(partModel);
    }

    @Override
    public CompositeBlockStateModelBuilder with(VariantMutator variantMutator) {
        return this;
    }

    @Override
    public CustomBlockStateModelBuilder with(UnbakedMutator variantMutator) {
        var result = new CompositeBlockStateModelBuilder();
        for (var model : models) {
            result.models.add(variantMutator.apply(model));
        }
        return result;
    }

    @Override
    public CustomUnbakedBlockStateModel toUnbaked() {
        return new CompositeBlockModel.Unbaked(this.models);
    }
}
