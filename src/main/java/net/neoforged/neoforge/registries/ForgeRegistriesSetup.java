/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

@ApiStatus.Internal
public class ForgeRegistriesSetup {
    private static boolean setup = false;

    /**
     * Internal forge method. Modders do not call.
     */
    public static void setup(IEventBus modEventBus) {
        synchronized (ForgeRegistriesSetup.class) {
            if (setup)
                throw new IllegalStateException("Setup has already been called!");

            setup = true;
        }

        ForgeRegistries.DEFERRED_INGREDIENT_TYPES.register(modEventBus);
        ForgeRegistries.DEFERRED_CONDITION_CODECS.register(modEventBus);
        ForgeRegistries.DEFERRED_ITEM_PREDICATE_SERIALIZERS.register(modEventBus);
        ForgeRegistries.DEFERRED_ENTITY_DATA_SERIALIZERS.register(modEventBus);
        ForgeRegistries.DEFERRED_GLOBAL_LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
        ForgeRegistries.DEFERRED_BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
        ForgeRegistries.DEFERRED_FLUID_TYPES.register(modEventBus);
        ForgeRegistries.DEFERRED_STRUCTURE_MODIFIER_SERIALIZERS.register(modEventBus);
        ForgeRegistries.DEFERRED_HOLDER_SET_TYPES.register(modEventBus);
        ForgeRegistries.DEFERRED_DISPLAY_CONTEXTS.register(modEventBus);

        modEventBus.addListener(ForgeRegistriesSetup::onModifyRegistry);
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void onModifyRegistry(ModifyRegistryEvent event) {
        if (!event.isBuiltin() || !(event.getRegistry() instanceof BaseNeoRegistry<?> forgeRegistry))
            return;

        ResourceKey<? extends Registry<?>> registryKey = event.getRegistryKey();

        if (VANILLA_SYNC_KEYS.contains(registryKey))
            forgeRegistry.setSync(true);

        if (registryKey == Registries.BLOCK) {
            ((BaseNeoRegistry) forgeRegistry).addCallback(ForgeRegistryCallbacks.BlockCallbacks.INSTANCE);
        } else if (registryKey == Registries.ITEM) {
            ((BaseNeoRegistry) forgeRegistry).addCallback(ForgeRegistryCallbacks.ItemCallbacks.INSTANCE);
        } else if (registryKey == Registries.ATTRIBUTE) {
            ((BaseNeoRegistry) forgeRegistry).addCallback(ForgeRegistryCallbacks.AttributeCallbacks.INSTANCE);
        } else if (registryKey == Registries.POINT_OF_INTEREST_TYPE) {
            ((BaseNeoRegistry) forgeRegistry).addCallback(ForgeRegistryCallbacks.PoiTypeCallbacks.INSTANCE);
        } else if (registryKey == ForgeRegistries.Keys.DISPLAY_CONTEXTS) {
            // We add this callback here to not cause a tricky classloading loop with ForgeRegistries#DISPLAY_CONTEXTS and ItemDisplayContext#CODEC
            ((BaseNeoRegistry) forgeRegistry).addCallback(ItemDisplayContext.ADD_CALLBACK);
        }
    }
}
