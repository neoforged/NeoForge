/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resource;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.IStackFactory;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.IResourceStack;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
import net.neoforged.neoforge.transfer.resources.ResourceStack;

/**
 * An immutable data structure used to store a list of resources and their amounts that can be serialized as a DataComponent. This is ideal when working with ItemStacks to hold the data.
 */
public final class ResourceStorageComponent<T extends IResource> {
    public final IStackFactory<T, ResourceStack<T>> stackFactory;
    private final NonNullList<ResourceStack<T>> stacks;
    private final int hashCode;

    /**
     * Intended to be used as a data component on an ItemStack. This stores an immutable list, and any changes needed, create a new list.
     */
    public ResourceStorageComponent(NonNullList<ResourceStack<T>> stacks, IStackFactory<T, ResourceStack<T>> stackFactory) {
        this.stacks = stacks;
        this.hashCode = IResourceStack.hashTypes(stacks);
        this.stackFactory = stackFactory;
    }

    public static <T extends IResource> ResourceStorageComponent<T> of(int size, T emptyResource, IStackFactory<T, ResourceStack<T>> stackFactory) {
        return new ResourceStorageComponent<>(NonNullList.withSize(size, stackFactory.create(emptyResource, 0)), stackFactory);
    }

    public static <T extends IResource> Codec<ResourceStorageComponent<T>> codec(Codec<T> resourceCodec, IStackFactory<T, ResourceStack<T>> stackFactory) {
        return NonNullList.codecOf(IResourceStack.codec(resourceCodec, stackFactory)).xmap(resourceStacks -> new ResourceStorageComponent<>(resourceStacks, stackFactory), contents -> contents.stacks);
    }

    public static <T extends IResource> StreamCodec<RegistryFriendlyByteBuf, ResourceStorageComponent<T>> streamCodec(StreamCodec<RegistryFriendlyByteBuf, ResourceStack<T>> resourceCodec, IStackFactory<T, ResourceStack<T>> stackFactory) {
        return resourceCodec.apply(ByteBufCodecs.collection(NonNullList::<ResourceStack<T>>createWithCapacity))
                .map(resourceStacks -> new ResourceStorageComponent<>(resourceStacks, stackFactory),
                        component -> component.stacks);
    }

    public ResourceStack<T> get(int index) {
        return stacks.get(index);
    }

    public ResourceStorageComponent<T> modify(int index, T resource, int amount) {
        NonNullList<ResourceStack<T>> list = NonNullList.createWithCapacity(stacks.size());
        for (IResourceStack<T> stack : stacks) {
            list.add(stack.immutable());
        }
        list.set(index, stackFactory.create(resource, amount));
        return new ResourceStorageComponent<>(list, stackFactory);
    }

    public ResourceStorageComponent<T> immutable() {
        return this;
    }

    public Mutable<T> mutable() {
        NonNullList<MutableResourceStack<T>> list = NonNullList.createWithCapacity(stacks.size());
        for (ResourceStack<T> stack : stacks) {
            list.add(stack.mutable());
        }
        return new Mutable<>(list, stackFactory);
    }

    @Override
    public boolean equals(Object otherObj) {
        return this == otherObj
                || otherObj instanceof ResourceStorageComponent<?> otherData
                        && stacks.equals(otherData.stacks);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "ResourceStorageComponent[%s]".formatted(stacks);
    }

    /**
     * A mutable data structure used to store a list of resources and their amounts.
     */
    public static final class Mutable<T extends IResource> {
        public final IStackFactory<T, ResourceStack<T>> stackFactory;
        private final NonNullList<MutableResourceStack<T>> stacks;

        /**
         * @param stacks a list of MutableResourceStacks. The stacks are expected to have their amount mutated internally never externally.
         */
        private Mutable(NonNullList<MutableResourceStack<T>> stacks, IStackFactory<T, ResourceStack<T>> stackFactory) {
            this.stacks = stacks;
            this.stackFactory = stackFactory;
        }

        public MutableResourceStack<T> get(int index) {
            return stacks.get(index);
        }

        public Mutable<T> modify(int index, T resource, int amount) {
            MutableResourceStack<T> current = get(index);
            if (current.resource().equals(resource))
                current.withAmount(amount);
            else {
                //We do .mutable() to get the advantage of not creating new empty instances with our stackFactory.
                //The downside to this is that we are creating an immutable resource stack that is immediately disposed
                // of when converting it to a mutable stack.
                stacks.set(index, stackFactory.create(resource, amount).mutable());
            }
            return this;
        }

        public Mutable<T> mutable() {
            return this;
        }

        public ResourceStorageComponent<T> immutable() {
            NonNullList<ResourceStack<T>> list = NonNullList.createWithCapacity(stacks.size());
            for (IResourceStack<T> stack : stacks) {
                list.add(stack.immutable());
            }
            return new ResourceStorageComponent<>(list, stackFactory);
        }

        @Override
        public String toString() {
            return "ResourceStorageAttachment[" + stacks + ']';
        }
    }
}
