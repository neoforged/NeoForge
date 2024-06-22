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
import net.neoforged.neoforge.transfer.handlers.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.items.ItemResource;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A simple fluid storage handler that uses a single item context to store a fluid resource.
 * An item with this handler can only ever be full or empty, and will not allow for partial fills or extractions.
 * This handler is designed to support stack
 */
public class SimpleItemFluidStorage implements ISingleResourceHandler<FluidResource> {
    private final int individualLimit;
    private final Supplier<DataComponentType<SimpleFluidContent>> componentType;
    protected final IItemContext context;
    private Predicate<FluidResource> validator = r -> true;

    public SimpleItemFluidStorage(int limit, Supplier<DataComponentType<SimpleFluidContent>> componentType, IItemContext context) {
        this.individualLimit = limit;
        this.componentType = componentType;
        this.context = context;
    }

    /**
     * Set a validator for the input fluid resource. This value will be used for {@link #isValid(FluidResource)} checks.
     * This method uses a builder pattern and will return the current instance for usage in a capability provider.
     * @param validator The validator to use for input fluid resources.
     * @return The current instance of this class.
     */
    public SimpleItemFluidStorage setValidator(Predicate<FluidResource> validator) {
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

    @Override
    public int getLimit(FluidResource resource) {
        return getIndividualLimit() * context.getAmount();
    }

    public int getIndividualAmount() {
        return context.getResource().getOrDefault(componentType, SimpleFluidContent.EMPTY).getAmount();
    }

    public int getIndividualLimit() {
        return individualLimit;
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
        if (resource.isBlank() || amount <= 0 || !isValid(resource) || !isEmpty()) return 0;
        int toFill = getIndividualLimit();
        if (amount < toFill) return 0;
        return fill(resource, amount / toFill, action) * toFill;
    }

    @Override
    public int extract(FluidResource resource, int amount, TransferAction action) {
        if (resource.isBlank() || amount <= 0 || isEmpty() || !getResource().equals(resource)) return 0;
        int containerFill = getIndividualAmount();
        if (amount > containerFill) {
            int extractedCount = amount / containerFill;
            int exchanged = empty(extractedCount, action);
            return exchanged * containerFill;
        }
        return 0;
    }

    protected int empty(int count, TransferAction action) {
        ItemResource emptiedContainer = context.getResource().remove(componentType);
        return context.exchange(emptiedContainer, count, action);
    }

    protected int fill(FluidResource resource, int count, TransferAction action) {
        ItemResource filledContainer = context.getResource().set(componentType, SimpleFluidContent.of(resource, getIndividualAmount()));
        return context.exchange(filledContainer, count, action);
    }

    public static class Consumable extends SimpleItemFluidStorage {
        public Consumable(int limit, Supplier<DataComponentType<SimpleFluidContent>> componentType, IItemContext context) {
            super(limit, componentType, context);
        }

        @Override
        protected int empty(int count, TransferAction action) {
            return context.extract(context.getResource(), count, action);
        }
    }

    public static class SwapEmpty extends SimpleItemFluidStorage {
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
