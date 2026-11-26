/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.registries;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.client.CustomCloudsRenderer;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;
import net.neoforged.neoforge.client.CustomWeatherEffectRenderer;
import net.neoforged.neoforge.client.entity.animation.AnimationTarget;
import net.neoforged.neoforge.client.gui.map.IMapDecorationRenderer;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.neoforged.neoforge.client.model.block.CustomBlockModelDefinition;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.client.pipeline.PipelineModifier;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.RegistryBuilder;

public final class NeoForgeClientRegistries {
    public static final Registry<MapCodec<? extends CustomBlockModelDefinition>> BLOCK_STATE_MODEL_DISPATCHER_TYPE = new RegistryBuilder<>(Keys.BLOCK_STATE_MODEL_DISPATCHER_TYPE).create();
    public static final Registry<MapCodec<? extends CustomUnbakedBlockStateModel>> BLOCK_STATE_MODEL_TYPE = new RegistryBuilder<>(Keys.BLOCK_STATE_MODEL_TYPE).create();
    public static final Registry<UnbakedModelLoader<?>> UNBAKED_MODEL_LOADER = new RegistryBuilder<>(Keys.UNBAKED_MODEL_LOADER).create();
    public static final Registry<CustomCloudsRenderer> CUSTOM_CLOUDS_RENDERER = new RegistryBuilder<>(Keys.CUSTOM_CLOUDS_RENDERER)
            .onAdd(NeoForgeClientRegistryCallbacks.CustomCloudsRendererCallbacks.INSTANCE)
            .create();
    public static final Registry<CustomSkyboxRenderer> CUSTOM_SKYBOX_RENDERER = new RegistryBuilder<>(Keys.CUSTOM_SKYBOX_RENDERER)
            .onAdd(NeoForgeClientRegistryCallbacks.CustomSkyboxRendererCallbacks.INSTANCE)
            .create();
    public static final Registry<CustomWeatherEffectRenderer> CUSTOM_WEATHER_EFFECT_RENDERER = new RegistryBuilder<>(Keys.CUSTOM_WEATHER_EFFECT_RENDERER)
            .onAdd(NeoForgeClientRegistryCallbacks.CustomWeatherEffectRendererCallbacks.INSTANCE)
            .create();
    public static final Registry<AnimationTarget> ANIMATION_CHANNEL_TARGET = new RegistryBuilder<>(Keys.ANIMATION_CHANNEL_TARGET)
            .onBake(NeoForgeClientRegistryCallbacks.AnimationChannelTargetCallbacks.INSTANCE)
            .create();
    public static final Registry<AnimationChannel.Interpolation> ANIMATION_CHANNEL_INTERPOLATION = new RegistryBuilder<>(Keys.ANIMATION_CHANNEL_INTERPOLATION).create();
    public static final Registry<PresetEditor> WORLD_PRESET_EDITOR = new RegistryBuilder<>(Keys.WORLD_PRESET_EDITOR).create();
    public static final Registry<IMapDecorationRenderer> MAP_DECORATION_RENDERER = new RegistryBuilder<>(Keys.MAP_DECORATION_RENDERER)
            .onAdd(NeoForgeClientRegistryCallbacks.MapDecorationRendererCallbacks.INSTANCE)
            .onBake(NeoForgeClientRegistryCallbacks.MapDecorationRendererCallbacks.INSTANCE)
            .create();
    public static final Registry<PipelineModifier> RENDER_PIPELINE_MODIFIER = new RegistryBuilder<>(Keys.RENDER_PIPELINE_MODIFIER).create();

    public static final class Keys {
        public static final ResourceKey<Registry<MapCodec<? extends CustomBlockModelDefinition>>> BLOCK_STATE_MODEL_DISPATCHER_TYPE = key("block_state_model_dispatcher_type");
        public static final ResourceKey<Registry<MapCodec<? extends CustomUnbakedBlockStateModel>>> BLOCK_STATE_MODEL_TYPE = key("block_state_model_type");
        public static final ResourceKey<Registry<UnbakedModelLoader<?>>> UNBAKED_MODEL_LOADER = key("unbaked_model_loader");
        public static final ResourceKey<Registry<CustomCloudsRenderer>> CUSTOM_CLOUDS_RENDERER = key("custom_clouds_renderer");
        public static final ResourceKey<Registry<CustomSkyboxRenderer>> CUSTOM_SKYBOX_RENDERER = key("custom_skybox_renderer");
        public static final ResourceKey<Registry<CustomWeatherEffectRenderer>> CUSTOM_WEATHER_EFFECT_RENDERER = key("custom_weather_effect_renderer");
        public static final ResourceKey<Registry<AnimationTarget>> ANIMATION_CHANNEL_TARGET = key("animation_channel_target");
        public static final ResourceKey<Registry<AnimationChannel.Interpolation>> ANIMATION_CHANNEL_INTERPOLATION = key("animation_channel_interpolation");
        public static final ResourceKey<Registry<PresetEditor>> WORLD_PRESET_EDITOR = key("world_preset_editor");
        public static final ResourceKey<Registry<IMapDecorationRenderer>> MAP_DECORATION_RENDERER = key("map_decoration_renderer");
        public static final ResourceKey<Registry<PipelineModifier>> RENDER_PIPELINE_MODIFIER = key("render_pipeline_modifier");

        private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, name));
        }

        private Keys() {}
    }

    private NeoForgeClientRegistries() {}
}
