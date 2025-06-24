/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.energy;

import com.google.common.math.IntMath;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.transfer.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.handlers.templates.contexts.PlayerItemContext;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * Variant of {@link EnergyBufferAttachment} for use with data components.
 * <p>
 * The actual data storage is managed by a data component, and all changes will write back to that component.
 * <p>
 * To use this class, register a new {@link DataComponentType} which holds an {@link Integer} for your item.
 * Then reference that component from your when registering the item with a capability. Something like the following:
 *
 * <pre>
 * {@code
 * capabilityRegistryEvent.registerItem(
 *         Capabilities.EnergyHandler.ITEM,
 *         EnergyBufferComponentHandler.builder(someCapacity, YourComponent)::build,
 *         YourItem);
 * }
 * </pre>
 */
public final class EnergyBufferComponentHandler implements IEnergyHandler {
    private final IItemContext itemContext;
    private final DataComponentType<Integer> componentType;

    // These are not handled by the component and are constant upon the handler creation.
    // If you'd like to make this controlled by ComponentData, then a different implementation would be required.
    private final int capacityOfOneItem;
    private final int maxInsert;
    private final int maxExtract;

    /**
     * Creates a new ComponentEnergyStorage with a data component as the backing store for the energy value.
     *
     * @param itemContext       The context controlling how the handler should be used such as {@link PlayerItemContext}
     * @param componentType     The data component referencing the stored energy of the item stack
     * @param capacityOfOneItem The max capacity of the energy being stored
     * @param maxInsert         The max per-transfer power input rate
     * @param maxExtract        The max per-transfer power output rate
     */
    public EnergyBufferComponentHandler(IItemContext itemContext, DataComponentType<Integer> componentType, int capacityOfOneItem, int maxInsert, int maxExtract) {
        this.itemContext = itemContext;
        this.componentType = componentType;
        this.capacityOfOneItem = capacityOfOneItem;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
    }

    private int getIndividualAmount() {
        return itemContext.getResource().getOrDefault(componentType, 0);
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        if (EnergyHandlerUtil.checkEnergy(amount)) return 0;
        if (maxInsert == 0) return 0;
        if (itemContext.getAmount() == 0) return 0;

        int stackedAmount = IntMath.saturatedMultiply(maxInsert, itemContext.getAmount());
        amount = Math.min(amount, stackedAmount);

        int currentOfOne = getIndividualAmount();
        int spaceLeft = capacityOfOneItem - currentOfOne;
        if (spaceLeft == 0) return 0;

        if (amount < spaceLeft) {
            return setPartial(amount + currentOfOne, transaction) == 1 ? amount : 0;
        }
        return IntMath.saturatedMultiply(setFull(amount / spaceLeft, transaction), spaceLeft);
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        if (EnergyHandlerUtil.checkEnergy(amount)) return 0;
        if (maxExtract == 0) return 0;
        if (itemContext.getAmount() == 0) return 0;

        int stackedAmount = IntMath.saturatedMultiply(maxExtract, itemContext.getAmount());
        amount = Math.min(amount, stackedAmount);

        int currentOfOne = getIndividualAmount();
        if (currentOfOne == 0) return 0;

        if (amount < currentOfOne) {
            return setPartial(currentOfOne - amount, transaction) == 1 ? amount : 0;
        }

        //check to see if this can overflow
        return IntMath.saturatedMultiply(empty(amount / currentOfOne, transaction), currentOfOne);
    }

    private int empty(int count, TransactionContext context) {
        ItemResource emptiedContainer = itemContext.getResource().without(componentType);
        return itemContext.exchange(emptiedContainer, count, context);
    }

    private int setFull(int count, TransactionContext context) {
        ItemResource filledContainer = itemContext.getResource().with(componentType, capacityOfOneItem);
        return itemContext.exchange(filledContainer, count, context);
    }

