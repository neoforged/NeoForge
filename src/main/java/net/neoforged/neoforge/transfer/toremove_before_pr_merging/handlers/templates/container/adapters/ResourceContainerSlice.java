/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.adapters;

import java.util.Objects;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.IResourceContainer;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;

/**
 * A slice of a {@link IResourceContainer}. Changes to the slice should reflect in the parent.
 */
public record ResourceContainerSlice<R extends IResource>(
        IResourceContainer<R> parent,
        int start, int length) implements IResourceContainer<R> {
    @Override
    public int size() {
        return length;
    }

    @Override
    public SnapshotJournal<?> getIndexJournal(int index) {
        Objects.checkIndex(index, length);
        return parent.getIndexJournal(index + start);
    }

    @Override
    public MutableResourceStack<R> get(int index) {
        Objects.checkIndex(index, length);
        return parent.get(index + start);
    }

    @Override
    public void set(int index, MutableResourceStack<R> stack) {
        Objects.checkIndex(index, length);
        parent.set(index + start, stack);
    }

    @Override
    public boolean isValid(int index, R stack) {
        Objects.checkIndex(index, length);
        return parent.isValid(index + start, stack);
    }

    @Override
    public int getCapacity(int index, R resource) {
        return parent.getCapacity(index, resource);
    }

    @Override
    public IResourceContainer<R> slice(int from, int to) {
        Objects.checkFromToIndex(from, to, length);
        return new ResourceContainerSlice<>(parent, this.start + from, to - from);
    }

    @Override
    public ResourceStack<R> defaultResource() {
        return parent.defaultResource();
    }
}
