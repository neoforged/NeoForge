package net.neoforged.neoforge.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.configuration.SyncConfig;
import net.neoforged.neoforge.network.configuration.SyncRegistries;
import net.neoforged.neoforge.network.configuration.SyncTierSortingRegistry;
import net.neoforged.neoforge.network.event.OnGameConfigurationEvent;

@Mod.EventBusSubscriber(modid = "neoforge", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigurationInitialization {
    
    @SubscribeEvent
    public static void onGameConfiguration(OnGameConfigurationEvent event) {
        event.register(new SyncRegistries());
        event.register(new SyncConfig(event.getListener()));
        event.register(new SyncTierSortingRegistry(event.getListener()));
    }
}
