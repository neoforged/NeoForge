/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.data;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * A container for data to be passed to {@link BakedModel} instances.
 * <p>
 * All objects stored in here <b>MUST BE IMMUTABLE OR THREAD-SAFE</b>.
 * Properties will be accessed from another thread.
 *
 * @see ModelProperty
 * @see BlockEntity#getModelData()
 * @see BakedModel#getQuads(BlockState, Direction, RandomSource, ModelData, RenderType)
 * @see BakedModel#getModelData(BlockAndTintGetter, BlockPos, BlockState, ModelData)
 */
public final class ModelData {
    public static final ModelData EMPTY = ModelData.builder().build();

    private final Map<ModelProperty<?>, Object> properties;

    @Nullable
    private Set<ModelProperty<?>> propertySetView;

    private ModelData(Map<ModelProperty<?>, Object> properties) {
        this.properties = properties;
    }

    /**
     * {@return an unmodifiable set of properties contained in this model data container}
     */
    public Set<ModelProperty<?>> getProperties() {
        var view = propertySetView;
        if (view == null) {
            propertySetView = view = Collections.unmodifiableSet(properties.keySet());
        }
        return view;
    }

    /**
     * {@return true if this model data container has a value for the given property}
     */
    public boolean has(ModelProperty<?> property) {
        return properties.containsKey(property);
    }

    /**
     * {@return the value associated with the given property, or null if none exists}
     */
    @Nullable
    public <T> T get(ModelProperty<T> property) {
        return (T) properties.get(property);
    }

    /**
     * {@return the value for a given property, or the provided default value if none exists}
     */
    public <T> T getOrDefault(ModelProperty<T> property, T defaultValue) {
        return (T) properties.getOrDefault(property, defaultValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return properties.equals(((ModelData) o).properties);
    }

    @Override
    public int hashCode() {
        return properties.hashCode();
    }

    /**
     * {@return a {@link ModelData.Builder} initially containing all property-value pairs in this container}
     */
    public Builder derive() {
        return new Builder(this);
    }

    /**
     * {@return a {@link ModelData.Builder} initially containing no properties}
     */
    public static Builder builder() {
        return new Builder(null);
    }

    public static final class Builder {
        /**
         * Hash maps are slower than array maps for *extremely* small maps (empty maps or singletons are the most
         * extreme examples). Many block entities/models only use a single model data property, which means the
         * overhead of hashing is quite wasteful. However, we do want to support any number of properties with
         * reasonable performance. Therefore, we use an array map until the number of properties reaches this
         * threshold, at which point we convert it to a hash map.
         */
        private static final int HASH_THRESHOLD = 4;

        private Map<ModelProperty<?>, Object> properties;

        private Builder(@Nullable ModelData parent) {
            if (parent != null) {
                // When cloning the map, use the expected type based on size
                properties = parent.properties.size() >= HASH_THRESHOLD ? new Reference2ObjectOpenHashMap<>(parent.properties) : new Reference2ObjectArrayMap<>(parent.properties);
            } else {
                // Allocate the maximum number of entries we'd ever put into the map.
                // We convert to a hash map *after* insertion of the HASH_THRESHOLD
                // entry, so we need at least that many spots.
                properties = new Reference2ObjectOpenHashMap<>(HASH_THRESHOLD);
            }
        }

        @Contract("_, _ -> this")
        public <T> Builder with(ModelProperty<T> property, T value) {
            Preconditions.checkState(property.test(value), "The provided value is invalid for this property.");
            properties.put(property, value);
            // Convert to a hash map if needed
            if (properties.size() == HASH_THRESHOLD && properties instanceof Reference2ObjectArrayMap<ModelProperty<?>, Object>) {
                properties = new Reference2ObjectOpenHashMap<>(properties);
            }
            return this;
        }

        @Contract("-> new")
        public ModelData build() {
            return new ModelData(properties);
        }
    }
}
