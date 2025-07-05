/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.resources;

import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.TransferCharacteristics;
import net.neoforged.neoforge.transfer.handlers.templates.resources.StackListHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.ApiStatus;

/**
 * A generic handler for handling a {@link IResource resource} of type {@link T} whether it be inserting, extracting, querying some size value, etc.
 * Trying to interact with a handler with an index larger than the size is expected to often times throw an {@link IndexOutOfBoundsException} or similar error. Ensure you are within the size constraints of the handler before calling a method.
 *
 * @param <T> The type of resource this handler manages.
 */
public interface IResourceHandler<T extends IResource> {
    /**
     * An index is synonymous with "slot", "tank", "buffer", etc.
     *
     * @return The number of indices this handler manages. <strong>Must be non-negative</strong>
     */
    int size();

    /**
     * @param index The index to get the resource from. <strong>Must be non-negative</strong>
     * @return The resource at the given index.
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     */
    T getResource(int index);

    /**
     * @param index The index to get the amount from. <strong>Must be non-negative</strong>
     * @return The amount of the resource at the given index. <strong>Must be non-negative</strong> and should never surpass capacity
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #getAmountAsLong(int)
     */
    int getAmount(int index);

    /**
     * @param index The index to get the amount from. <strong>Must be non-negative</strong>
     * @return The amount as a long of the resource at the given index. <strong>Must be non-negative</strong> and must never surpass capacity
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #getAmount(int)
     */
    default long getAmountAsLong(int index) {
        return getAmount(index);
    }

    /**
     * Gets the maximum capacity that the given index can handle of the given resource.
     * If an empty resource (an {@link IResource} that returns {@code true} on {@link IResource#isEmpty()}) is provided,
     * then the theoretical maximum should be returned, regardless of the return of {@link #getResource}.
     * <p>
     * While passing in resources that would return {@code false} on {@link #isValid(int, IResource)}, it should be expected to always return 0.
     * <p>
     * If the resource returned from {@link #getResource(int)} with the same index does not match and is not empty, it is expected the capacity would return 0.
     *
     * @param index    The index to get the limit from. <strong>Must be non-negative</strong>
     * @param resource The resource to get the limit for. If empty, this should return the theoretical limit of that index
     * @return The limit of the resource at the given index. <strong>Must be non-negative</strong>
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #getCapacityAsLong(int, IResource)
     */
    int getCapacity(int index, T resource);

    /**
     * Gets the maximum capacity that the given index can handle as a long of the given resource.
     * If an empty resource (an {@link IResource} that returns {@code true} on {@link IResource#isEmpty()}) is provided,
     * then the theoretical maximum should be returned, regardless of the return of {@link #getResource}.
     * <p>
     * While passing in resources that would return {@code false} on {@link #isValid(int, IResource)}, it should be expected to always return 0.
     * <p>
     * If the resource returned from {@link #getResource(int)} with the same index does not match, it is expected the capacity would return 0.
     *
     * @param index    The index to get the limit from. <strong>Must be non-negative</strong>
     * @param resource The resource to get the limit for. If empty, this should return the theoretical limit of that index
     * @return The limit of the resource at the given index. <strong>Must be non-negative</strong>
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #getCapacity(int, IResource)
     */
    default long getCapacityAsLong(int index, T resource) {
        return getCapacity(index, resource);
    }

    /**
     * Checks if the given resource is allowed to be inserted into the handler at the given index.
     * This is typically called in the {@link #insert(int, IResource, int, TransactionContext Context)}
     * implementations or general resource querying. However, this is separate from if the resource could
     * currently fit in the handler. This is expected to be true, even if the handler would be full.
     * Empty resources should always return {@code true}.
     *
     * @param index    The index to check. <strong>Must be non-negative</strong>
     * @param resource The resource to check.
     * @return True if the resource can be inserted, false otherwise.
     * @throws IndexOutOfBoundsException when passing an invalid index.
     *                                   Negative indices are always invalid.
     */
    boolean isValid(int index, T resource);

