/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.forge.snapshots;

import net.minecraft.CrashReport;
import net.minecraft.client.Options;

public class ForgeSnapshotsMod {
    public static final String BRANDING_NAME = "NeoForge";
    public static final String BRANDING_ID = "neoforge";

    public static void processOptions(Options.FieldAccess fieldAccess) {}

    public static void logStartupWarning() {}

    public static void addCrashReportHeader(StringBuilder builder, CrashReport crashReport) {}
}
