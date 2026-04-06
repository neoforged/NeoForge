/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.standalone;

import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.Identifier;

/**
 * An {@link UnbakedStandaloneModel} implementation that loads a single model.
 *
 * <p>Depending on the context where it is used, different parts of a {@link ResolvedModel}s might be baked.
 * For example, block models query ambient occlusion, item models query transforms,
 * and both query baked geometry.
 * Each standalone model baker can therefore bake exactly the properties it needs,
 * and store them in an object of arbitrary type {@code T}.
 *
 * <p>The baked object can be retrieved later using {@link ModelManager#getStandaloneModel(StandaloneModelKey)}.
 *
 * @param <T> the type of the baked object, which contains some properties baked from the {@link ResolvedModel}
 * @see StandaloneModelKey
 */
public final class SimpleUnbakedStandaloneModel<T> implements UnbakedStandaloneModel<T> {
    private final Identifier modelId;
    private final SimpleBaker<T> bake;

    /**
     * Construct a new {@link SimpleUnbakedStandaloneModel}.
     *
     * @param modelId The id of the model to load.
     * @param bake    The function to bake the model, converting a {@link ResolvedModel} into the required type.
     */
    public SimpleUnbakedStandaloneModel(Identifier modelId, SimpleBaker<T> bake) {
        this.modelId = modelId;
        this.bake = bake;
    }

    @Override
    public T bake(ModelBaker baker, ModelDebugName name) {
        return bake.bake(baker.getModel(modelId), baker, name);
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        resolver.markDependency(modelId);
    }

    /**
     * {@return an unbaked standalone model for a {@link BlockStateModelPart}, baked without additional transformations}
     */
    public static SimpleUnbakedStandaloneModel<BlockStateModelPart> simpleModelWrapper(Identifier modelId) {
        return new SimpleUnbakedStandaloneModel<>(
                modelId, (model, baker, _) -> SimpleModelWrapper.bake(baker, model, BlockModelRotation.IDENTITY));
    }

    /**
     * {@return an unbaked standalone model for a {@link BlockStateModelPart}, baked with the provided {@link ModelState} transformations}
     */
    public static SimpleUnbakedStandaloneModel<BlockStateModelPart> simpleModelWrapper(Identifier modelId, ModelState modelState) {
        return new SimpleUnbakedStandaloneModel<>(
                modelId, (model, baker, _) -> SimpleModelWrapper.bake(baker, model, modelState));
    }

    /**
     * {@return an unbaked standalone model for a {@link BlockStateModel}, baked without additional transformations}
     */
    public static SimpleUnbakedStandaloneModel<BlockStateModel> blockStateModel(Identifier modelId) {
        return new SimpleUnbakedStandaloneModel<>(
                modelId, (model, baker, _) -> new SingleVariant(SimpleModelWrapper.bake(baker, model, BlockModelRotation.IDENTITY)));
    }

    /**
     * {@return an unbaked standalone model for a {@link BlockStateModel}, baked with the provided {@link ModelState} transformations}
     */
    public static SimpleUnbakedStandaloneModel<BlockStateModel> blockStateModel(Identifier modelId, ModelState modelState) {
        return new SimpleUnbakedStandaloneModel<>(
                modelId, (model, baker, _) -> new SingleVariant(SimpleModelWrapper.bake(baker, model, modelState)));
    }

    /**
     * {@return an unbaked standalone model for a {@link QuadCollection}, baked without additional transformations}
     */
    public static SimpleUnbakedStandaloneModel<QuadCollection> quadCollection(Identifier modelId) {
        return new SimpleUnbakedStandaloneModel<>(
                modelId, (model, baker, _) -> model.bakeTopGeometry(model.getTopTextureSlots(), baker, BlockModelRotation.IDENTITY));
    }

    /**
     * {@return an unbaked standalone model for a {@link QuadCollection}, baked with the provided {@link ModelState} transformations}
     */
    public static SimpleUnbakedStandaloneModel<QuadCollection> quadCollection(Identifier modelId, ModelState modelState) {
        return new SimpleUnbakedStandaloneModel<>(
                modelId, (model, baker, _) -> model.bakeTopGeometry(model.getTopTextureSlots(), baker, modelState));
    }

    @FunctionalInterface
    public interface SimpleBaker<T> {
        T bake(ResolvedModel model, ModelBaker baker, ModelDebugName name);
    }
}
