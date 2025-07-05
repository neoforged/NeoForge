/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

/**
 * Most general form of a resource that can be quantified and moved around.
 *
 * <p>Instances must all be immutable, comparable with {@link Object#equals(Object)}
 * and they must implement a suitable {@link Object#hashCode()}.
 * <p>
 * Note, the amount is not encoded in the resource, for that you can use something like {@link ResourceStack}.
 */
public interface IResource {
    /**
     * Returns {@code true} if this represents an empty resource.
     *
     * <p>Examples include item resource with air as an item, or fluid resource with empty fluid.
     */
    boolean isEmpty();

    //Pup's comment pre-slicing
    // I am torn on whether it makes sense or not, so would like some more input from other maintainers
    // before this change is made, but should we have a toStack (or toResourceStack) type thing to
    // help creating ResourceStack when in a generic context, without having to explicitly also keep
    // track of the empty resource when in generic code? Given theoretically all resources should know
    // their own type, and their empty variant.
    //
    // Personal Followup:
    //  Having a `withAmount` method would likely make sense as a helper. However, the toStack (ItemStack/FluidStack similar) is a little trickier,
    //  as we shouldn't assume the resource has a backing stack element or even has an associated stack.
    //  Having this would simplify a lot of the generic logic we have since we likely wouldn't need to pass in the empty,
    //  stack anymore to the resource stack, so it may be a good idea to have.
    //  Since our current implementations already have this method, this is what it'd look like the following.
    // Though to clean it up fully, we would likely want to make IResource generic bounded to a type like
    // `IResource<T extends IResource<T>>` this way we can avoid the unchecked problem when working purely with generics.
    ResourceStack<? extends IResource> withAmount(int amount);
}
