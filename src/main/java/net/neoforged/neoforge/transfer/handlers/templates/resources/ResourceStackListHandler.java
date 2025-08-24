/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resources;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;

// TODO: javadocs
public abstract class ResourceStackListHandler<R extends IResource> extends StackListHandler<ResourceStack<R>, R> {
    // TODO: do we want to be passing a resource codec instead?
    public ResourceStackListHandler(int size, R emptyResource, Codec<ResourceStack<R>> stackCodec) {
        super(size, new ResourceStack<>(emptyResource, 0), stackCodec);
    }

    public ResourceStackListHandler(NonNullList<ResourceStack<R>> stacks, R emptyResource, Codec<ResourceStack<R>> stackCodec) {
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
