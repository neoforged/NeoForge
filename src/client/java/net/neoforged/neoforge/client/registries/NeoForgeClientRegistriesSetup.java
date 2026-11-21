package net.neoforged.neoforge.client.registries;

import net.minecraft.client.core.registries.ClientRegistries;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class NeoForgeClientRegistriesSetup {
    private static boolean setup = false;

    public static synchronized void setup(IEventBus modEventBus) {
        if (setup) {
            throw new IllegalStateException("Setup has already been called!");
        }

        setup = true;

        modEventBus.addListener(NeoForgeClientRegistriesSetup::registerRegistries);
        modEventBus.addListener(NeoForgeClientRegistriesSetup::modifyRegistries);
    }

    private static void registerRegistries(NewClientRegistryEvent event) {
        event.register(NeoForgeClientRegistries.BLOCK_STATE_MODEL_DISPATCHER_TYPE);
        event.register(NeoForgeClientRegistries.BLOCK_STATE_MODEL_TYPE);
        event.register(NeoForgeClientRegistries.UNBAKED_MODEL_LOADER);
        event.register(NeoForgeClientRegistries.CUSTOM_CLOUDS_RENDERER);
        event.register(NeoForgeClientRegistries.CUSTOM_SKYBOX_RENDERER);
        event.register(NeoForgeClientRegistries.CUSTOM_WEATHER_EFFECT_RENDERER);
        event.register(NeoForgeClientRegistries.ANIMATION_CHANNEL_TARGET);
        event.register(NeoForgeClientRegistries.ANIMATION_CHANNEL_INTERPOLATION);
        event.register(NeoForgeClientRegistries.WORLD_PRESET_EDITOR);
        event.register(NeoForgeClientRegistries.MAP_DECORATION_RENDERER);
        event.register(NeoForgeClientRegistries.PIPELINE_MODIFIER);
    }

    private static void modifyRegistries(ModifyClientRegistriesEvent event) {
        event.getRegistry(ClientRegistries.BLOCK_ENTITY_RENDERER).addCallback(NeoForgeClientRegistryCallbacks.BlockEntityRendererCallbacks.INSTANCE);
        event.getRegistry(ClientRegistries.ENTITY_RENDERER).addCallback(NeoForgeClientRegistryCallbacks.EntityRendererCallbacks.INSTANCE);
        event.getRegistry(ClientRegistries.MENU_SCREEN).addCallback(NeoForgeClientRegistryCallbacks.MenuScreenCallbacks.INSTANCE);
        event.getRegistry(ClientRegistries.PARTICLE_PROVIDER).addCallback(NeoForgeClientRegistryCallbacks.ParticleProviderCallbacks.INSTANCE);
    }

    private NeoForgeClientRegistriesSetup() {}
}
