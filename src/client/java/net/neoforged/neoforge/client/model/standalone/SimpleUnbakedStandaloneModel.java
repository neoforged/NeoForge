/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.standalone;

import java.util.function.BiFunction;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.ResourceLocation;

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
    private final ResourceLocation modelId;
    private final BiFunction<ResolvedModel, ModelBaker, T> bake;

    /**
     * Construct a new {@link SimpleUnbakedStandaloneModel}.
     *
     * @param modelId The id of the model to load.
     * @param bake    The function to bake the model, converting a {@link ResolvedModel} into the required type.
     */
    public SimpleUnbakedStandaloneModel(ResourceLocation modelId, BiFunction<ResolvedModel, ModelBaker, T> bake) {
        this.modelId = modelId;
        this.bake = bake;
    }

    @Override
    public T bake(ModelBaker baker) {
        return bake.apply(baker.getModel(modelId), baker);
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        resolver.markDependency(modelId);
    }

    /**
     * {@return an unbaked standalone model for a {@link SimpleModelWrapper}, baked without additional transformations}
     */
    public static SimpleUnbakedStandaloneModel<SimpleModelWrapper> simpleModelWrapper(ResourceLocation modelId) {
        return new SimpleUnbakedStandaloneModel<>(
                modelId, (model, baker) -> SimpleModelWrapper.bake(baker, model, BlockModelRotation.X0_Y0));
    }

    /**
     * {@return an unbaked standalone model for a {@link SimpleModelWrapper}, baked with the provided {@link ModelState} transformations}
     */
    public static SimpleUnbakedStandaloneModel<SimpleModelWrapper> simpleModelWrapper(ResourceLocation modelId, ModelState modelState) {
        return new SimpleUnbakedStandaloneModel<>(
                modelId, (model, baker) -> SimpleModelWrapper.bake(baker, model, modelState));
    }

    /**
     * {@return an unbaked standalone model for a {@link BlockStateModel}, baked without additional transformations}
     */
    public static SimpleUnbakedStandaloneModel<BlockStateModel> blockStateModel(ResourceLocation modelId) {
        return new SimpleUnbakedStandaloneModel<>(
                modelId, (model, baker) -> new SingleVariant(SimpleModelWrapper.bake(baker, model, BlockModelRotation.X0_Y0)));
    }

    /**
     * {@return an unbaked standalone model for a {@link BlockStateModel}, baked with the provided {@link ModelState} transformations}
     */
    public static SimpleUnbakedStandaloneModel<BlockStateModel> blockStateModel(ResourceLocation modelId, ModelState modelState) {
        return new SimpleUnbakedStandaloneModel<>(
                modelId, (model, baker) -> new SingleVariant(SimpleModelWrapper.bake(baker, model, modelState)));
    }

    /**
     * {@return an unbaked standalone model for a {@link QuadCollection}, baked without additional transformations}
     */
    public static SimpleUnbakedStandaloneModel<QuadCollection> quadCollection(ResourceLocation modelId) {
        return new SimpleUnbakedStandaloneModel<>(
                modelId, (model, baker) -> model.bakeTopGeometry(model.getTopTextureSlots(), baker, BlockModelRotation.X0_Y0));
    }

    /**
     * {@return an unbaked standalone model for a {@link QuadCollection}, baked with the provided {@link ModelState} transformations}
     */
    public static SimpleUnbakedStandaloneModel<QuadCollection> quadCollection(ResourceLocation modelId, ModelState modelState) {
        return new SimpleUnbakedStandaloneModel<>(
                modelId, (model, baker) -> model.bakeTopGeometry(model.getTopTextureSlots(), baker, modelState));
    }
}
