/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.neoforge.transfer.items.ItemResource;
import org.jetbrains.annotations.Nullable;

/**
 * Capabilities provided by NeoForge itself, for modders to directly reference.
 */
public final class Capabilities {
    public static final class EnergyHandler {
        public static final BlockCapability<IEnergyHandler, @Nullable Direction> BLOCK = BlockCapability.createSided(create("energy"), IEnergyHandler.class);
        public static final EntityCapability<IEnergyHandler, @Nullable Direction> ENTITY = EntityCapability.createSided(create("energy"), IEnergyHandler.class);
        public static final ItemCapability<IEnergyHandler, IItemContext> ITEM = ItemCapability.createContextual(create("energy"), IEnergyHandler.class);

        private EnergyHandler() {}
    }

    public static final class FluidHandler {
        public static final BlockCapability<IResourceHandler<FluidResource>, @Nullable Direction> BLOCK = BlockCapability.createSided(create("fluid_handler"), IResourceHandler.asClass());
        public static final EntityCapability<IResourceHandler<FluidResource>, @Nullable Direction> ENTITY = EntityCapability.createSided(create("fluid_handler"), IResourceHandler.asClass());
        public static final ItemCapability<IResourceHandler<FluidResource>, IItemContext> ITEM = ItemCapability.createContextual(create("fluid_handler"), IResourceHandler.asClass());

        private FluidHandler() {}
    }

    public static final class ItemHandler {
        public static final BlockCapability<IResourceHandler<ItemResource>, @Nullable Direction> BLOCK = BlockCapability.createSided(create("item_handler"), IResourceHandler.asClass());
        /**
         * Capability for the inventory of an entity.
         * If an entity has multiple inventory "subparts", this capability should give a combined view of all the subparts.
         */
        public static final EntityCapability<IResourceHandler<ItemResource>, Void> ENTITY = EntityCapability.createVoid(create("item_handler"), IResourceHandler.asClass());
        /**
         * Capability for an inventory of entity that should be accessible to automation,
         * in the sense that droppers, hoppers, and similar modded devices will try to use it.
         */
        public static final EntityCapability<IResourceHandler<ItemResource>, @Nullable Direction> ENTITY_AUTOMATION = EntityCapability.createSided(create("item_handler_automation"), IResourceHandler.asClass());
        public static final ItemCapability<IResourceHandler<ItemResource>, IItemContext> ITEM = ItemCapability.createContextual(create("item_handler"), IResourceHandler.asClass());

        private ItemHandler() {}
    }

    private static ResourceLocation create(String path) {
        return ResourceLocation.fromNamespaceAndPath("neoforge", path);
    }

    private Capabilities() {}
}
