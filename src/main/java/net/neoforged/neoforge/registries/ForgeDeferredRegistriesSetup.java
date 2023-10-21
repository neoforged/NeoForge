/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import net.neoforged.bus.api.IEventBus;

public class ForgeDeferredRegistriesSetup
{
    private static boolean setup = false;

    /**
     * Internal forge method. Modders do not call.
     */
    public static void setup(IEventBus modEventBus)
    {
        synchronized (ForgeDeferredRegistriesSetup.class)
        {
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
    }
}
