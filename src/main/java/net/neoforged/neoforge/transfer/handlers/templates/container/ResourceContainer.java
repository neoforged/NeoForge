/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.container;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.container.adapters.ItemContainerToVanillaAdapter;
import net.neoforged.neoforge.transfer.resources.*;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A data storage for mutable resource stacks. This data can be put anywhere (with limited exceptions such as DataComponents), but it was designed with {@link net.neoforged.neoforge.attachment.AttachmentType DataAttachments} in mind.
 * You are able to build new containers, slice existing ones, as well as convert them to other types such as an {@link IResourceHandler IResourceHandler}.
 * To be more clear, the container itself is not an {@link IResourceHandler IResourceHandler}, but by calling {@link #asHandler()} it will create one, though may be wise to cache this rather than call it every time you need a handler of the container.
 * <p>
 *
 * <strong>Example Usage</strong>
 *
 * <pre>
 * {@code
 * var container = SimpleItemResourceContainer.from(someSerializedList)
 *         .onChange(this::markHolderAsDirty)
 *         .build();
 * IResourceHandler<ItemResource> handler = container.asHandler();
 * var outputContainer = container.slice(3, 4);
 * var outputHandler = outputContainer.asHandler(IHandleIOBehaviour.EXTRACT_ONLY);
 * }
 * </pre>
 *
 * <p>
 * To reiterate, this can work anywhere in a mutable context, but things like {@link net.minecraft.core.component.DataComponentType DataComponents} that require an immutable scope will not work properly.
 *
 * @param <T> resource type
 */
//Originally written by Soaryn for XyCraft adopted from Amadornes's ItemContainer.
public class ResourceContainer<T extends IResource> implements IResourceContainer<T> {
    private final NonNullList<MutableResourceStack<T>> resourceStacks;
    private final List<IndexSnapshot> indexSnapshots = new ArrayList<>();
    private final SetChangedJournal changedJournal;
    private final ResourceStack<T> defaultResource;
    private final int size;
    private final int capacity;

    /**
     * @param resourceStacks  The backing list of stacks that are stored. This is what is snapshotted
     * @param defaultResource The resource that should fill the backing list given a reset or clear
     * @param capacity        The amount all resource stacks can stack up to.
     * @param updateCallback  Called in {@link #changedJournal changedJournal's} {@link SetChangedJournal#onCommit onCommit}
     */
    public ResourceContainer(NonNullList<MutableResourceStack<T>> resourceStacks, ResourceStack<T> defaultResource, int capacity, @Nullable Runnable updateCallback) {
        Objects.requireNonNull(resourceStacks);
        Objects.requireNonNull(defaultResource);

        Objects.checkIndex(0, resourceStacks.size());
        this.size = resourceStacks.size();
        this.resourceStacks = resourceStacks;
        changedJournal = SetChangedJournal.of(updateCallback);
        this.defaultResource = defaultResource;
        this.capacity = capacity;
        updateSlots();
    }

    @Override
    public SnapshotJournal<MutableResourceStack<T>> getParticipant(int index) {
        return indexSnapshots.get(index);
    }

    public SetChangedJournal getChangeSetJournal(){
        return changedJournal;
    }

    public static final class Codecs {
        public static <TAttachment, TResource extends IResource> RecordCodecBuilder<TAttachment, NonNullList<MutableResourceStack<TResource>>> resourcesOf(String key, Codec<TResource> codec, Function<TAttachment, IResourceContainer<TResource>> containerToStackList) {
            return NonNullList.codecOf(MutableResourceStack.flatCodec(codec)).fieldOf(key).forGetter(attachment -> containerToStackList.apply(attachment).copyToList());
        }

        public static <TAttachment> RecordCodecBuilder<TAttachment, NonNullList<MutableResourceStack<ItemResource>>> itemsOf(String key, Function<TAttachment, IResourceContainer<ItemResource>> containerToStackList) {
            return resourcesOf(key, ItemResource.OPTIONAL_CODEC, containerToStackList);
        }

        public static <TAttachment> RecordCodecBuilder<TAttachment, NonNullList<MutableResourceStack<FluidResource>>> fluidsOf(String key, Function<TAttachment, IResourceContainer<FluidResource>> containerToStackList) {
            return resourcesOf(key, FluidResource.OPTIONAL_CODEC, containerToStackList);
        }
    }

    public ResourceStack<T> emptyResource() {
        return defaultResource;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isValid(int index, T resource) {
        Objects.checkIndex(index, size());
        return true;
    }

    //An item resource container will override this and use which ever is smaller, the resource stack or the capacity
    @Override
    public int getCapacity(int index, T resource) {
        return capacity;
    }

    @Override
    public MutableResourceStack<T> get(int index) {
        Objects.checkIndex(index, size());
        return resourceStacks.get(index);
    }

    @Override
    public void set(int index, MutableResourceStack<T> stack) {
        Objects.checkIndex(index, size());
        resourceStacks.set(index, stack);
    }

    @Override
    public void clearContent() {
        Collections.fill(resourceStacks, emptyResource().mutable());
        changedJournal.runCallback();
    }

    @Override
    public IResourceContainer<T> slice(int from, int to) {
        Objects.checkFromToIndex(from, to, size());
        return new Slice(from, to - from);
    }

    protected void updateSlots() {
        while (indexSnapshots.size() < resourceStacks.size()) {
            indexSnapshots.add(new IndexSnapshot(indexSnapshots.size()));
        }
    }

    /**
     * Creates a vanilla {@link Container} instance that reflects this item holder.
     *
     * @return The container.
     */
    @Contract(pure = true)
    public Container asVanillaContainer() {
        //This is used to determine the type of the resource container.
        // A round about way of doing it perhaps, but this was what was possible in Java
        //in C# we'd do typeof(T) but we can't do that here. Since we know the
        if (emptyResource().resource() instanceof ItemResource) {
            //noinspection unchecked
            return new ItemContainerToVanillaAdapter((ResourceContainer<ItemResource>) this);
        }

        return EMPTY;
    }

    private static final Container EMPTY = new Container() {
        @Override
        public void clearContent() { }

        @Override
        public int getContainerSize() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public ItemStack getItem(int p_18941_) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int p_18942_, int p_18943_) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int p_18951_) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int p_18944_, ItemStack p_18945_) { }

        @Override
        public void setChanged() { }

        @Override
        public boolean stillValid(Player p_18946_) {
            return false;
        }
    };

    private class Slice implements IResourceContainer<T> {
        private final int start, length;

        public Slice(int start, int length) {
            this.start = start;
            this.length = length;
        }

        @Override
        public int size() {
            return length;
        }

        @Override
        public SnapshotJournal<MutableResourceStack<T>> getParticipant(int index) {
            Objects.checkIndex(index, size());
            return ResourceContainer.this.getParticipant(index + start);
        }

        @Override
        public MutableResourceStack<T> get(int index) {
            Objects.checkIndex(index, size());
            return ResourceContainer.this.get(index + start);
        }

        @Override
        public void set(int index, MutableResourceStack<T> stack) {
            Objects.checkIndex(index, size());
            ResourceContainer.this.set(index + start, stack);
        }

        @Override
        public int getCapacity(int index, T resource) {
            Objects.checkIndex(index, size());
            return ResourceContainer.this.getCapacity(index + start, resource);
        }

        @Override
        public boolean isValid(int index, T stack) {
            Objects.checkIndex(index, size()); //audit called in the this.get
            return ResourceContainer.this.isValid(index + start, stack);
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < length; i++)
                ResourceContainer.this.resourceStacks.set(i + start, emptyResource().mutable());
            getChangeSetJournal().runCallback();

        }

        @Override
        public IResourceContainer<T> slice(int from, int to) {
            Objects.checkFromToIndex(from, to, length);
            return new Slice(this.start + from, to - from);
        }

        @Override
        public ResourceStack<T> emptyResource() {
            return ResourceContainer.this.emptyResource();
        }
    }

    public static class Builder<T extends IResource, TBuilder extends Builder<T, TBuilder>> {
        protected int capacity;
        @Nullable
        protected Runnable updateCallback;
        @Nullable
        protected NonNullList<MutableResourceStack<T>> stacks;
        protected ResourceStack<T> emptyStack;

        public Builder(ResourceStack<T> emptyStack) {
            this.emptyStack = emptyStack;
        }

        private TBuilder self() {
            //noinspection unchecked
            return (TBuilder) this;
        }

        public TBuilder size(int size) {
            this.stacks = NonNullList.withSize(size, emptyStack.mutable());
            return self();
        }

        public TBuilder capacity(int capacity) {
            this.capacity = capacity;
            return self();
        }

        public TBuilder onChange(Runnable updateCallback) {
            this.updateCallback = updateCallback;
            return self();
        }

        public TBuilder from(NonNullList<MutableResourceStack<T>> stacks) {
            this.stacks = stacks;
            return self();
        }

        public ResourceContainer<T> build() {
            if (stacks == null) throw new IllegalArgumentException("ResourceContainer's stacks must not be null");
            return new ResourceContainer<>(stacks, emptyStack, capacity, updateCallback);
        }

        public final ResourceContainer<T> buildRaw() {
            if (stacks == null) throw new IllegalArgumentException("ResourceContainer's stacks must not be null");
            return new ResourceContainer<>(stacks, emptyStack, capacity, updateCallback);
        }
    }


    private class IndexSnapshot extends SnapshotJournal<MutableResourceStack<T>> {
        private final int slot;

        private IndexSnapshot(int slot) {
            this.slot = slot;
        }

        @Override
        protected MutableResourceStack<T> createSnapshot() {
            return MutableResourceStack.of(resourceStacks.get(slot));
        }

        @Override
        protected void revertToSnapshot(MutableResourceStack<T> snapshot) {
            resourceStacks.set(slot, snapshot);
        }

        @Override
        public void updateSnapshots(TransactionContext transaction) {
            changedJournal.updateSnapshots(transaction);
            super.updateSnapshots(transaction);
        }
    }
}
