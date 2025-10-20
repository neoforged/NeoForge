/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.client.model.standalone.UnbakedStandaloneModel;
import org.jetbrains.annotations.ApiStatus;

/**
 * Houses events related to models.
 */
public abstract class ModelEvent extends Event {
    @ApiStatus.Internal
    protected ModelEvent() {}

    /**
     * Fired while the {@link ModelManager} is reloading models, after the model registry is set up, but before it's
     * passed to the {@link net.minecraft.client.renderer.block.BlockModelShaper} for caching.
     *
     * <p>
     * This event is fired from a worker thread and it is therefore not safe to access anything outside the
     * model registry and {@link ModelBakery} provided in this event.<br>
     * The {@link ModelManager} firing this event is not fully set up with the latest data when this event fires and
     * must therefore not be accessed in this event.
     * </p>
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
     *
     * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class ModifyBakingResult extends ModelEvent implements IModBusEvent {
        private final ModelBakery.BakingResult bakingResult;
        private final Function<ResourceLocation, TextureAtlasSprite> textureGetter;
        private final ModelBakery modelBakery;

        @ApiStatus.Internal
        public ModifyBakingResult(ModelBakery.BakingResult bakingResult, Function<ResourceLocation, TextureAtlasSprite> textureGetter, ModelBakery modelBakery) {
            this.bakingResult = bakingResult;
            this.textureGetter = textureGetter;
            this.modelBakery = modelBakery;
        }

        /**
         * @return The result of the model baking
         */
        public ModelBakery.BakingResult getBakingResult() {
            return bakingResult;
        }

        /**
         * Returns a lookup function to retrieve {@link TextureAtlasSprite}s by name from the block atlas.
         *
         * @return a function to lookup sprites from an atlas by name
         */
        public Function<ResourceLocation, TextureAtlasSprite> getTextureGetter() {
            return textureGetter;
        }

        /**
         * @return the model loader
         */
        public ModelBakery getModelBakery() {
            return modelBakery;
        }
    }

    /**
     * Fired when the {@link ModelManager} is notified of the resource manager reloading.
     * Called after the model registry is set up and cached in the {@link net.minecraft.client.renderer.block.BlockModelShaper}.<br>
     * The model registry given by this event is unmodifiable. To modify the model registry, use
     * {@link ModelEvent.ModifyBakingResult} instead.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
     *
     * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class BakingCompleted extends ModelEvent implements IModBusEvent {
        private final ModelManager modelManager;
        private final ModelBakery.BakingResult bakingResult;
        private final ModelBakery modelBakery;

        @ApiStatus.Internal
        public BakingCompleted(ModelManager modelManager, ModelBakery.BakingResult bakingResult, ModelBakery modelBakery) {
            this.modelManager = modelManager;
            this.bakingResult = new ModelBakery.BakingResult(
                    bakingResult.missingModels(),
                    Collections.unmodifiableMap(bakingResult.blockStateModels()),
                    Collections.unmodifiableMap(bakingResult.itemStackModels()),
                    Collections.unmodifiableMap(bakingResult.itemProperties()),
                    bakingResult.standaloneModels().unmodifiable());
            this.modelBakery = modelBakery;
        }

        /**
         * @return the model manager
         */
        public ModelManager getModelManager() {
            return modelManager;
        }

        /**
         * @return The result of the model baking
         */
        public ModelBakery.BakingResult getBakingResult() {
            return bakingResult;
        }

        /**
         * @return the model loader
         */
        public ModelBakery getModelBakery() {
            return modelBakery;
        }
    }

    /**
     * Fired when the {@link net.minecraft.client.resources.model.ModelDiscovery} is notified of dependency discovery of its top models.
     * Allows developers to register standalone models to be loaded, along with their dependencies.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
     *
     * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class RegisterStandalone extends ModelEvent implements IModBusEvent {
        private final Map<StandaloneModelKey<?>, UnbakedStandaloneModel<?>> modelMap;

        @ApiStatus.Internal
        public RegisterStandalone(Map<StandaloneModelKey<?>, UnbakedStandaloneModel<?>> modelMap) {
            this.modelMap = modelMap;
        }

        /**
         * Registers a {@linkplain UnbakedStandaloneModel model} to be loaded, along with its dependencies.
         */
        public <T> void register(StandaloneModelKey<T> modelKey, UnbakedStandaloneModel<T> baker) {
            modelMap.put(modelKey, baker);
        }
    }

    /**
     * Allows users to register their own {@link UnbakedModelLoader unbaked model loaders} for use in block/item models.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
     *
     * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     */
    public static class RegisterLoaders extends ModelEvent implements IModBusEvent {
        private final Map<ResourceLocation, UnbakedModelLoader<?>> loaders;

        @ApiStatus.Internal
        public RegisterLoaders(Map<ResourceLocation, UnbakedModelLoader<?>> loaders) {
            this.loaders = loaders;
        }

        /**
         * Registers a new unbaked model loader.
         *
         * @param key    the ID of the loader
         * @param loader the loader to register
         */
        public void register(ResourceLocation key, UnbakedModelLoader<?> loader) {
            Preconditions.checkArgument(!loaders.containsKey(key), "Unbaked model loader already registered: " + key);
            loaders.put(key, loader);
        }
    }
}
