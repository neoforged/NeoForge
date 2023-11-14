/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.simple;

import java.util.function.IntSupplier;

/**
 * An abstraction for login network packets.
 *
 * @see SimpleChannel#simpleLoginMessageBuilder(Class, int)
 */
public interface SimpleLoginMessage extends SimpleMessage, IntSupplier {
    /**
     * {@return the index of this packet}
     */
    int getLoginIndex();

    /**
     * Sets the index of this packet.
     *
     * @param loginIndex the index of the packet
     */
    void setLoginIndex(int loginIndex);

    @Override
    default int getAsInt() {
        return getLoginIndex();
    }
}
