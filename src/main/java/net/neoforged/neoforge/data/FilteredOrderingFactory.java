/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.data.DataProvider;
import org.jetbrains.annotations.Nullable;

/**
 * Default implementation of {@link FieldOrderingFactory} that applies a given comparator if both the path and JSON filters pass.
 * 
 * See the {@link Builder} for convenient methods to create common filters and comparators.
 */
public record FilteredOrderingFactory(Predicate<Path> pathFilter, Predicate<JsonElement> jsonFilter, Comparator<String> comparator) implements FieldOrderingFactory {
    @Override
    @Nullable
    public Comparator<String> getKeyComparator(JsonElement json, Path path) {
        if (this.pathFilter.test(path) && this.jsonFilter.test(json)) {
            return this.comparator;
        }
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Predicate<Path> pathFilter = p -> true;
        private Predicate<JsonElement> jsonFilter = j -> true;
        private Comparator<String> comparator;

        /**
         * Applies a path filter that checks if the output path contains the given string as a path segment.
         * <p>
         * This can be used to filter by object type, e.g. "recipe", "advancement", etc.
         */
        public Builder forObjectPath(String objPath) {
            String pathSegment = File.separator + objPath + File.separator;
            return pathFilter(p -> p.toString().contains(pathSegment));
        }

        /**
         * Applies a JSON filter that checks if the JSON object has a field with the given key and string value.
         * <p>
         * This can be used to filter by object subtype, e.g. "type": "minecraft:crafting_shaped" for recipes.
         */
        public Builder forObjectSubtype(String typeKey, String subtype) {
            return jsonFilter(typeKey, new JsonPrimitive(subtype));
        }

        /**
         * Version of {@link #forObjectSubtype(String, String)} that uses "type" as the key.
         */
        public Builder forObjectSubtype(String subtype) {
            return forObjectSubtype("type", subtype);
        }

        /**
         * Applies an ordering based on the given field names, in the given order.
         * <p>
         * Any keys not in the list are placed after the listed keys.
         * 
         * @apiNote This method inherits the default mappings from {@link DataProvider#FIXED_ORDER_FIELDS}.
         */
        public Builder order(String... fieldsInOrder) {
            return orderMap(map -> {
                for (int i = 0; i < fieldsInOrder.length; i++) {
                    map.put(fieldsInOrder[i], 2 + i); // Use this offset of 2 to avoid clashing with the default settings.
                }
                map.defaultReturnValue(2 + fieldsInOrder.length); // Fields not in the list get the next index after the last one.
            });
        }

        /**
         * Applies an ordering based on a modified copy of {@link DataProvider#FIXED_ORDER_FIELDS}.
         * <p>
         * This method allows you to specify custom sort orderings for individual keys. Keys with a lower numeric value are sorted first.
         * 
         * @apiNote Unless explicitly set by the {@code config}, the map's default return value is 2.
         */
        public Builder orderMap(Consumer<Object2IntOpenHashMap<String>> config) {
            Object2IntOpenHashMap<String> map = defaultMap();
            config.accept(map);
            Comparator<String> comparator = Comparator.comparingInt(map).thenComparing(Function.identity());
            return comparator(comparator);
        }

        /**
         * Sets a custom path filter. If the path filter returns false, the comparator will not be applied.
         */
        public Builder pathFilter(Predicate<Path> filter) {
            this.pathFilter = filter;
            return this;
        }

        /**
         * Applies a JSON filter that checks if the JSON object has a field with the given key and value.
         */
        public <T> Builder jsonFilter(String key, JsonElement value) {
            return jsonFilter(j -> j.isJsonObject() && value.equals(j.getAsJsonObject().get(key)));
        }

        /**
         * Sets a custom JSON filter. If the JSON filter returns false, the comparator will not be applied.
         */
        public Builder jsonFilter(Predicate<JsonElement> filter) {
            this.jsonFilter = filter;
            return this;
        }

        /**
         * Sets a fully custom comparator to be applied if both filters pass.
         */
        public Builder comparator(Comparator<String> comparator) {
            this.comparator = comparator;
            return this;
        }

        /**
         * Builds the {@link FilteredOrderingFactory}. Before calling this method, the comparator
         * must have been set by one of: {@link #order(String...)}, {@link #orderMap(Consumer)}, or {@link #comparator(Comparator)}.
         */
        public FilteredOrderingFactory build() {
            if (this.comparator == null) throw new IllegalStateException("Comparator must be set");
            return new FilteredOrderingFactory(this.pathFilter, this.jsonFilter, this.comparator);
        }

        private static Object2IntOpenHashMap<String> defaultMap() {
            Object2IntOpenHashMap<String> map = new Object2IntOpenHashMap<>((Object2IntOpenHashMap<String>) DataProvider.FIXED_ORDER_FIELDS);
            map.defaultReturnValue(2); // This is the default return value for FIXED_ORDER_FIELDS, but the copy constructor doesn't copy it.
            return map;
        }
    }
}
