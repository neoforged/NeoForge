/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers;

import org.intellij.lang.annotations.MagicConstant;

/**
 * A list of handler characteristics that help describe its intent to the consumer.
 * For instance, if you have a handler that is expected to only allow insertion and is statically sized,
 * you could return something like the following:
 * 
 * <pre>{@code
 * INSERTABLE | STATICALLY_SIZED
 * }</pre>
 *
 * While this would not prevent a consumer from calling extract, your characteristics would indicate that it isn't
 * something that is provided and would be expected to return 0.
 */
public final class TransferCharacteristics {
    private TransferCharacteristics() {}

    /**
     * A characteristic value signifying no information can be provided.
     */
    public static final int UNKNOWN = 0;

    /**
     * Characteristic value signifying that the handler can be
     * inserted into; that is, resources can be inserted to the handler during a transaction.
     * A handler that does not report {@code #INSERTABLE} or {@link #BIDIRECTIONAL} is expected to not
     * be insertable into. This however does not prevent a caller from calling {@code insert} on the handler.
     */
    public static final int INSERTABLE = 0x00000001;
    /**
     * Characteristic value signifying that the handler can be
     * extract from; that is, resources can be extracted from the handler during a transaction.
     * A handler that does not report {@code #EXTRACTABLE} or {@link #BIDIRECTIONAL} is expected to not
     * be extractable from. This however does not prevent a caller from calling {@code extract} on the handler.
     */
    public static final int EXTRACTABLE = 0x00000002;
    /**
     * Characteristic value signifying that anything sent to the handler is to be destroyed.
     * It is still recommended to call the respective insert of the handler this is describing,
     * but can be used to lower the priority of voiding handlers in a collection as just an example.
     */
    public static final int VOIDING = 0x00000004;

    /**
     * Characteristic value signifying that the handler has an infinite supply.
     * It is still recommended to call the respective extract of the handler this is describing.
     */
    public static final int INFINITE = 0x00000008;

    /**
     * Characteristic value signifying that the handler can be
     * inserted into and extract from; that is, resources can be extracted from the handler during a transaction.
     * This value is the composite of {@link #INSERTABLE} and {@link #EXTRACTABLE}.
     * <p>
     * This is just a helper value and is a pre-composite of {@code INSERTABLE} and {@code EXTRACTABLE}
     */
    public static final int BIDIRECTIONAL = INSERTABLE | EXTRACTABLE;

    /**
     * Characteristic value signifying that the handler should not be resized. All index characteristics are able be known
     * at compile time, thus are stable to use while the capability cache is valid.
     */
    public static final int STATICALLY_SIZED = 0x00000010;

    /**
     * Characteristic value signifying that the handler is expected to change size during operations. Using this
     * typically will make using the indexed based characteristics return method, need to return the handler as a whole
     * unless it is certain a specific index will always be the same value.
     */
    public static final int DYNAMICALLY_SIZED = 0x00000020;

    /**
     * A characteristic value signifying that the handler is statically sized, insertable, and extractable. This is the most
     * common characteristic value between handlers.
     */
    public static final int DEFAULT = STATICALLY_SIZED | INSERTABLE | EXTRACTABLE;
    /**
     * A characteristic value signifying that the handler will reject calls made to it. This can be used to skip the handler entirely
     * when iterating over a group.
     */
    public static final int NO_OP = 0x10000000;
    /**
     * A characteristic value signifying that the handler's contents will not change on a given operation.
     * These don't imply they should be skipped, but allows skipping opening a sub-transaction when operating for example.
     * To be clear, a transaction context will still be needed, but a specific sub-transaction won't be necessary, nor would committing.
     */
    public static final int IMMUTABLE = 0x20000000;

    /**
     * Does the composite value have the specified characteristic. This differs slightly from the handler specific
     * {@code hasCharacteristics} as it doesn't make any assumptions about {@code UNKNOWN} values. It specifically answers the question
     * does this value have this characteristic.
     * 
     * @param value           Some value to check.
     * @param characteristics The characteristic(s) that is being inquired.
     * @return {@code true} if the characteristic is composited in the value; {@code false} otherwise.
     */
    public static boolean hasCharacteristics(@MagicConstant(flagsFromClass = TransferCharacteristics.class) int value, @MagicConstant(flagsFromClass = TransferCharacteristics.class) int characteristics) {
        return (value & characteristics) == characteristics;
    }
}
