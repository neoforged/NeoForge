/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.blockstate;

import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

public abstract class CustomBlockStateModelBuilder {
    protected CustomBlockStateModelBuilder() {}

    public abstract CustomBlockStateModelBuilder with(VariantMutator variantMutator);

    public abstract BlockStateModel.Unbaked toUnbaked();

    public static final class Simple extends CustomBlockStateModelBuilder {
        private final CustomUnbakedBlockStateModel blockStateModel;

        public Simple(CustomUnbakedBlockStateModel blockStateModel) {
            this.blockStateModel = blockStateModel;
        }

        @Override
        public Simple with(VariantMutator variantMutator) {
            return this;
        }

        @Override
        public BlockStateModel.Unbaked toUnbaked() {
            return this.blockStateModel;
        }
    }
}
