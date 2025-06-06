/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.resources;

import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.templates.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

/**
 * This is more of a demonstration, not an implementation that should necessarily be used, that will be a followup PR that will handle typeless, primitive value based handlers.
 * This is just to showcase we don't need an entire separate system
 * for energy or other single value amounts, we can just use a singleton resource type such as {@link EnergyUnit}.
 * Any instruction call we can assume the resource is the instance.
 * <p>
 * The added benefit, is that any improvements one system gets, the other shall inherit
 */
public class EnergyBuffer implements ISingleResourceHandler<EnergyUnit> {
    /**
     * A immutable capacity once the buffer is initialized.
     */
    public final int capacity;

    /**
     * How much energy the current buffer has
     */
    public int amount;

    public EnergyBuffer(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public EnergyUnit getResource(int index) {
        return EnergyUnit.INSTANCE;
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public int insert(EnergyUnit resource, @Range(from = 1, to = ResourceHandlerUtil.MAX) int amount, TransactionContext context) {
        var inserted = Math.min(capacity - this.amount, amount);
        //todo take snapshot
        this.amount += inserted;
        return inserted;
    }

    @Override
    public int extract(EnergyUnit resource, @Range(from = 1, to = ResourceHandlerUtil.MAX) int amount, TransactionContext context) {
        int extracted = Math.min(amount, this.amount);
        //todo take snapshot
        this.amount -= extracted;
        return extracted;
    }

    @Override
    public int getAmount(int ignoredIndex) {
        return amount;
    }

    //We need to return capacity in two different method overrides since the ResourceHandler is designed around an unknown number of resources per type.
    //Since energy is only ever 1 singular instance, we can ignore the resource
    @Override
    public int getCapacity(int index, EnergyUnit resource) {
        return capacity;
    }

    @Override
    public boolean isValid(int index, EnergyUnit resource) {
        return true;
    }
}