    private int setPartial(int amount, TransactionContext context) {
        ItemResource filledContainer = itemContext.getResource().with(componentType, amount);
        return itemContext.exchange(filledContainer, 1, context);
    }

    @Override
    public int getAmount() {
        int rawEnergy = getIndividualAmount();
        return IntMath.saturatedMultiply(Math.min(rawEnergy, capacityOfOneItem), itemContext.getAmount());
    }

    @Override
    public long getAmountAsLong() {
        int currentOfOne = getIndividualAmount();
        return (long) Math.min(currentOfOne, capacityOfOneItem) * itemContext.getAmount();
    }

    @Override
    public int getCapacity() {
        return IntMath.saturatedMultiply(capacityOfOneItem, itemContext.getAmount());
    }

    @Override
    public long getCapacityAsLong() {
        return (long) capacityOfOneItem * itemContext.getAmount();
    }

    @Override
    public boolean supportsInsertion() {
        return maxInsert > 0;
    }

    @Override
    public boolean supportsExtraction() {
        return maxExtract > 0;
    }

    /**
     * Creates a builder of a specified capacity. This is the advised way to make an {@link EnergyBufferComponentHandler}.
     * An important note, is by default the transfer rate is 1% of the capacity (but never less than 1).
     * This it to help make a simple feeling of something charging
     *
     * @param capacityOfOneItem How much energy the buffer is set to be able to hold for a single item.
     * @return Chainable builder to allow creation of a new {@link EnergyBufferComponentHandler}
     */
    public static Builder builder(int capacityOfOneItem, Supplier<DataComponentType<Integer>> energyComponentSupplier) {
        return builder(capacityOfOneItem, energyComponentSupplier.get());
    }

    public static Builder builder(int capacity, DataComponentType<Integer> energyComponent) {
        return new Builder(energyComponent).capacity(capacity).maxExtractRate(capacity).maxInsertRate(Mth.ceil(capacity * 0.01f));
    }

    public static class Builder {
        private final DataComponentType<Integer> componentType;
        private int capacity;
        private int maxInsertRate;
        private int maxExtractRate;

        private Builder(DataComponentType<Integer> componentType) {
            Objects.requireNonNull(componentType, "component type must be set");
            this.componentType = componentType;
        }

        /**
         * @param capacity How much energy the buffer can hold.
         */
        private Builder capacity(int capacity) {
            if (capacity < 0) throw new IllegalArgumentException("capacity must be non-negative");
            this.capacity = capacity;
            return this;
        }

        /**
         * @param rate How much energy can be inserted in a single call.
         */
        public Builder maxInsertRate(int rate) {
            if (maxInsertRate < 0) throw new IllegalArgumentException("maxInsertRate must be non-negative");
            this.maxInsertRate = rate;
            return this;
        }

        /**
         * @param rate How much energy can be extracted in a single call.
         */
        public Builder maxExtractRate(int rate) {
            if (maxExtractRate < 0) throw new IllegalArgumentException("maxExtractRate must be non-negative");
            this.maxExtractRate = rate;
            return this;
        }

        /**
         * @param rate How much energy can be inserted or extracted in a single call.
         */
        public Builder maxTransferRate(int rate) {
            return maxExtractRate(rate).maxInsertRate(rate);
        }

        /**
         * Constructs a new {@link EnergyBufferAttachment} to use.
         *
         * @param ignored     The ItemStack that is usually provided with the capability, but in this case it is ignored for easier construction.
         * @param itemContext The context that handles exchanging the component data upon mutation.
         */
        public EnergyBufferComponentHandler build(MutableDataComponentHolder ignored, @Nullable IItemContext itemContext) {
            //the holder is ignored to allow calling builder as a reference
            //itemContext is nullable to alleviate the ide warning the user using it as a reference. The IItemContext should never be null.
            return new EnergyBufferComponentHandler(Objects.requireNonNull(itemContext), componentType, capacity, maxInsertRate, maxExtractRate);
        }
    }
}
