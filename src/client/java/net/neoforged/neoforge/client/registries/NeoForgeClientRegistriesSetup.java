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
    }

    private static void modifyRegistries(ModifyClientRegistriesEvent event) {
        event.getRegistry(ClientRegistries.BLOCK_ENTITY_RENDERER).addCallback(NeoForgeClientRegistryCallbacks.BlockEntityRendererCallbacks.INSTANCE);
        event.getRegistry(ClientRegistries.ENTITY_RENDERER).addCallback(NeoForgeClientRegistryCallbacks.EntityRendererCallbacks.INSTANCE);
        event.getRegistry(ClientRegistries.MENU_SCREEN).addCallback(NeoForgeClientRegistryCallbacks.MenuScreenCallbacks.INSTANCE);
        event.getRegistry(ClientRegistries.PARTICLE_PROVIDER).addCallback(NeoForgeClientRegistryCallbacks.ParticleProviderCallbacks.INSTANCE);
    }

    private NeoForgeClientRegistriesSetup() {}
}
