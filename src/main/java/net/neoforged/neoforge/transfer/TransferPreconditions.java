/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;

public class TransferPreconditions {
    private TransferPreconditions() {}

    /**
     * Ensures the value passed in is non-negative, throws otherwise.
     *
     * @return The value passed.
     * @throws ReportedException when value is negative.
     */
    public static int checkNonNegative(int value) {
        if (value >= 0) return value;

        CrashReport report = CrashReport.forThrowable(new IllegalArgumentException("Value must be non-negative"), "Value was negative");
        report.addCategory("Non-negative")
                .setDetail("Value", value);
        throw new ReportedException(report);
    }
}
