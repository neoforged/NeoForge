/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class NeoForgeRegistriesSetup {
    private static boolean setup = false;

    public static synchronized void setup(IEventBus modEventBus) {
        if (setup)
            throw new IllegalStateException("Setup has already been called!");

        setup = true;

        modEventBus.addListener(NeoForgeRegistriesSetup::registerRegistries);
        modEventBus.addListener(NeoForgeRegistriesSetup::modifyRegistries);
    }

    /**
     * The set of vanilla registries which should be synced to the client.
     */
    private static final Set<Registry<?>> VANILLA_SYNC_REGISTRIES = Set.of(
            BuiltInRegistries.SOUND_EVENT, // Required for SoundEvent packets
            BuiltInRegistries.MOB_EFFECT, // Required for MobEffect packets
            BuiltInRegistries.BLOCK, // Required for chunk BlockState paletted containers syncing
            BuiltInRegistries.ENCHANTMENT, // Required for EnchantmentMenu syncing
            BuiltInRegistries.ENTITY_TYPE, // Required for Entity spawn packets
            BuiltInRegistries.ITEM, // Required for Item/ItemStack packets
            BuiltInRegistries.FLUID, // Required for Fluid/FluidStack packets
            BuiltInRegistries.PARTICLE_TYPE, // Required for ParticleType packets
            BuiltInRegistries.BLOCK_ENTITY_TYPE, // Required for BlockEntity packets
            BuiltInRegistries.PAINTING_VARIANT, // Required for EntityDataSerializers
            BuiltInRegistries.MENU, // Required for ClientboundOpenScreenPacket
            BuiltInRegistries.COMMAND_ARGUMENT_TYPE, // Required for ClientboundCommandsPacket
            BuiltInRegistries.STAT_TYPE, // Required for ClientboundAwardStatsPacket
            BuiltInRegistries.VILLAGER_TYPE, // Required for EntityDataSerializers
            BuiltInRegistries.VILLAGER_PROFESSION, // Required for EntityDataSerializers
            BuiltInRegistries.CAT_VARIANT, // Required for EntityDataSerializers
            BuiltInRegistries.FROG_VARIANT, // Required for EntityDataSerializers
            BuiltInRegistries.DATA_COMPONENT_TYPE // Required for itemstack sync
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
        event.register(NeoForgeRegistries.ATTACHMENT_TYPES);
    }

    private static void modifyRegistries(ModifyRegistriesEvent event) {
        for (var registry : VANILLA_SYNC_REGISTRIES) {
            ((BaseMappedRegistry<?>) registry).setSync(true);
        }

        BuiltInRegistries.BLOCK.addCallback(NeoForgeRegistryCallbacks.BlockCallbacks.INSTANCE);
        BuiltInRegistries.ITEM.addCallback(NeoForgeRegistryCallbacks.ItemCallbacks.INSTANCE);
        BuiltInRegistries.ATTRIBUTE.addCallback(NeoForgeRegistryCallbacks.AttributeCallbacks.INSTANCE);
        BuiltInRegistries.POINT_OF_INTEREST_TYPE.addCallback(NeoForgeRegistryCallbacks.PoiTypeCallbacks.INSTANCE);
        // We add this callback here to not cause a tricky classloading loop with ForgeRegistries#DISPLAY_CONTEXTS and ItemDisplayContext#CODEC
        NeoForgeRegistries.DISPLAY_CONTEXTS.addCallback(ItemDisplayContext.ADD_CALLBACK);
    }
}
