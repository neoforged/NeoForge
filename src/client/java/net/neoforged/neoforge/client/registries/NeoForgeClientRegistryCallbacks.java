package net.neoforged.neoforge.client.registries;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.callback.AddCallback;

import java.util.Locale;

final class NeoForgeClientRegistryCallbacks {
    static final class BlockEntityRendererCallbacks implements AddCallback<BlockEntityRendererProvider<?, ?>> {
        static final BlockEntityRendererCallbacks INSTANCE = new BlockEntityRendererCallbacks();

        @Override
        public void onAdd(Registry<BlockEntityRendererProvider<?, ?>> registry, int id, ResourceKey<BlockEntityRendererProvider<?, ?>> key, BlockEntityRendererProvider<?, ?> value) {
            assertHasCommonCounterpart(registry, BuiltInRegistries.BLOCK_ENTITY_TYPE, key.identifier());
        }
    }

    static final class EntityRendererCallbacks implements AddCallback<EntityRendererProvider<?>> {
        static final EntityRendererCallbacks INSTANCE = new EntityRendererCallbacks();

        @Override
        public void onAdd(Registry<EntityRendererProvider<?>> registry, int id, ResourceKey<EntityRendererProvider<?>> key, EntityRendererProvider<?> value) {
            assertHasCommonCounterpart(registry, BuiltInRegistries.ENTITY_TYPE, key.identifier());
        }
    }

    static final class MenuScreenCallbacks implements AddCallback<MenuScreens.ScreenConstructor<?, ?>> {
        static final MenuScreenCallbacks INSTANCE = new MenuScreenCallbacks();

        @Override
        public void onAdd(Registry<MenuScreens.ScreenConstructor<?, ?>> registry, int id, ResourceKey<MenuScreens.ScreenConstructor<?, ?>> key, MenuScreens.ScreenConstructor<?, ?> value) {
            assertHasCommonCounterpart(registry, BuiltInRegistries.MENU, key.identifier());
        }
    }

    static final class ParticleProviderCallbacks implements AddCallback<ParticleProvider<?>> {
        static final ParticleProviderCallbacks INSTANCE = new ParticleProviderCallbacks();

        @Override
        public void onAdd(Registry<ParticleProvider<?>> registry, int id, ResourceKey<ParticleProvider<?>> key, ParticleProvider<?> value) {
            assertHasCommonCounterpart(registry, BuiltInRegistries.PARTICLE_TYPE, key.identifier());
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