    /**
     * A description of how this handler is intended to be used. For instance, if resources are intended
     * to be insertable, then this would be expected to return a composite value that contains {@link TransferCharacteristics#INSERTABLE}.
     * It should be noted, that this isn't intended to be used as the control logic for your handler, but rather a communication to
     * outside consumers of this resource handler to make some pre-calculated decisions on. Examples of when calling this
     * is to be used, pipes creating pre-emptive look up tables for what should be interactable, prioritization on handlers
     * that don't have {@code VOIDING} as a characteristic, etc.
     * <p>
     * If this were to return {@link TransferCharacteristics#UNKNOWN}, then no assumptions can be made about the
     * handler and should be used as you would without this information. Meaning that if you were planning on calling {@link #insert}
     * and the return is {@code UNKNOWN} then you would carry on as though it was insertable.
     * <p>
     * <strong>For blocks, this value is expected to be the same as long as the capability cache is valid.</strong>
     * 
     * <pre>{@code
     * TransferCharacteristics.STATICALLY_SIZED | TransferCharacteristics.INSERT | TransferCharacteristics.EXTRACT
     * }</pre>
     * 
     * @return Composite value of characteristics. These can be composed with a bitwise OR, (the '|').
     *
     * @see TransferCharacteristics
     * @see #characteristics(int)
     * @see #hasCharacteristics(int)
     */
    @MagicConstant(flagsFromClass = TransferCharacteristics.class)
    int characteristics();

    /**
     * A description of how this handler for this index is intended to be used. For instance, if resources are intended
     * to be insertable, then this would be expected to return a composite value that contains {@link TransferCharacteristics#INSERTABLE}.
     * It should be noted, that this isn't intended to be used as the control logic for your handler, but rather a communication to
     * outside consumers of this resource handler to make some pre-calculated decisions on. By default, all indices return what the
     * handler would return with the {@link #characteristics() index-less variant}.
     * <p>
     * If this were to return {@link TransferCharacteristics#UNKNOWN}, then no assumptions can be made about the
     * handler and should be used as you would without this information.
     * <p>
     * <strong>For blocks, this value is expected to be the same as long as the capability cache is valid.</strong>
     * 
     * <pre>{@code
     * TransferCharacteristics.STATICALLY_SIZED | TransferCharacteristics.INSERT | TransferCharacteristics.EXTRACT
     * }</pre>
     * 
     * @return Composite value of characteristics. These can be composed with a bitwise OR (the '|').
     * @param index The index to check. <strong>Must be non-negative</strong>
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #characteristics()
     * @see #hasCharacteristics(int,int)
     */
    @MagicConstant(flagsFromClass = TransferCharacteristics.class)
    default int characteristics(int index) {
        return characteristics();
    }

    /**
     * Inserts a given amount of the resource into the handler at the given index.
     *
     * @param index       The index to insert the resource into. <strong>Must be non-negative</strong>
     * @param resource    The resource to insert.
     * @param amount      The amount of the resource to insert. <strong>Must be non-negative</strong>
     * @param transaction The {@link TransactionContext} transaction to be inserting with.
     *                    It is expected that the handler properly supports rollbacks/reversions with a {@link SnapshotJournal}
     * @return The amount of the resource that was inserted. <strong>Must be non-negative.</strong>
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @throws IllegalArgumentException  if the amount is negative.
     * @see #insert(IResource, int, TransactionContext) Inserting into any index in the handler
     */
    int insert(int index, T resource, int amount, TransactionContext transaction);

    /**
     * Inserts a given amount of the resource into the handler. Distribution of the resource is up to the handler.
     * <p>
     * Implementation advice, there are some performance gains that can be achieved by handling the indexable and non-indexed versions of this call more carefully.
     * See {@link StackListHandler#insert(IResource, int, TransactionContext) StackListHandler.insertBehaviour} for an example.
     *
     * @param resource    The resource to insert. <strong>Must be non-negative</strong>
     * @param amount      The amount of the resource to insert. <strong>Must be non-negative</strong>
     * @param transaction The {@link TransactionContext } transaction to be inserting with.
     *                    It is expected that the handler properly supports rollbacks/reversions with a {@link SnapshotJournal}
     * @return The amount (Must be non-negative) of the resource that was inserted.
     * @throws IllegalArgumentException if the amount is negative.
     * @see #insert(int, IResource, int, TransactionContext) Inserting by index
     */
    default int insert(T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        int handled = 0;
        int size = size();
        for (int index = 0; index < size; index++) {
            handled += insert(index, resource, amount - handled, transaction);
            if (handled == amount) break;
        }
        return handled;
    }

