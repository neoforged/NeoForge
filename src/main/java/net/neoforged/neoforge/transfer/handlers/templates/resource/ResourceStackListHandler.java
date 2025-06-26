/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resource;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.transfer.IStackFactory;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import org.jetbrains.annotations.Nullable;

public abstract class ResourceStackListHandler<R extends IResource> extends StackListHandler<ResourceStack<R>, R> {
    public ResourceStackListHandler(int size, ResourceStack<R> emptyStack, int capacity, IStackFactory<R, ResourceStack<R>> stackFactory, @Nullable Runnable onChangedCallback) {
        super(size, emptyStack, capacity, stackFactory, onChangedCallback);
    }

    public ResourceStackListHandler(NonNullList<ResourceStack<R>> stacks, int capacity, ResourceStack<R> emptyStack, IStackFactory<R, ResourceStack<R>> stackFactory, @Nullable Runnable onChangedCallback) {
        super(stacks, emptyStack, capacity, stackFactory, onChangedCallback);
    }

    @Override
    public R getResourceFrom(ResourceStack<R> stack) {
        return stack.resource();
    }

    @Override
    public int getAmountFrom(ResourceStack<R> stack) {
        return stack.amount();
    }

    @Override
    public boolean matches(ResourceStack<R> stack, R resource) {
        return stack.resource().equals(resource);
    }

    @Override
    public ResourceStack<R> snapshotOf(ResourceStack<R> stack) {
        //Since it is immutable, we can just take the stack as is instead of copying it.
        return stack;
    }

    public static class Item extends ResourceStackListHandler<ItemResource> {
        public Item(int size, int capacity, @Nullable Runnable onChangedCallback) {
            super(size, ItemResource.EMPTY_STACK, capacity, ItemResource::withAmount, onChangedCallback);
        }

        public Item(NonNullList<ResourceStack<ItemResource>> mutableResourceStacks, int capacity, @Nullable Runnable onChangedCallback) {
            super(mutableResourceStacks, capacity, ItemResource.EMPTY_STACK, ItemResource::withAmount, onChangedCallback);
        }

        @Override
        public Codec<ResourceStack<ItemResource>> stackCodec() {
            return ItemResource.RESOURCE_STACK_CODEC;
        }
    }

    public static class Fluid extends ResourceStackListHandler<FluidResource> {
        public Fluid(int size, int capacity, @Nullable Runnable onChangedCallback) {
            super(size, FluidResource.EMPTY_STACK, capacity, FluidResource::withAmount, onChangedCallback);
        }

        public Fluid(NonNullList<ResourceStack<FluidResource>> stacks, int capacity, @Nullable Runnable onChangedCallback) {
            super(stacks, capacity, FluidResource.EMPTY_STACK, FluidResource::withAmount, onChangedCallback);
        }

        @Override
        public Codec<ResourceStack<FluidResource>> stackCodec() {
            return FluidResource.OPTIONAL_RESOURCE_STACK_CODEC;
        }
    }
}
