/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.registries;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.neoforged.neoforge.client.CustomCloudsRenderer;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;
import net.neoforged.neoforge.client.CustomWeatherEffectRenderer;
import net.neoforged.neoforge.client.entity.animation.AnimationTarget;
import net.neoforged.neoforge.client.entity.animation.json.AnimationTypeManager;
import net.neoforged.neoforge.client.gui.map.IMapDecorationRenderer;
import net.neoforged.neoforge.client.gui.map.MapDecorationRendererManager;
import net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes;
import net.neoforged.neoforge.registries.callback.AddCallback;
import net.neoforged.neoforge.registries.callback.BakeCallback;

final class NeoForgeClientRegistryCallbacks {
    static final class BlockEntityRendererCallbacks extends ClientRegistryCallback<BlockEntityType<?>, BlockEntityRendererProvider<?, ?>> {
        static final BlockEntityRendererCallbacks INSTANCE = new BlockEntityRendererCallbacks();

        private BlockEntityRendererCallbacks() {
            super(BuiltInRegistries.BLOCK_ENTITY_TYPE, () -> {});
        }
    }

    static final class EntityRendererCallbacks extends ClientRegistryCallback<EntityType<?>, EntityRendererProvider<?>> {
        static final EntityRendererCallbacks INSTANCE = new EntityRendererCallbacks();

        private EntityRendererCallbacks() {
            super(BuiltInRegistries.ENTITY_TYPE, EntityRenderers::collectMissingEntityRenderers);
        }
    }

    static final class MenuScreenCallbacks extends ClientRegistryCallback<MenuType<?>, MenuScreens.ScreenConstructor<?, ?>> {
        static final MenuScreenCallbacks INSTANCE = new MenuScreenCallbacks();

        private MenuScreenCallbacks() {
            super(BuiltInRegistries.MENU, MenuScreens::selfTest);
        }
    }

    static final class ParticleProviderCallbacks extends ClientRegistryCallback<ParticleType<?>, ParticleProvider<?>> {
        static final ParticleProviderCallbacks INSTANCE = new ParticleProviderCallbacks();

        private ParticleProviderCallbacks() {
            super(BuiltInRegistries.PARTICLE_TYPE, () -> {});
        }
    }

    static final class CustomCloudsRendererCallbacks implements AddCallback<CustomCloudsRenderer> {
        static final CustomCloudsRendererCallbacks INSTANCE = new CustomCloudsRendererCallbacks();

        @Override
        public void onAdd(Registry<CustomCloudsRenderer> registry, int id, ResourceKey<CustomCloudsRenderer> key, CustomCloudsRenderer value) {
            if (NeoForgeEnvironmentAttributes.DEFAULT_CUSTOM_CLOUDS.equals(key.identifier())) {
                throw new IllegalArgumentException("You cannot register a renderer for the default clouds");
            }
        }
    }

    static final class CustomSkyboxRendererCallbacks implements AddCallback<CustomSkyboxRenderer> {
        static final CustomSkyboxRendererCallbacks INSTANCE = new CustomSkyboxRendererCallbacks();

        @Override
        public void onAdd(Registry<CustomSkyboxRenderer> registry, int id, ResourceKey<CustomSkyboxRenderer> key, CustomSkyboxRenderer value) {
            if (NeoForgeEnvironmentAttributes.DEFAULT_CUSTOM_SKYBOX.equals(key.identifier())) {
                throw new IllegalArgumentException("You cannot register a renderer for the default skybox");
            }
        }
    }

    static final class CustomWeatherEffectRendererCallbacks implements AddCallback<CustomWeatherEffectRenderer> {
        static final CustomWeatherEffectRendererCallbacks INSTANCE = new CustomWeatherEffectRendererCallbacks();

        @Override
        public void onAdd(Registry<CustomWeatherEffectRenderer> registry, int id, ResourceKey<CustomWeatherEffectRenderer> key, CustomWeatherEffectRenderer value) {
            if (NeoForgeEnvironmentAttributes.DEFAULT_CUSTOM_WEATHER_EFFECTS.equals(key.identifier())) {
                throw new IllegalArgumentException("You cannot register a renderer for the default weather effects");
            }
        }
    }

    static final class AnimationChannelTargetCallbacks implements BakeCallback<AnimationTarget> {
        static final AnimationChannelTargetCallbacks INSTANCE = new AnimationChannelTargetCallbacks();

        @Override
        public void onBake(Registry<AnimationTarget> registry) {
            AnimationTypeManager.recomputeTargetDerivedFields(registry);
        }
    }

    static final class MapDecorationRendererCallbacks extends ClientRegistryCallback<MapDecorationType, IMapDecorationRenderer> {
        static final MapDecorationRendererCallbacks INSTANCE = new MapDecorationRendererCallbacks();

        private MapDecorationRendererCallbacks() {
            super(BuiltInRegistries.MAP_DECORATION_TYPE, () -> {});
        }

        @Override
        public void onBake(Registry<IMapDecorationRenderer> registry) {
            super.onBake(registry);
            MapDecorationRendererManager.updateMapping(registry);
        }
    }

    private NeoForgeClientRegistryCallbacks() {}
}
