/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import org.jetbrains.annotations.Nullable;

final class HandlerTestUtil {
    @Nullable
    static final SlotInfo<TestResource> EMPTY_SLOT = null;
    @Nullable
    static final ResourceStack<TestResource> EMPTY_STACK = null;

    private HandlerTestUtil() {}

    static <T extends Resource> List<SlotInfo<T>> describeSlots(ResourceHandler<T> handler) {
        List<SlotInfo<T>> content = new ArrayList<>(handler.size());
        for (int i = 0; i < handler.size(); i++) {
            T resource = handler.getResource(i);
            long amount = handler.getAmountAsLong(i);
            long capacity = handler.getCapacityAsLong(i, resource);
            if (!resource.isEmpty() && amount > 0) {
                content.add(new SlotInfo<>(resource, amount, capacity));
            } else {
                content.add(null);
            }
        }
        return content;
    }

    static <T extends Resource> List<ResourceStack<T>> describeStacks(ResourceHandler<T> handler) {
        List<ResourceStack<T>> content = new ArrayList<>(handler.size());
        for (int i = 0; i < handler.size(); i++) {
            T resource = handler.getResource(i);
            long amount = handler.getAmountAsLong(i);
            if (!resource.isEmpty() && amount > 0) {
                if (amount > Integer.MAX_VALUE) {
                    throw new IllegalStateException("Cannot use describeStacks. Index " + i + " contains more than Integer.MAX_VALUE");
                }
                content.add(new ResourceStack<>(resource, (int) amount));
            } else {
                content.add(null);
            }
        }
        return content;
    }

    @SafeVarargs
    static MockResourceHandler handler(@Nullable SlotInfo<TestResource> firstSlot, SlotInfo<TestResource>... slots) {
        MockResourceHandler handler = new MockResourceHandler(1 + slots.length);
        if (firstSlot != null) {
            handler.set(0, firstSlot.resource(), firstSlot.amount());
            handler.setCapacity(0, firstSlot.capacity());
        }
        for (int i = 0; i < slots.length; i++) {
            var slot = slots[i];
            if (slot != null) {
                handler.set(1 + i, slot.resource(), slot.amount());
                handler.setCapacity(1 + i, slot.capacity());
            }
        }
        return handler;
    }

    @SafeVarargs
    static MockResourceHandler handlerForStacks(ResourceStack<TestResource>... slots) {
        MockResourceHandler handler = new MockResourceHandler(slots.length);
        for (int i = 0; i < slots.length; i++) {
            var stack = slots[i];
            if (stack != null) {
                handler.set(i, stack.resource(), stack.amount());
            }
        }
        return handler;
    }

    static SlotInfo<TestResource> slotInfo(TestResource resource, long amount) {
        return new SlotInfo<>(resource, amount, Long.MAX_VALUE);
    }

    static SlotInfo<TestResource> slotInfo(TestResource resource, long amount, long capacity) {
        return new SlotInfo<>(resource, amount, capacity);
    }

    static ResourceStack<TestResource> stack(TestResource resource, int amount) {
        return new ResourceStack<>(resource, amount);
    }

    record SlotInfo<T extends Resource>(T resource, long amount, long capacity) {
        @Override
        public String toString() {
            return amount + "x" + resource + " (cap: " + capacity + ")";
        }
    }
}
