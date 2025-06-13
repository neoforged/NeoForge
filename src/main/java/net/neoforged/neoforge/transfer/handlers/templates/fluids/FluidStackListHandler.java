/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Objects;
import javax.annotation.Nonnegative;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.IResourceStack;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;

/**
 * This is provided as a simple handler to still use a {@link FluidStack} in a List as the backing data structure.
 * It is advised to use a {@link FluidResource} or similar form of {@link IResourceStack}.
 * <p>
 * This is expected to be used as an attachment.
 */
public final class FluidStackListHandler implements IResourceHandlerModifiable<FluidResource> {
    public static final MapCodec<FluidStackListHandler> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NonNullList.codecOf(FluidStack.OPTIONAL_CODEC).fieldOf("stacks").forGetter(data -> data.stacks),
            Codec.INT.fieldOf("capacity").forGetter(data -> data.capacity)).apply(instance, FluidStackListHandler::new));

    private final NonNullList<FluidStack> stacks;
    @Nonnegative
    private final int size;
    @Range(from = 0, to = Item.ABSOLUTE_MAX_STACK_SIZE)
    private final int capacity;
    private final ArrayList<StackJournal> journals = new ArrayList<>();

    public FluidStackListHandler(@Nonnegative int size, @Range(from = 0, to = Item.ABSOLUTE_MAX_STACK_SIZE) int capacity) {
        this(NonNullList.withSize(size, FluidStack.EMPTY), capacity);
    }

    private FluidStackListHandler(NonNullList<FluidStack> stacks, @Range(from = 0, to = Item.ABSOLUTE_MAX_STACK_SIZE) int capacity) {
        this.capacity = capacity;
        this.stacks = stacks;
        this.size = stacks.size();
        this.journals.ensureCapacity(size);
        for (int i = 0; i < size; i++) {
            journals.add(new StackJournal(i));
        }
    }

    /**
     * Copies all the contents of this handler to a non-null list of the same size.
     *
     * @return A new non-null list.
     */
    @Contract(pure = true)
    public NonNullList<FluidStack> copyToList() {
        NonNullList<FluidStack> list = NonNullList.withSize(size(), FluidStack.EMPTY);
        int size = size();
        for (int index = 0; index < size; index++) {
            list.set(index, stacks.get(index));
        }
        return list;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public FluidResource getResource(int index) {
        Objects.checkIndex(index, size());
        return FluidResource.of(stacks.get(index));
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        return stacks.get(index).getAmount();
    }

    @Override
    public int getCapacity(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        return capacity;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        return true;
    }

    @Override
    public boolean supportsInsertion(int index) {
        Objects.checkIndex(index, size());
        return true;
    }

    @Override
    public boolean supportsExtraction(int index) {
        Objects.checkIndex(index, size());
        return true;
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext context) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        int handled = 0;
        for (int index = 0; index < size; index++) {
            handled += insertBehaviour(index, resource, amount - handled, context);
            if (handled == amount)
                break;
        }
        return handled;
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size());
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        return insertBehaviour(index, resource, amount, context);
    }

    private int insertBehaviour(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (!isValid(index, resource)) return 0;

        FluidStack currentStack = stacks.get(index);
        int capacity = getCapacity(index, resource);

        int inserted, newAmount;
        if (currentStack.isEmpty()) {
            //the specified index is empty
            inserted = Math.min(capacity, amount);
            newAmount = inserted;
        } else {
            //is there an item in the specified index already?
            if (!resource.is(currentStack)) return 0;

            int currentStackAmount = currentStack.getAmount();
            inserted = Math.min(capacity - currentStackAmount, amount);
            newAmount = currentStackAmount + inserted;
        }

        if (inserted > 0) {
            journals.get(index).updateSnapshots(transaction);
            set(index, resource, newAmount);
        }

        return inserted;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size());
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        return extractBehaviour(index, resource, amount, context);
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext context) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        int handled = 0;
        for (int index = 0; index < size; index++) {
            handled += extractBehaviour(index, resource, amount - handled, context);
            if (handled == amount) break;
        }
        return handled;
    }

    private int extractBehaviour(int index, FluidResource resource, int amount, TransactionContext transaction) {
        FluidStack currentStack = stacks.get(index);

        if (!resource.is(currentStack)) return 0;

        int currentAmount = currentStack.getAmount();
        int handledAmount = Math.min(amount, currentAmount);

        if (handledAmount > 0) {
            journals.get(index).updateSnapshots(transaction);
            set(index, resource, currentAmount - handledAmount);
        }

        return handledAmount;
    }

    @Override
    public void set(int index, FluidResource resource, int amount) {
        stacks.set(index, resource.toStack(amount));
    }

    private class StackJournal extends SnapshotJournal<FluidStack> {
        private final int index;

        private StackJournal(int index) {
            this.index = index;
        }

        @Override
        protected FluidStack createSnapshot() {
            FluidStack original = stacks.get(index);
            return original.copy();
        }

        @Override
        protected void revertToSnapshot(FluidStack snapshot) {
            stacks.set(index, snapshot);
        }
    }
}
