/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.data;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
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

    public Set<ModelProperty<?>> getProperties() {
        var view = propertySetView;
        if (view == null) {
            propertySetView = view = Collections.unmodifiableSet(properties.keySet());
        }
        return view;
    }

    public boolean has(ModelProperty<?> property) {
        return properties.containsKey(property);
    }

    @Nullable
    public <T> T get(ModelProperty<T> property) {
        return (T) properties.get(property);
    }

    public Builder derive() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder(null);
    }

    public static final class Builder {
        /**
         * Hash maps are slower than array maps for *extremely* small maps (empty maps or singletons are the most
         * extreme examples). Many block entities/models only use a single model data property, which means the
         * overhead of hashing is quite wasteful. However, we do want to support any number of properties with
         * reasonable performance. Therefore, we use an array map until the number of properties exceeds this
         * threshold, at which point we convert it to a hash map.
         */
        private static final int HASH_THRESHOLD = 4;

        private Map<ModelProperty<?>, Object> properties;

        private Builder(@Nullable ModelData parent) {
            if (parent != null) {
                // When cloning the map, use the expected type based on size
                properties = parent.properties.size() >= HASH_THRESHOLD ? new Reference2ReferenceOpenHashMap<>(parent.properties) : new Reference2ReferenceArrayMap<>(parent.properties);
            } else {
                properties = new Reference2ReferenceArrayMap<>();
            }
        }

        @Contract("_, _ -> this")
        public <T> Builder with(ModelProperty<T> property, T value) {
            Preconditions.checkState(property.test(value), "The provided value is invalid for this property.");
            properties.put(property, value);
            // Convert to a hash map if needed
            if (properties.size() >= HASH_THRESHOLD && properties instanceof Reference2ReferenceArrayMap<ModelProperty<?>, Object>) {
                properties = new Reference2ReferenceOpenHashMap<>(properties);
            }
            return this;
        }

        @Contract("-> new")
        public ModelData build() {
            return new ModelData(properties);
        }
    }
}
