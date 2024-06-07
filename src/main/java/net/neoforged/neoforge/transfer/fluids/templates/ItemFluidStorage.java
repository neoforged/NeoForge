/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluids.templates;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.handlers.ISingleResourceHandler;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ItemFluidStorage implements ISingleResourceHandler<FluidResource> {
    private final int individualLimit;
    private final Supplier<DataComponentType<SimpleFluidContent>> componentType;
    protected final IItemContext context;
    private Predicate<FluidResource> validator = r -> true;

    public ItemFluidStorage(int limit, Supplier<DataComponentType<SimpleFluidContent>> componentType, IItemContext context) {
        this.individualLimit = limit;
        this.componentType = componentType;
        this.context = context;
    }

    public ItemFluidStorage setValidator(Predicate<FluidResource> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public FluidResource getResource() {
        return context.getResource().getOrDefault(componentType, SimpleFluidContent.EMPTY).getResource();
    }

    @Override
    public int getAmount() {
        return getIndividualAmount() * context.getAmount();
    }

    public int getIndividualAmount() {
        return context.getResource().getOrDefault(componentType, SimpleFluidContent.EMPTY).getAmount();
    }

    public int getIndividualLimit() {
        return individualLimit;
    }

    @Override
    public int getLimit(FluidResource resource) {
        return getIndividualLimit() * context.getAmount();
    }

    @Override
    public boolean isValid(FluidResource resource) {
        return validator.test(resource);
    }

    public boolean isEmpty() {
        return !context.getResource().has(componentType);
    }

    @Override
    public boolean canInsert() {
        return true;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public int insert(FluidResource resource, int amount, TransferAction action) {
        if (resource.isBlank() || amount <= 0 || !isValid(resource)) return 0;
        FluidResource presentResource = getResource();
        if (presentResource.isBlank()) {
            if (amount < getIndividualLimit()) return setPartial(resource, amount, action) == 1 ? amount : 0;
            return setFull(resource, amount / getIndividualLimit(), action) * getIndividualLimit();
        }

        if (!presentResource.equals(resource)) return 0;

        int containerFill = getIndividualAmount();
        int spaceLeft = getIndividualLimit() - containerFill;
        if (amount < spaceLeft) return setPartial(resource, amount + containerFill, action) == 1 ? amount : 0;
        return setFull(resource, amount / spaceLeft, action) * spaceLeft;
    }

    @Override
    public int extract(FluidResource resource, int amount, TransferAction action) {
        if (resource.isBlank() || amount <= 0 || isEmpty() || !getResource().equals(resource)) return 0;
        int containerFill = getIndividualAmount();
        if (amount < containerFill) {
            int exchanged = setPartial(resource, containerFill - amount, action);
            return exchanged == 1 ? amount : 0;
        } else {
            int extractedCount = amount / containerFill;
            int exchanged = empty(extractedCount, action);
            return exchanged * containerFill;
        }
    }

    protected int empty(int count, TransferAction action) {
        ItemResource emptiedContainer = context.getResource().remove(componentType);
        return context.exchange(emptiedContainer, count, action);
    }

    protected int setFull(FluidResource resource, int count, TransferAction action) {
        ItemResource filledContainer = context.getResource().set(componentType, SimpleFluidContent.of(resource, getIndividualLimit()));
        return context.exchange(filledContainer, count, action);
    }

    protected int setPartial(FluidResource resource, int amount, TransferAction action) {
        ItemResource filledContainer = context.getResource().set(componentType, SimpleFluidContent.of(resource, amount));
        return context.exchange(filledContainer, 1, action);
    }

    public static class Consumable extends ItemFluidStorage {
        public Consumable(int limit, Supplier<DataComponentType<SimpleFluidContent>> componentType, IItemContext context) {
            super(limit, componentType, context);
        }

        @Override
        protected int empty(int count, TransferAction action) {
            return context.extract(context.getResource(), count, action);
        }
    }

    public static class SwapEmpty extends ItemFluidStorage {
        private final ItemResource emptyContainer;

        public SwapEmpty(int limit, Supplier<DataComponentType<SimpleFluidContent>> componentType, IItemContext context, ItemResource emptyContainer) {
            super(limit, componentType, context);
            this.emptyContainer = emptyContainer;
        }

        @Override
        protected int empty(int count, TransferAction action) {
            return context.exchange(emptyContainer, count, action);
        }
    }
}
