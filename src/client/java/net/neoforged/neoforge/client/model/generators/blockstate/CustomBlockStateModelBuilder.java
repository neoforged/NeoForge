/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.blockstate;

import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

/**
 * Builder for {@link CustomUnbakedBlockStateModel}s to allow using them with {@link MultiVariantGenerator} and
 * {@link MultiPartGenerator} by plugging them into {@link MultiVariant}
 */
public abstract class CustomBlockStateModelBuilder {
    protected CustomBlockStateModelBuilder() {}

    /**
     * Apply the provided {@link VariantMutator} to this builder.
     */
    public abstract CustomBlockStateModelBuilder with(VariantMutator variantMutator);

    /**
     * Apply the provided {@link UnbakedMutator} to the model being built by this builder and
     * return a new builder with the mutated state.
     */
    public abstract CustomBlockStateModelBuilder with(UnbakedMutator variantMutator);

    /**
     * Convert this builder to the final {@link CustomUnbakedBlockStateModel} for serialization.
     */
    public abstract CustomUnbakedBlockStateModel toUnbaked();

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
        public CustomBlockStateModelBuilder with(UnbakedMutator variantMutator) {
            return new Simple(variantMutator.apply(blockStateModel));
        }

        @Override
        public CustomUnbakedBlockStateModel toUnbaked() {
            return this.blockStateModel;
        }
    }
}
