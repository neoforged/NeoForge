/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.blockstate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.neoforged.neoforge.client.model.block.CompositeBlockModel;
import org.jetbrains.annotations.Nullable;

public class CompositeBlockStateModelBuilder extends CustomBlockStateModelBuilder {
    @Nullable
    private CompositeBlockModel.Unbaked model = null;

    public void addPartModel(BlockStateModel.Unbaked partModel) {
        List<BlockStateModel.Unbaked> models = new ArrayList<>();
        if (this.model != null) {
            models.addAll(this.model.models());
        }
        models.add(partModel);
        this.model = new CompositeBlockModel.Unbaked(models);
    }

    @Override
    public CustomBlockStateModelBuilder with(VariantMutator variantMutator) {
        return this;
    }

    @Override
    public BlockStateModel.Unbaked toUnbaked() {
        return Objects.requireNonNull(this.model, "Composite model has no parts");
    }
}
