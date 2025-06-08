/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.energy;

import javax.annotation.Nonnegative;

/**
 * <b>PRIMER: New</b>
 * <p>
 * An energy handler extension when wanting to expose direct energy value mutations of a given index.
 * This is purely optional, and is mainly provided to allow mod authors to expose a direct `set` for
 * operations such as "emptying" or "filling" an energy container, non-explicitly
 */
public interface IEnergyHandlerModifiable extends IEnergyHandler {
    /**
     * Sets the amount at the given index to the supplied value.
     *
     * @param index  The index for what value to set.
     * @param amount The value to set. This should be non-negative.
     */
    void set(@Nonnegative int index, @Nonnegative int amount);
}
