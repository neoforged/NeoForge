/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data;

import com.google.gson.JsonElement;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.data.DataProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * The Field Ordering Factory allows users to provide custom comparators for ordering fields during datagen.
 * <p>
 * We don't have a ton of context at the time we need to evaluate the ordering, but the file contents and the output path should be good enough.
 */
@FunctionalInterface
public interface FieldOrderingFactory {
    /**
     * Returns a comparator for ordering fields in a JSON object.
     * <p>
     * Alternatively, returns null to use the default ordering (by {@link DataProvider#FIXED_ORDER_FIELDS}).
     * <p>
     * When multiple factories are registered, the first non-null comparator will be used.
     * 
     * @param json The JSON element being written (should be an object)
     * @param path The path the JSON is being written to
     * @return A comparator for ordering fields, or null to use the default ordering
     */
    @Nullable
    Comparator<String> getKeyComparator(JsonElement json, Path path);

    @ApiStatus.Internal
    static class Impl {
        private static final List<FieldOrderingFactory> FACTORIES = new ArrayList<>();

        public static void register(FieldOrderingFactory factory) {
            Objects.requireNonNull(factory, "Cannot register a null FieldOrderingFactory");
            synchronized (FACTORIES) { // We only need to synchronize registrations. Reads will happen far later near datagen completion.
                Impl.FACTORIES.add(factory);
            }
        }

        /**
         * Returns the current comparator for the given json/path pair.
         * <p>
         * This method takes the first non-null custom comparator, and falls back to the default comparator otherwise.
         */
        public static Comparator<String> getComparatorFor(JsonElement json, Path path) {
            for (FieldOrderingFactory factory : Impl.FACTORIES) {
                Comparator<String> comparator = factory.getKeyComparator(json, path);
                if (comparator != null) {
                    return comparator;
                }
            }
            return DataProvider.KEY_COMPARATOR;
        }
    }
}
