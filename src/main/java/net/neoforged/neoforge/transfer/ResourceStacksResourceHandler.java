/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;

/**
 * Base implementation of a {@link ResourceHandler} backed by a list of {@link ResourceStack}s.
 *
 * <p>The following methods will typically be overridden:
 * <ul>
 * <li>(optional) {@link #isValid} to limit which resources are allowed in this handler; by default any resource is allowed.</li>
 * <li>(required) {@link #getCapacity} to specify the capacity of this handler.</li>
 * <li>(recommended) {@link #onContentsChanged} to react to changes in this handler, for example to trigger {@code setChanged()}.</li>
 * </ul>
 *
 * @see StacksResourceHandler
 */
public abstract class ResourceStacksResourceHandler<R extends Resource> extends StacksResourceHandler<ResourceStack<R>, R> {
    // TODO: do we want to be passing a resource codec instead?
    public ResourceStacksResourceHandler(int size, R emptyResource, Codec<ResourceStack<R>> stackCodec) {
        super(size, new ResourceStack<>(emptyResource, 0), stackCodec);
    }

    public ResourceStacksResourceHandler(NonNullList<ResourceStack<R>> stacks, R emptyResource, Codec<ResourceStack<R>> stackCodec) {
        super(stacks, new ResourceStack<>(emptyResource, 0), stackCodec);
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
    protected ResourceStack<R> getStackFrom(R resource, int amount) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) {
            return emptyStack;
        }
        return new ResourceStack<>(resource, amount);
    }

    @Override
    protected ResourceStack<R> copyOf(ResourceStack<R> stack) {
        return stack;
    }

    @Override
    public boolean matches(ResourceStack<R> stack, R resource) {
        return stack.resource().equals(resource);
    }
}
