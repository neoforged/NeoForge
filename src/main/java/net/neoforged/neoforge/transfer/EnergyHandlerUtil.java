/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;

/**
 * Utility class for handling various {@link IEnergyHandler} interactions
 */
public final class EnergyHandlerUtil {
    /**
     * @param handler Energy Handler to iterate
     * @return Total energy stored across all of its sub-buffers. This is a long given the accumulation factor can be several max {@code ints} together.
     */
    public static long getAmount(IEnergyHandler handler) {
        var sum = 0;

        for (var i = 0; i < handler.size(); i++) {
            //this can only ever be half max long
            sum += handler.getAmount(i);
        }
        return sum;
    }

    /**
     * @param handler Energy Handler to iterate
     * @return Total capacity across all of its sub-buffers.
     */
    public static long getCapacity(IEnergyHandler handler) {
        var sum = 0;
        var size = handler.size();
        for (var i = 0; i < size; i++) {
            //this can only ever be half max long
            sum += handler.getCapacity(i);
        }
        return sum;
    }

    private EnergyHandlerUtil() {}
}