    /**
     * Extracts a given amount of the resource from the handler at the given index.
     *
     * @param index       The index to extract the resource from. <strong>Must be non-negative</strong>
     * @param resource    The resource to extract.
     * @param amount      The amount of the resource to extract. <strong>Must be non-negative</strong>
     * @param transaction The {@link TransactionContext} transaction to be extracting with.
     *                    It is expected that the handler properly supports rollbacks/reversions with a {@link SnapshotJournal}
     * @return The amount (Must be non-negative) of the resource that was extracted.
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @throws IllegalArgumentException  if the amount is negative.
     * @see #extract(IResource, int, TransactionContext) Extracting from any index in the handler
     */
    int extract(int index, T resource, int amount, TransactionContext transaction);

    /**
     * Extracts a given amount of the resource from the handler. Distribution of the resource is up to the handler.
     * <p>
     * Implementation advice, there are some performance gains that can be achieved by handling the indexable and non-indexed versions of this call more carefully.
     * See {@link StackListHandler#extract(IResource, int, TransactionContext) StackListHandler.extractBehaviour} for an example.
     *
     * @param resource    The resource to extract.
     * @param amount      The amount of the resource to extract. <strong>Must be non-negative</strong>
     * @param transaction The {@link TransactionContext } transaction to be extracting with.
     *                    It is expected that the handler properly supports rollbacks/reversions with a {@link SnapshotJournal}
     * @return The amount (Must be non-negative) of the resource that was extracted.
     * @throws IllegalArgumentException if the amount is negative.
     * @see #extract(int, IResource, int, TransactionContext) Extracting by index
     */
    default int extract(T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        int handled = 0;
        int size = size();
        for (int index = 0; index < size; index++) {
            handled += extract(index, resource, amount - handled, transaction);
            if (handled == amount) break;
        }
        return handled;
    }

    /**
     * Transfer characteristics can be used to describe how this handler is intended to be used based on the returns
     * of {@link #characteristics()}
     * <p>
     * <strong>Don't override this method.</strong>
     *
     * @param characteristics The characteristics to test against.
     * @return {@code true} if the current set of characteristics contains the inquiry or is fully {@code UNKNOWN}; {@code false} otherwise.
     * @see #characteristics()
     * @see TransferCharacteristics
     */
    @ApiStatus.NonExtendable
    default boolean hasCharacteristics(@MagicConstant(flagsFromClass = TransferCharacteristics.class) int characteristics) {
        if (characteristics == TransferCharacteristics.UNKNOWN) return true;
        return (characteristics() & characteristics) == characteristics;
    }

    /**
     * Transfer characteristics can be used to describe how this handler is intended to be used based on the returns
     * of {@link #characteristics()}
     * <p>
     * <strong>Don't override this method.</strong>
     * 
     * @param index           The index to check. <strong>Must be non-negative</strong>
     * @param characteristics The characteristics to test against.
     * @return {@code true} if the current set of characteristics at the index contains the inquiry or is fully {@code UNKNOWN}; {@code false} otherwise.
     * @see #characteristics(int)
     * @see TransferCharacteristics
     */
    @ApiStatus.NonExtendable
    default boolean hasCharacteristics(int index, @MagicConstant(flagsFromClass = TransferCharacteristics.class) int characteristics) {
        if (characteristics == TransferCharacteristics.UNKNOWN) return true;
        return (characteristics(index) & characteristics) == characteristics;
    }

    /**
     * <p>
     * Example:
     *
     * <pre>{@code
     * public static final BlockCapability<IResourceHandler<FluidResource>, @Nullable Direction> BLOCK = BlockCapability.createSided(create("fluid_handler"), IResourceHandler.asClass());
     * }</pre>
     *
     * @return a class type ready to be used by something like the capability token registry.
     */
    static <T extends IResource> Class<IResourceHandler<T>> asClass() {
        //noinspection unchecked
        return (Class<IResourceHandler<T>>) (Object) IResourceHandler.class;
    }
}
