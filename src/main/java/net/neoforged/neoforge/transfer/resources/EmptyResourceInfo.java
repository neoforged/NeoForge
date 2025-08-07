/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import java.util.Objects;

/**
 * Information regarding the empty instances for a given resource type.
 *
 * @param <T> The resource type
 */
//TODO Note for reviewers, this name (while works) might need alternatives. It was originally called ResourceType when @covers
// proposed it as a possible avenue to explore, but I was concerned it would be seen as a FluidType like system which it is not.
// The purpose of this class is to set up the resource's empty instances of both its type and the resource stack.
// The benefit of doing it this way, is that we can make ResourceStack.of(resource, amount) always be the route to take
// to create a new resource stack. It will return either the new resource stack, or in the case of being empty, the empty instance below.
//Possible names off the top of my head were, but I'm not particularly sold on any of them:
//  - ResourceInfo
//  - EmptyResourceInfo
//  - EmptyResourceInstance
public final class EmptyResourceInfo<T extends IResource> {
    private final T emptyInstance;
    private final ResourceStack<T> emptyStackInstance;

    /**
     * A new resource info that initializes the empty {@link ResourceStack}instance.
     *
     * @param emptyInstance The empty instance of the resource type. In the case that the resource type is never empty, a default value should be provided.
     * @throws NullPointerException if {@code emptyInstance} is {@code null}
     *
     */
    public EmptyResourceInfo(T emptyInstance) {
        Objects.requireNonNull(emptyInstance);
        this.emptyInstance = emptyInstance;
        this.emptyStackInstance = new ResourceStack<>(emptyInstance, 0);
    }

    /**
     * {@return the empty resource instance of the resource type}
     * If the resource type is classified as never being empty, then a defaulting instance should be specified.
     *
     * @see ItemResource#EMPTY
     * @see FluidResource#EMPTY
     */
    public T emptyInstance() {
        return emptyInstance;
    };

    /**
     * {@return the empty resource stack instance of the resource type}
     * If the resource type is classified as never being empty, then a default resource
     * stack instance of the resource should be specified.
     *
     * @see ItemResource#EMPTY_STACK
     * @see FluidResource#EMPTY_STACK
     */
    public ResourceStack<T> emptyResourceStack() {
        return emptyStackInstance;
    };
}
