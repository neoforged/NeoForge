/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.registries;

import java.util.Locale;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
    static final class BlockEntityRendererCallbacks implements AddCallback<BlockEntityRendererProvider<?, ?>> {
        static final BlockEntityRendererCallbacks INSTANCE = new BlockEntityRendererCallbacks();

        @Override
        public void onAdd(Registry<BlockEntityRendererProvider<?, ?>> registry, int id, ResourceKey<BlockEntityRendererProvider<?, ?>> key, BlockEntityRendererProvider<?, ?> value) {
            assertHasCommonCounterpart(registry, BuiltInRegistries.BLOCK_ENTITY_TYPE, key.identifier());
        }
    }

    static final class EntityRendererCallbacks implements AddCallback<EntityRendererProvider<?>>, BakeCallback<EntityRendererProvider<?>> {
        static final EntityRendererCallbacks INSTANCE = new EntityRendererCallbacks();

        @Override
        public void onAdd(Registry<EntityRendererProvider<?>> registry, int id, ResourceKey<EntityRendererProvider<?>> key, EntityRendererProvider<?> value) {
            assertHasCommonCounterpart(registry, BuiltInRegistries.ENTITY_TYPE, key.identifier());
        }

        @Override
        public void onBake(Registry<EntityRendererProvider<?>> registry) {
            EntityRenderers.collectMissingEntityRenderers();
        }
    }

    static final class MenuScreenCallbacks implements AddCallback<MenuScreens.ScreenConstructor<?, ?>>, BakeCallback<MenuScreens.ScreenConstructor<?, ?>> {
        static final MenuScreenCallbacks INSTANCE = new MenuScreenCallbacks();

        @Override
        public void onAdd(Registry<MenuScreens.ScreenConstructor<?, ?>> registry, int id, ResourceKey<MenuScreens.ScreenConstructor<?, ?>> key, MenuScreens.ScreenConstructor<?, ?> value) {
            assertHasCommonCounterpart(registry, BuiltInRegistries.MENU, key.identifier());
        }

        @Override
        public void onBake(Registry<MenuScreens.ScreenConstructor<?, ?>> registry) {
            MenuScreens.selfTest();
        }
    }

    static final class ParticleProviderCallbacks implements AddCallback<ParticleProvider<?>> {
        static final ParticleProviderCallbacks INSTANCE = new ParticleProviderCallbacks();

        @Override
        public void onAdd(Registry<ParticleProvider<?>> registry, int id, ResourceKey<ParticleProvider<?>> key, ParticleProvider<?> value) {
            assertHasCommonCounterpart(registry, BuiltInRegistries.PARTICLE_TYPE, key.identifier());
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

    static final class MapDecorationRendererCallbacks implements AddCallback<IMapDecorationRenderer>, BakeCallback<IMapDecorationRenderer> {
        static final MapDecorationRendererCallbacks INSTANCE = new MapDecorationRendererCallbacks();

        @Override
        public void onAdd(Registry<IMapDecorationRenderer> registry, int id, ResourceKey<IMapDecorationRenderer> key, IMapDecorationRenderer value) {
            assertHasCommonCounterpart(registry, BuiltInRegistries.MAP_DECORATION_TYPE, key.identifier());
        }

        @Override
        public void onBake(Registry<IMapDecorationRenderer> registry) {
            MapDecorationRendererManager.updateMapping(registry);
        }
    }

    private static void assertHasCommonCounterpart(Registry<?> clientRegistry, Registry<?> commonRegistry, Identifier id) {
        if (!commonRegistry.containsKey(id)) {
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    "Entry '%s' in client registry '%s' has no counterpart in common registry '%s'",
                    id,
                    clientRegistry.key().identifier(),
                    commonRegistry.key().identifier()));
        }
    }

    private NeoForgeClientRegistryCallbacks() {}
}
