/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.energy;

import javax.annotation.Nonnegative;
import net.neoforged.neoforge.transfer.handlers.resources.ITransactionHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

/**
 * Formerly `IEnergyStorage`
 * <p>
 * A capability interface providing the methods such as insert/extract a buffered energy amount for a handler.<br>
 * To use Neo's energy capability see the {@link net.neoforged.neoforge.capabilities.Capabilities.EnergyHandler#BLOCK EnergyCapability} in {@link net.neoforged.neoforge.capabilities.Capabilities Capabilities}.
 * <br>
 * To make your own energy system using this interface, you can register a new capability using something like the following.
 *
 * <pre>
 * {@code public static final BlockCapability<IEnergyHandler, @Nullable Direction> BLOCK = BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath([MOD_ID], [CUSTOM_ENERGY_NAME]), IEnergyHandler.class);}
 * </pre>
 *
 * <p>
 * This would effectively create a new capability that other mods could utilize so long as they create a new capability with the same id without needing any extra API provided by you.
 */
public interface IEnergyHandler extends ITransactionHandler {
    /**
     * <b>PRIMER: New</b> - Required for the indexed methods below to be inquired correctly.
     *
     * @return The number of indices this handler manages. You shouldn't return 0 where avoidable, but this is allowed in cases there are no buffers such as the {@link net.neoforged.neoforge.transfer.handlers.templates.energy.EmptyEnergyHandler Empty handler}
     */
    @Nonnegative
    int size();

    /**
     * <b>PRIMER: Formerly</b> `getEnergyStored`. Now needs an index.
     * <p>
     * To know the total amount of energy stored in a {@link IEnergyHandler} consider using {@link net.neoforged.neoforge.transfer.EnergyHandlerUtil#getAmount(IEnergyHandler) EnergyHandlerUtil.getAmount(IEnergyHandler)}
     *
     * @param index The index to get the amount from.
     * @return The amount of energy stored at the given index. This should be non-negative.
     */
    @Nonnegative
    int getAmount(@Nonnegative int index);

    /**
     * This is an optional method that provides the ability to query the contents up to a long should the internals allow for it.
     * This is only needed to be overridden should you store more than an int in a given index.
     *
     * @param index The index to get the amount from.
     * @return The amount of energy stored at the given index. This should be non-negative.
     */
    @Nonnegative
    default long getAmountAsLong(@Nonnegative int index) {
        return getAmount(index);
    }

    /**
     * <b>PRIMER: Formerly</b> `getMaxEnergyStored`. Now has an index similar to IResourceHandler.
     * <p>
     * Gets the capacity that index can hold.
     * To know the total capacity of energy in a {@link IEnergyHandler} consider using {@link net.neoforged.neoforge.transfer.EnergyHandlerUtil#getCapacity(IEnergyHandler) EnergyHandlerUtil.getCapacity(IEnergyHandler)}
     *
     * @param index The index to get the limit from.
     * @return The capacity at the given index. This should be non-negative.
     */
    @Nonnegative
    int getCapacity(@Nonnegative int index);

    /**
     * This is an optional method that provides the ability to query the capacity up to a long should the internals allow for it.
     * This is only needed to be overridden should you be able to store more than an int in a given index.
     *
     * @param index The index to get the amount from.
     * @return The amount of energy stored at the given index. This should be non-negative.
     */
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getCapacityAsLong(@Nonnegative int index) {
        return getCapacity(index);
    }

    /**
     * <b>PRIMER: Formerly</b> `canReceive` This however deviates a bit from its use as it is now at ANY point can it be inserted to rather than in that point in time.
     * The update path for scenarios you'd call canReceive to determine if you'd call receiveEnergy are is now just to call allowsInsertion once on capability invalidation
     * then assuming it was true insert(simulate) anytime you are wanting to see if you can insert energy. This will be true for the indexed and the allowsExtraction methods.
     * Changing this requires you to invalidate your capability, so change sparingly.
     * <p>
     * <strong>Note:</strong> It is advised to override this and return true or false if you already know the handler will have a possible true result
     *
     * @return True if the handler can be inserted into at this time, false otherwise. If using indices, this should return true if any index allows insertion
     */
    default boolean supportsInsertion() {
        var indices = size();
        for (var index = 0; index < indices; index++) {
            if (supportsInsertion(index)) return true;
        }
        return false;
    }

    /**
     * <b>PRIMER: New</b>
     * <p>
     * An estimation of or hint if {@link IEnergyHandler#insert} would ever succeed. This should not be used to determine if something is full, nor should it return as such.
     * A typical use case is identifying which implementations of {@link IEnergyHandler} in a group would be able to be insertable. Changing this requires you to invalidate your capability, so change sparingly.
     * <p>
     * <b>IMPORTANT:</b> This doesn't add any control, this is merely a guide for things like pipes to know ahead of time if it can be ever inserted into when the capability invalidates for example.
     * You shouldn't call this in your own {@link IEnergyHandler#insert} method, but you still need to handle the result if insert wouldn't fill there.
     *
     * @param index The index to check
     * @return {@code false} if at the given index, the handler can <strong>never</strong> be inserted into; {@code true} otherwise.
     */
    boolean supportsInsertion(@Nonnegative int index);

