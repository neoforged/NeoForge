/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.standalone;

import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.ResolvedModel;
import net.neoforged.neoforge.client.event.ModelEvent;

/**
 * Baker implementation for standalone models registered to {@link ModelEvent.RegisterStandalone}.
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
@FunctionalInterface
public interface StandaloneModelBaker<T> {
    T bake(ResolvedModel model, ModelBaker baker);

    /**
     * {@return a standalone baker for a {@link SimpleModelWrapper}, baked without additional transformations}
     */
    static StandaloneModelBaker<SimpleModelWrapper> simpleModelWrapper() {
        return (model, baker) -> SimpleModelWrapper.bake(baker, model, BlockModelRotation.X0_Y0);
    }

    /**
     * {@return a standalone baker for a {@link SimpleModelWrapper}, baked with the provided {@link ModelState} transformations}
     */
    static StandaloneModelBaker<SimpleModelWrapper> simpleModelWrapper(ModelState modelState) {
        return (model, baker) -> SimpleModelWrapper.bake(baker, model, modelState);
    }

    /**
     * {@return a standalone baker for a {@link BlockStateModel}, baked without additional transformations}
     */
    static StandaloneModelBaker<BlockStateModel> blockStateModel() {
        return (model, baker) -> new SingleVariant(SimpleModelWrapper.bake(baker, model, BlockModelRotation.X0_Y0));
    }

    /**
     * {@return a standalone baker for a {@link BlockStateModel}, baked with the provided {@link ModelState} transformations}
     */
    static StandaloneModelBaker<BlockStateModel> blockStateModel(ModelState modelState) {
        return (model, baker) -> new SingleVariant(SimpleModelWrapper.bake(baker, model, modelState));
    }

    /**
     * {@return a standalone baker for a {@link QuadCollection}, baked without additional transformations}
     */
    static StandaloneModelBaker<QuadCollection> quadCollection() {
        return (model, baker) -> model.bakeTopGeometry(model.getTopTextureSlots(), baker, BlockModelRotation.X0_Y0);
    }

    /**
     * {@return a standalone baker for a {@link QuadCollection}, baked with the provided {@link ModelState} transformations}
     */
    static StandaloneModelBaker<QuadCollection> quadCollection(ModelState modelState) {
        return (model, baker) -> model.bakeTopGeometry(model.getTopTextureSlots(), baker, modelState);
    }
}
