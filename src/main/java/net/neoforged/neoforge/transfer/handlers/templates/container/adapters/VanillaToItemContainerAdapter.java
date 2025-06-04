/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.container.adapters;

import java.util.Objects;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.handlers.templates.container.IResourceContainer;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;

/**
 * Adapts a vanilla Minecraft {@link Container} to a {@link IResourceContainer<ItemResource>}.
 * This is always assumed to be of type {@link ItemResource} currently, since a {@link Container} can only handle {@link ItemStack ItemStacks}
 */
public final class VanillaToItemContainerAdapter implements IResourceContainer<ItemResource> {
    private final Container container;

    public VanillaToItemContainerAdapter(Container container) {
        this.container = container;
    }

    @Override
    public ResourceStack<ItemResource> defaultResource() {
        return ItemResource.EMPTY_STACK;
    }

    @Override
    public int size() {
        return container.getContainerSize();
    }

    @Override
    public SnapshotJournal<?> getParticipant(int index) {
        return EmptySnapshot.INSTANCE;
    }

    @Override
    public MutableResourceStack<ItemResource> get(int index) {
        var stack = container.getItem(index);
        return MutableResourceStack.of(ItemResource.of(stack), stack.getCount());
    }

    @Override
    public void set(int index, MutableResourceStack<ItemResource> stack) {
        container.setItem(index, stack.resource().toStack(stack.amount()));
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return container.canPlaceItem(index, resource.toStack());
    }

    @Override
    public int getCapacity(int index, ItemResource resource) {
        return Math.min(container.getMaxStackSize(resource.toStack()), resource.getMaxStackSize());
    }

    @Override
    public boolean isEmpty() {
        return container.isEmpty();
    }

    public Container container() {
        return container;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (VanillaToItemContainerAdapter) obj;
        return Objects.equals(this.container, that.container);
    }
}