    /**
     * <b>PRIMER: Formerly</b> `canExtract`
     * <p>
     * An estimation of or hint if {@link IEnergyHandler#extract} would result in any energy. This should not be used to determine if something is empty, nor should it return as such.
     * A typical use case is identifying which implementations of {@link IEnergyHandler} in a group would be able to be extractable. Changing this requires you to invalidate your capability, so change sparingly.
     * <p>
     * <strong>Note:</strong> It is advised to override this and return {@code true} if you already know the handler will have a possible true result or {@code false} if none of them will ever be true;
     * rather than having it require a full iteration to look up the results.
     *
     * @return {@code true} if the handler can be extracted from in its configuration, false otherwise. If using indices, this should return true if any index allows insertion.
     */
    default boolean supportsExtraction() {
        var indices = size();
        for (var index = 0; index < indices; index++) {
            if (supportsExtraction(index)) return true;
        }
        return false;
    }

    /**
     * <b>PRIMER: New</b>
     * <p>
     * An estimation of or hint if {@link IEnergyHandler#extract} would result in any energy. This should <b>not</b> be used to determine if something is empty, nor should it return as such.
     * A typical use case is identifying which implementations of {@link IEnergyHandler} in a group would be able to be extractable regardless of amount stored. Changing this requires you to invalidate your capability, so change sparingly.
     * <p>
     * <b>IMPORTANT:</b> This doesn't add any control, this is merely a guide for things like pipes to know ahead of time if it can be ever extracted from when the capability invalidates for example.
     * You shouldn't call this in your own {@link IEnergyHandler#extract} method.
     *
     * @param index The index to check
     * @return {@code false} if at the given index, the handler can <strong>never</strong> be extracted from; {@code true} otherwise.
     */
    boolean supportsExtraction(@Nonnegative int index);

    /**
     * <b>PRIMER: New</b>
     * <p>
     * Inserts a given amount of energy into the handler at the target index. If the intent is to just arbitrarily send power to the handler, consider using {@link IEnergyHandler#insert(int, TransactionContext)} instead.
     *
     * @param index       The index to insert into.
     * @param amount      The value to insert.
     * @param transaction the transaction chain that the insertion is part of. The developer is expected to handle snapshotting as necessary to handle rollbacks when the transaction is not committed. * @return The amount that was (or would have been, if simulated) inserted. This should be non-negative.
     * @return The amount that was (or would have been, if simulated) inserted. This should be non-negative.
     */
    @Nonnegative
    int insert(@Nonnegative int index, @Nonnegative int amount, TransactionContext transaction);

    /**
     * <b>PRIMER: Formerly</b> `receiveEnergy(int toReceive, bool simulate)`
     * <p>
     * Inserts a given amount into the handler. Distribution is up to the handler.
     * <p>
     * When implementing, it is advised to not make this call {@link IEnergyHandler#insert(int, int, TransactionContext) insert(index, ...)} for each index directly,
     * but rather reuse the logic already checked. See {@link net.neoforged.neoforge.transfer.handlers.templates.energy.EnergyBufferAttachment#insert(int, TransactionContext) EnergyBuffer.insertCommon} for a reference of an implementation.
     *
     * @param amount      The amount to insert.
     * @param transaction the transaction chain that the insertion is part of. The developer is expected to handle snapshotting as necessary to handle rollbacks when the transaction is not committed. * @return The amount that was (or would have been, if simulated) inserted. This should be non-negative.
     */
    @Nonnegative
    int insert(@Nonnegative int amount, TransactionContext transaction);

    /**
     * <b>PRIMER: New</b>
     * <p>
     * Extracts a given amount of energy from the handler at the given index. If the intent is to arbitrarily extract power from the handler, consider using {@link IEnergyHandler#extract(int, TransactionContext)} instead.
     *
     * @param index       The index to extract from.
     * @param amount      The amount to extract.
     * @param transaction the transaction chain that the extraction is part of. The developer is expected to handle snapshotting as necessary to handle rollbacks when the transaction is not committed.
     * @return The amount that was (or would have been, if simulated) extracted. This should be non-negative.
     */
    @Nonnegative
    int extract(@Nonnegative int index, @Nonnegative int amount, TransactionContext transaction);

    /**
     * <b>PRIMER: Formerly</b> `extractEnergy(int toReceive, bool simulate)`
     * <p>
     * Extracts a given amount from the handler. Distribution is up to the handler.
     * <p>
     * When implementing this method, it is advised to not make this call {@link IEnergyHandler#extract(int, int, TransactionContext) extract(index, ...)} for each index directly,
     * but rather reuse the logic already checked. See {@link net.neoforged.neoforge.transfer.handlers.templates.energy.EnergyBufferAttachment#extract(int, TransactionContext) EnergyBuffer.extractCommon} for a reference of an implementation.
     *
     * @param amount      The amount of energy to extract.
     * @param transaction the transaction chain that the extraction is part of. The developer is expected to handle snapshotting as necessary to handle rollbacks when the transaction is not committed.
     * @return The amount that was (or would have been, if simulated) extracted. This should be non-negative.
     */
    @Nonnegative
    int extract(@Nonnegative int amount, TransactionContext transaction);
}
