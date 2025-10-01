/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

/**
 * Capabilities provided by NeoForge itself, for modders to directly reference.
 */
public final class Capabilities {
    public static final class Energy {
        public static final BlockCapability<EnergyHandler, @Nullable Direction> BLOCK = BlockCapability.createSided(create("energy_handler"), EnergyHandler.class);
        public static final EntityCapability<EnergyHandler, @Nullable Direction> ENTITY = EntityCapability.createSided(create("energy_handler"), EnergyHandler.class);
        public static final ItemCapability<EnergyHandler, ItemAccess> ITEM = ItemCapability.create(create("energy_handler"), EnergyHandler.class, ItemAccess.class);

        private Energy() {}
    }

    public static final class Fluid {
        public static final BlockCapability<ResourceHandler<FluidResource>, @Nullable Direction> BLOCK = BlockCapability.createSided(create("fluid_handler"), ResourceHandler.asClass());
        public static final EntityCapability<ResourceHandler<FluidResource>, @Nullable Direction> ENTITY = EntityCapability.createSided(create("fluid_handler"), ResourceHandler.asClass());
        public static final ItemCapability<ResourceHandler<FluidResource>, ItemAccess> ITEM = ItemCapability.create(create("fluid_handler"), ResourceHandler.asClass(), ItemAccess.class);

        private Fluid() {}
    }

    public static final class Item {
        public static final BlockCapability<ResourceHandler<ItemResource>, @Nullable Direction> BLOCK = BlockCapability.createSided(create("item_handler"), ResourceHandler.asClass());
        /**
         * Capability for the inventory of an entity.
         * If an entity has multiple inventory "subparts", this capability should give a combined view of all the subparts.
         */
        public static final EntityCapability<ResourceHandler<ItemResource>, @Nullable Void> ENTITY = EntityCapability.createVoid(create("item_handler"), ResourceHandler.asClass());
        /**
         * Capability for an inventory of entity that should be accessible to automation,
         * in the sense that droppers, hoppers, and similar modded devices will try to use it.
         */
        public static final EntityCapability<ResourceHandler<ItemResource>, @Nullable Direction> ENTITY_AUTOMATION = EntityCapability.createSided(create("item_handler_automation"), ResourceHandler.asClass());
        public static final ItemCapability<ResourceHandler<ItemResource>, ItemAccess> ITEM = ItemCapability.create(create("item_handler"), ResourceHandler.asClass(), ItemAccess.class);

        private Item() {}
    }

    private static ResourceLocation create(String path) {
        return ResourceLocation.fromNamespaceAndPath("neoforge", path);
    }

    private Capabilities() {}
}
