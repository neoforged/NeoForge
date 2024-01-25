/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps;

import com.mojang.serialization.Codec;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * A version of {@link DataMapType data map types} that has two more features for compatibility and conflict handling: mergers and removers.
 * <p>
 * A {@link #remover() remover} will be used to support targeted removals that
 * support decomposition, instead of the removal of the entire value. That way, for instance, one is able to remove just a value with
 * a specific key from a {@link Map map-based} data map, instead of the entire map.
 * <br>
 * To use a remover one has to change the structure of the {@code remove} list, to an object:
 * 
 * <pre>
 * <code>
 * "remove": {
 *     "someobject:someid": {} // Remover object
 * }
 * </code>
 * </pre>
 * 
 * Or, to an object list:
 * 
 * <pre>
 * <code>
 * "remove": [
 *  {
 *      "key": someobject:someid",
 *      "remover": {} // Remover object. Optional. If not provided, the attached value will be removed from the object completely, without invoking the remover
 *  }
 * ]
 * </code>
 * </pre>
 * 
 * <p>
 * Advanced data map types also have the ability of handling conflicts between datapacks that attach an object to the same registry object.
 * Using {@link #merger() mergers}, {@linkplain DataMapValueMerger#listMerger() collection-based} data maps can, as such, merge values provided by multiple packs
 * in the same collection. <br>
 * The {@link DataMapValueMerger#defaultMerger() default merge} will however have the overriding behaviour of "last come wins", similar to recipes.
 *
 * @param <T>  the registry type
 * @param <R>  the type of the attached data
 * @param <VR> the type of the remover
 */
public final class AdvancedDataMapType<T, R, VR extends DataMapValueRemover<T, R>> extends DataMapType<T, R> {
    private final Codec<VR> remover;
    private final DataMapValueMerger<T, R> merger;

    private AdvancedDataMapType(ResourceKey<Registry<R>> registryKey, ResourceLocation id, Codec<T> codec, @Nullable Codec<T> networkCodec, boolean mandatorySync, Codec<VR> remover, DataMapValueMerger<T, R> merger) {
        super(registryKey, id, codec, networkCodec, mandatorySync);
        this.remover = Objects.requireNonNull(remover, "remover must not be null");
        this.merger = Objects.requireNonNull(merger, "merger must not be null");
    }

    /**
     * {@return the codec used to create removers}
     */
    public Codec<VR> remover() {
        return remover;
    }

    /**
     * {@return the merger that handles data map conflicts}
     */
    public DataMapValueMerger<T, R> merger() {
        return merger;
    }

    /**
     * {@return an advanced data map type builder}
     *
     * @param id       the ID of the data map
     * @param registry the key of the registry the data map is for
     * @param codec    the codec used to deserialize the values from JSON
     * @param <T>      the type of the data map
     * @param <R>      the registry the data is for
     */
    public static <T, R> AdvancedDataMapType.Builder<T, R, DataMapValueRemover.Default<T, R>> builder(ResourceLocation id, ResourceKey<Registry<R>> registry, Codec<T> codec) {
        return new AdvancedDataMapType.Builder<>(registry, id, codec).remover(DataMapValueRemover.Default.codec());
    }

    /**
     * A builder for {@link AdvancedDataMapType advanced data map types}.
     *
     * @param <T>  the type of the data
     * @param <R>  the registry the data is for
     * @param <VR> the type of the remover
     */
    public static final class Builder<T, R, VR extends DataMapValueRemover<T, R>> extends DataMapType.Builder<T, R> {
        // The remover will be set in the default builder factory, as otherwise it's not generically safe
        private Codec<VR> remover;
        private DataMapValueMerger<T, R> merger = DataMapValueMerger.defaultMerger();

        Builder(ResourceKey<Registry<R>> registryKey, ResourceLocation id, Codec<T> codec) {
            super(registryKey, id, codec);
        }

        /**
         * Configures a remover for the data map.
         *
         * @param remover a codec used to decode the remover
         * @param <VR1>   the type of the new remover
         * @return the builder instance
         * @see DataMapValueRemover
         */
        public <VR1 extends DataMapValueRemover<T, R>> AdvancedDataMapType.Builder<T, R, VR1> remover(Codec<VR1> remover) {
            this.remover = (Codec) remover;
            return (Builder<T, R, VR1>) this;
        }

        /**
         * Configures the merger that will handle conflicting values for the same registry object.
         *
         * @param merger a merger that handles conflicting values
         * @return the builder instance
         */
        public AdvancedDataMapType.Builder<T, R, VR> merger(DataMapValueMerger<T, R> merger) {
            this.merger = merger;
            return this;
        }

        /**
         * Marks the data map as synced. <br>
         * A synced data map will be sent to clients that support it.
         *
         * @param networkCodec a codec used to sync the values
         * @param mandatory    if {@code true}, clients that do not support this data map will not be able to connect to the server
         * @return the builder instance
         */
        @Override
        public AdvancedDataMapType.Builder<T, R, VR> synced(Codec<T> networkCodec, boolean mandatory) {
            super.synced(networkCodec, mandatory);
            return this;
        }

        /**
         * {@return a built advanced data map type}
         */
        @Override
        public AdvancedDataMapType<T, R, VR> build() {
            return new AdvancedDataMapType<>(registryKey, id, codec, networkCodec, mandatorySync, remover, merger);
        }
    }
}
