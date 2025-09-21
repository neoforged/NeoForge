/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

class MockResourceHandler extends SnapshotJournal<MockResourceHandler.State> implements ResourceHandler<TestResource> {
    private final TestResource[] resources;
    private final long[] amounts;
    private final int size;
    private final long[] capacities;
    private final Map<Integer, Set<TestResource>> validPerSlot = new HashMap<>();

    public MockResourceHandler(int size) {
        this.size = size;
        this.resources = new TestResource[size];
        this.capacities = new long[size];
        this.amounts = new long[size];
        Arrays.fill(this.resources, TestResource.EMPTY);
        Arrays.fill(this.capacities, Long.MAX_VALUE);
    }

    public void set(int index, TestResource resource, long amount) {
        resources[index] = resource;
        amounts[index] = amount;
    }

    public void setCapacity(long capacity) {
        Arrays.fill(capacities, capacity);
    }

    public void setCapacity(int index, long capacity) {
        capacities[index] = capacity;
    }

    public void setValid(int index, TestResource... testResources) {
        validPerSlot.put(index, Set.of(testResources));
    }

    public void setAllValid(int index) {
        validPerSlot.remove(index);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public TestResource getResource(int index) {
        return resources[index];
    }

    @Override
    public long getAmountAsLong(int index) {
        return amounts[index];
    }

    @Override
    public long getCapacityAsLong(int index, TestResource resource) {
        return capacities[index];
    }

    @Override
    public boolean isValid(int index, TestResource resource) {
        TransferPreconditions.checkNonEmpty(resource);

        var validSet = validPerSlot.get(index);
        if (validSet != null) {
            return validSet.contains(resource);
        }

        return true;
    }

    @Override
    public int insert(int index, TestResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        if (!isValid(index, resource)) {
            return 0;
        }

        if (resources[index].isEmpty() || resources[index].equals(resource)) {
            long currentAmount = amounts[index];
            long toInsert = Math.min(amount, capacities[index] - currentAmount);
            if (toInsert > 0) {
                updateSnapshots(transaction);
                resources[index] = resource;
                amounts[index] = currentAmount + toInsert;
            }
            return (int) toInsert;
        }
        return 0;
    }

    @Override
    public int extract(int index, TestResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        if (resources[index].equals(resource)) {
            updateSnapshots(transaction);
            long currentAmount = amounts[index];
            long toExtract = Math.min(amount, currentAmount);
            amounts[index] = currentAmount - toExtract;
            if (amounts[index] == 0) {
                resources[index] = TestResource.EMPTY;
            }
            return (int) toExtract;
        }
        return 0;
    }

    @Override
    protected State createSnapshot() {
        return new State(resources.clone(), amounts.clone());
    }

    @Override
    protected void revertToSnapshot(State snapshot) {
        System.arraycopy(snapshot.resources, 0, resources, 0, size);
        System.arraycopy(snapshot.amounts, 0, amounts, 0, size);
    }

    record State(TestResource[] resources, long[] amounts) {}
}
