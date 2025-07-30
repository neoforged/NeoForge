/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.neoforged.neoforge.transfer.ResourceHandlerDeprecationHandling;
import org.jetbrains.annotations.ApiStatus;

// TODO: Remove after migrations have been established. This is more for info to validate assumptions on use rather than guessing and inquiring a subset.
@Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6)
@ApiStatus.Internal
public class SnapshotJournalDebugInfo {
    private final static Object2IntMap<Class<?>> depths = new Object2IntOpenHashMap<>();
    private final static Object2IntMap<Class<?>> depthView = Object2IntMaps.unmodifiable(depths);

    /**
     * @return An unmodifiable copy of {@link #depths} for inspecting debug values.
     */
    public static Object2IntMap<Class<?>> getDepthView() {
        return depthView;
    }

    public static void updateDeepestSnapshot(int depth, SnapshotJournal<?> snapshotJournal) {
        var journalClass = snapshotJournal.getClass();
        if (depth > depths.getInt(journalClass)) {
            depths.put(journalClass, depth);
        }
    }
}
