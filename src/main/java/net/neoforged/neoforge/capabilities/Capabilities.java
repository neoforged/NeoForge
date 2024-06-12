/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.color.IColorable;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Capabilities provided by NeoForge itself, for modders to directly reference.
 */
public final class Capabilities {
    public static final class EnergyStorage {
        public static final BlockCapability<IEnergyStorage, @Nullable Direction> BLOCK = BlockCapability.createSided(create("energy"), IEnergyStorage.class);
        public static final EntityCapability<IEnergyStorage, @Nullable Direction> ENTITY = EntityCapability.createSided(create("energy"), IEnergyStorage.class);
        public static final ItemCapability<IEnergyStorage, Void> ITEM = ItemCapability.createVoid(create("energy"), IEnergyStorage.class);

        private EnergyStorage() {}
    }

    public static final class FluidHandler {
        public static final BlockCapability<IFluidHandler, @Nullable Direction> BLOCK = BlockCapability.createSided(create("fluid_handler"), IFluidHandler.class);
        public static final EntityCapability<IFluidHandler, @Nullable Direction> ENTITY = EntityCapability.createSided(create("fluid_handler"), IFluidHandler.class);
        public static final ItemCapability<IFluidHandlerItem, Void> ITEM = ItemCapability.createVoid(create("fluid_handler"), IFluidHandlerItem.class);

        private FluidHandler() {}
    }

    public static final class ItemHandler {
        public static final BlockCapability<IItemHandler, @Nullable Direction> BLOCK = BlockCapability.createSided(create("item_handler"), IItemHandler.class);
        /**
         * Capability for the inventory of an entity.
         * If an entity has multiple inventory "subparts", this capability should give a combined view of all the subparts.
         */
        public static final EntityCapability<IItemHandler, Void> ENTITY = EntityCapability.createVoid(create("item_handler"), IItemHandler.class);
        /**
         * Capability for an inventory of entity that should be accessible to automation,
         * in the sense that droppers, hoppers, and similar modded devices will try to use it.
         */
        public static final EntityCapability<IItemHandler, @Nullable Direction> ENTITY_AUTOMATION = EntityCapability.createSided(create("item_handler_automation"), IItemHandler.class);
        public static final ItemCapability<IItemHandler, Void> ITEM = ItemCapability.createVoid(create("item_handler"), IItemHandler.class);

        private ItemHandler() {}
    }

    public static final class Colorable {
        public static final BlockCapability<IColorable, BlockHitResult> BLOCK = BlockCapability.createHitResult(create("colorable"), IColorable.class);

        public static final EntityCapability<IColorable, Void> ENTITY = EntityCapability.createVoid(create("colorable"), IColorable.class);

        public static final ItemCapability<IColorable, Void> ITEM = ItemCapability.createVoid(create("colorable"), IColorable.class);
    }

    private static ResourceLocation create(String path) {
        return ResourceLocation.fromNamespaceAndPath("neoforge", path);
    }

    private Capabilities() {}
}
