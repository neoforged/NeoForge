/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.loadingplugin;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Modifiers called at various points during model loading to allow modification of models loaded from JSON files.
 */
public sealed interface ModelModifier {
    /**
     * Called when an {@link UnbakedModel} has been loaded from a resource pack.
     */
    non-sealed interface ModifyOnLoad extends ModelModifier {
        /**
         * Modify the incoming model
         *
         * @param model   The model to be modified
         * @param context Additional information about the model being modified
         * @return the modified model or, if no modifications were applied, the original model
         */
        default UnbakedModel modifyModelOnLoad(UnbakedModel model, Context context) {
            return model;
        }

        interface Context {
            /** {@return the ID of the model being modified} */
            Identifier id();
        }
    }

    /**
     * Called after a {@link BlockStateModel.UnbakedRoot} has been instantiated in a {@link BlockStateModelDispatcher}.
     */
    non-sealed interface ModifyBlockOnLoad extends ModelModifier {
        /**
         * Modify the incoming model
         *
         * @param model   The model to be modified
         * @param context Additional information about the model being modified
         * @return the modified model or, if no modifications were applied, the original model
         */
        default BlockStateModel.UnbakedRoot modifyBlockModelOnLoad(BlockStateModel.UnbakedRoot model, Context context) {
            return model;
        }

        interface Context {
            /** {@return the {@link BlockState} the model being modified is assigned to} */
            BlockState state();
        }
    }

    /**
     * Called before a {@link BlockStateModel.UnbakedRoot} is about to be baked into a {@link BlockStateModel}.
     */
    non-sealed interface ModifyBlockBeforeBake extends ModelModifier {
        /**
         * Modify the incoming model
         *
         * @param model   The model to be modified
         * @param context Additional information about the model being modified
         * @return the modified model or, if no modifications were applied, the original model
         */
        default BlockStateModel.UnbakedRoot modifyBlockModelBeforeBake(BlockStateModel.UnbakedRoot model, Context context) {
            return model;
        }

        interface Context {
            /** {@return the {@link BlockState} the model being modified is assigned to} */
            BlockState state();

            /** {@return the {@link ModelBaker} the model will be baked with} */
            ModelBaker baker();
        }
    }

    /**
     * Called after a {@link BlockStateModel.UnbakedRoot} has been baked into a {@link BlockStateModel}.
     */
    non-sealed interface ModifyBlockAfterBake extends ModelModifier {
        /**
         * Modify the incoming model
         *
         * @param model   The model to be modified
         * @param context Additional information about the model being modified
         * @return the modified model or, if no modifications were applied, the original model
         */
        default BlockStateModel modifyBlockModelAfterBake(BlockStateModel model, Context context) {
            return model;
        }

        interface Context {
            /** {@return the {@link BlockState} the model being modified is assigned to} */
            BlockState state();

            /** {@return the {@link BlockStateModel.UnbakedRoot} the model was baked from} */
            BlockStateModel.UnbakedRoot sourceModel();

            /** {@return the {@link ModelBaker} the model was baked with} */
            ModelBaker baker();
        }
    }

    /**
     * Called before an {@link ItemModel.Unbaked} is about to be baked into an {@link ItemModel}.
     */
    non-sealed interface ModifyItemBeforeBake extends ModelModifier {
        /**
         * Modify the incoming model
         *
         * @param model   The model to be modified
         * @param context Additional information about the model being modified
         * @return the modified model or, if no modifications were applied, the original model
         */
        default ItemModel.Unbaked modifyItemModelBeforeBake(ItemModel.Unbaked model, Context context) {
            return model;
        }

        interface Context {
            /** {@return the ID of the model being modified} */
            Identifier id();

            /** {@return the {@link ClientItem} the model is declared by} */
            ClientItem clientItem();

            /** {@return the {@link ItemModel.BakingContext} the model will be baked with} */
            ItemModel.BakingContext bakingContext();
        }
    }

    /**
     * Called after an {@link ItemModel.Unbaked} has been baked into an {@link ItemModel}.
     */
    non-sealed interface ModifyItemAfterBake extends ModelModifier {
        /**
         * Modify the incoming model
         *
         * @param model   The model to be modified
         * @param context Additional information about the model being modified
         * @return the modified model or, if no modifications were applied, the original model
         */
        default ItemModel modifyItemModelAfterBake(ItemModel model, Context context) {
            return model;
        }

        interface Context {
            /** {@return the ID of the model being modified} */
            Identifier id();

            /** {@return the {@link ItemModel.Unbaked} the model was baked from} */
            ItemModel.Unbaked sourceModel();

            /** {@return the {@link ClientItem} the model is declared by} */
            ClientItem clientItem();

            /** {@return the {@link ItemModel.BakingContext} the model was baked with} */
            ItemModel.BakingContext bakingContext();
        }
    }

    /**
     * Different phases of modifier application to provide a rough order between modifiers with different intentions.
     */
    enum Phase {
        /**
         * Intended for modifiers which entirely override the incoming model
         */
        OVERRIDE,
        /**
         * Default catch-all phase
         */
        DEFAULT,
        /**
         * Intended for modifiers which wrap the incoming model
         */
        WRAP,
        /**
         * Intended for modifiers which wrap the incoming model and need to be the last to do so
         */
        WRAP_LAST,
    }
}
