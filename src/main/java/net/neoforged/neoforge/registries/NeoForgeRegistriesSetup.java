/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class NeoForgeRegistriesSetup {
    private static boolean setup = false;

    /**
     * Internal forge method. Modders do not call.
     */
    public static synchronized void setup(IEventBus modEventBus) {
        if (setup)
            throw new IllegalStateException("Setup has already been called!");

        setup = true;

        modEventBus.addListener(NeoForgeRegistriesSetup::registerRegistries);
        modEventBus.addListener(NeoForgeRegistriesSetup::onModifyRegistry);
    }

    /**
     * The set of vanilla registries which should be synced to the client.
     */
    private static final Set<ResourceKey<? extends Registry<?>>> VANILLA_SYNC_KEYS = Set.of(
            Registries.SOUND_EVENT, // Required for SoundEvent packets
            Registries.MOB_EFFECT, // Required for MobEffect packets
            Registries.BLOCK, // Required for chunk BlockState paletted containers syncing
            Registries.ENCHANTMENT, // Required for EnchantmentMenu syncing
            Registries.ENTITY_TYPE, // Required for Entity spawn packets
            Registries.ITEM, // Required for Item/ItemStack packets
            Registries.PARTICLE_TYPE, // Required for ParticleType packets
            Registries.BLOCK_ENTITY_TYPE, // Required for BlockEntity packets
            Registries.PAINTING_VARIANT, // Required for EntityDataSerializers
            Registries.MENU, // Required for ClientboundOpenScreenPacket
            Registries.COMMAND_ARGUMENT_TYPE, // Required for ClientboundCommandsPacket
            Registries.STAT_TYPE, // Required for ClientboundAwardStatsPacket
            Registries.VILLAGER_TYPE, // Required for EntityDataSerializers
            Registries.VILLAGER_PROFESSION, // Required for EntityDataSerializers
            Registries.CAT_VARIANT, // Required for EntityDataSerializers
            Registries.FROG_VARIANT // Required for EntityDataSerializers
    );

    private static void registerRegistries(NewRegistryEvent event) {
        event.register(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS);
        event.register(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS);
        event.register(NeoForgeRegistries.BIOME_MODIFIER_SERIALIZERS);
        event.register(NeoForgeRegistries.STRUCTURE_MODIFIER_SERIALIZERS);
        event.register(NeoForgeRegistries.FLUID_TYPES);
        event.register(NeoForgeRegistries.HOLDER_SET_TYPES);
        event.register(NeoForgeRegistries.DISPLAY_CONTEXTS);
        event.register(NeoForgeRegistries.INGREDIENT_TYPES);
        event.register(NeoForgeRegistries.CONDITION_SERIALIZERS);
        event.register(NeoForgeRegistries.ITEM_PREDICATE_SERIALIZERS);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void onModifyRegistry(ModifyRegistryEvent event) {
        if (!event.isBuiltin() || !(event.getRegistry() instanceof BaseMappedRegistry<?> forgeRegistry))
            return;

        ResourceKey<? extends Registry<?>> registryKey = event.getRegistryKey();

        if (VANILLA_SYNC_KEYS.contains(registryKey))
            forgeRegistry.setSync(true);

        if (registryKey == Registries.BLOCK) {
            ((BaseMappedRegistry) forgeRegistry).addCallback(NeoForgeRegistryCallbacks.BlockCallbacks.INSTANCE);
        } else if (registryKey == Registries.ITEM) {
            ((BaseMappedRegistry) forgeRegistry).addCallback(NeoForgeRegistryCallbacks.ItemCallbacks.INSTANCE);
        } else if (registryKey == Registries.ATTRIBUTE) {
            ((BaseMappedRegistry) forgeRegistry).addCallback(NeoForgeRegistryCallbacks.AttributeCallbacks.INSTANCE);
        } else if (registryKey == Registries.POINT_OF_INTEREST_TYPE) {
            ((BaseMappedRegistry) forgeRegistry).addCallback(NeoForgeRegistryCallbacks.PoiTypeCallbacks.INSTANCE);
        } else if (registryKey == NeoForgeRegistries.Keys.DISPLAY_CONTEXTS) {
            // We add this callback here to not cause a tricky classloading loop with ForgeRegistries#DISPLAY_CONTEXTS and ItemDisplayContext#CODEC
            ((BaseMappedRegistry) forgeRegistry).addCallback(ItemDisplayContext.ADD_CALLBACK);
        }
    }
}
