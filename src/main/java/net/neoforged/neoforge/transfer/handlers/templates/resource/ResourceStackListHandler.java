/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resource;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.IResourceStack;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
import org.jetbrains.annotations.Nullable;

public abstract class ResourceStackListHandler<R extends IResource> extends StackListHandler<MutableResourceStack<R>, R> {
    public ResourceStackListHandler(int size, MutableResourceStack<R> emptyStack, int capacity, @Nullable Runnable onChangedCallback) {
        super(size, emptyStack, capacity, onChangedCallback);
    }

    public ResourceStackListHandler(NonNullList<MutableResourceStack<R>> stacks, MutableResourceStack<R> emptyStack, int capacity, @Nullable Runnable onChangedCallback) {
        super(stacks, emptyStack, capacity, onChangedCallback);
    }

    @Override
    public R getResourceFrom(MutableResourceStack<R> stack) {
        return stack.resource();
    }

    @Override
    public int getAmountFrom(MutableResourceStack<R> stack) {
        return stack.amount();
    }

    @Override
    public boolean isStackEmpty(MutableResourceStack<R> stack) {
        return stack.isEmpty();
    }

    @Override
    public boolean matches(R resource, MutableResourceStack<R> stack) {
        return stack.resource().equals(resource);
    }

    @Override
    public MutableResourceStack<R> toStack(R resource, int amount) {
        return MutableResourceStack.of(resource, amount);
    }

    @Override
    public MutableResourceStack<R> copyOf(MutableResourceStack<R> stack) {
        return stack.copy();
    }

    public static class Item extends ResourceStackListHandler<ItemResource> {
        public Item(int size, int capacity, @Nullable Runnable onChangedCallback) {
            super(size, ItemResource.EMPTY_MUTABLE_STACK, capacity, onChangedCallback);
        }

        public Item(NonNullList<MutableResourceStack<ItemResource>> mutableResourceStacks, int capacity, @Nullable Runnable onChangedCallback) {
            super(mutableResourceStacks, ItemResource.EMPTY_MUTABLE_STACK, capacity, onChangedCallback);
        }

        @Override
        public Codec<MutableResourceStack<ItemResource>> stackCodec() {
            return IResourceStack.flatCodec(ItemResource.OPTIONAL_CODEC, ItemResource::withMutableAmount);
        }
    }

    public static class Fluid extends ResourceStackListHandler<FluidResource> {
        public Fluid(int size, int capacity, @Nullable Runnable onChangedCallback) {
            super(size, FluidResource.EMPTY_MUTABLE_STACK, capacity, onChangedCallback);
        }

        public Fluid(NonNullList<MutableResourceStack<FluidResource>> stacks, int capacity, @Nullable Runnable onChangedCallback) {
            super(stacks, FluidResource.EMPTY_MUTABLE_STACK, capacity, onChangedCallback);
        }

        @Override
        public Codec<MutableResourceStack<FluidResource>> stackCodec() {
            return IResourceStack.flatCodec(FluidResource.OPTIONAL_CODEC, FluidResource::withMutableAmount);
        }
    }
}
