/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import org.jetbrains.annotations.Nullable;

/**
 * A registry data map contains data-driven object that can be attached to a registry object. <p>
 * Data maps are registered to the {@link RegisterDataMapTypesEvent}. <p>
 * They are loaded from JSON files located at:
 * 
 * <pre>
 * <code>:mapNamespace/data_maps/:registryNamespace/:registryPath/:mapPath.json</code>
 * </pre>
 * 
 * The {@code registryNamespace} is omitted if it is {@value ResourceLocation#DEFAULT_NAMESPACE}. <br>
 * The structure of the json file is as follows:
 * 
 * <pre>
 * <code>
 *     {
 *         "replace": false // If true, all previous data will be cleared.
 *         // The values object is a map of registry entry ID / tag to attachment values.
 *         "values": {
 *             "someobject:someid": {},
 *             "#somepath:sometag": {}
 *         },
 *         // Optional object. Entries specified here will be removed after the data of the current json file is attached
 *         // The remove object can also be a list (i.e. ["minecraft:carrot", "#minecraft:logs"]). When it is a list the data of the objects with the specified IDs/tags will simply be removed, without invoking the remover.
 *         "remove": {
 *              "someobject:someid2": {} // Remover object
 *         }
 *     }
 * </pre>
 * 
 * </code>
 * Data maps support conditions both JSON-level and attachment-level through the {@value ConditionalOps#CONDITIONAL_VALUE_KEY} object.
 * <p>
 * Data maps may be synced by specifying a {@link #networkCodec()}. If the map is {@link #mandatorySync() mandatory},
 * then vanilla clients (or any client that doesn't support this map) will not be able to connect.
 *
 * <p>
 * Data maps also provide a {@link #remover() remover} which will be used to support targeted removals that
 * support decomposition, instead of the removal of the entire value. That way, for instance, one is able to remove just a value with
 * a specific key from a {@link java.util.Map map-based} data map, instead of the entire map.
 *
 * <p>
 * Data maps have the ability of handling conflicts between datapacks that attach an object to the same registry object.
 * Using {@link #merger() mergers}, {@linkplain DataMapValueMerger#listMerger() collection-based} data maps can, as such, merge values provided by multiple packs
 * in the same collection. <br>
 * The {@link DataMapValueMerger#defaultMerger() default merge} will however have the overriding behaviour of "last come wins", similar to recipes.
 *
 * <p>
 * Both datapack registries and normal, built-in registries support data maps.
 *
 * @param registryKey   the ID of the registry this data map is for
 * @param id            the ID of the data map
 * @param codec         the codec used to decode and encode the values to and from JSON
 * @param networkCodec  an optional codec that is used to sync the data map to clients
 * @param mandatorySync if {@code true}, this data map must be present on the client
 * @param remover       a remover used to remove specific values
 * @param merger        a merger that merges conflicting values
 * @param <T>           the type of the data map
 * @param <R>           the registry the data map is for
 * @param <VR>          the type of the remover
 */
public record DataMapType<T, R, VR extends DataMapValueRemover<T, R>>(
        ResourceKey<Registry<R>> registryKey,
        ResourceLocation id,
        Codec<T> codec, @Nullable Codec<T> networkCodec,
        boolean mandatorySync,
        Codec<VR> remover,
        DataMapValueMerger<T, R> merger) {

    public DataMapType {
        Preconditions.checkArgument(networkCodec != null || !mandatorySync, "Mandatory sync cannot be enabled when the attachment isn't synchronized");
    }

    /**
     * {@return a data map type builder}
     *
     * @param id       the ID of the data map
     * @param registry the key of the registry the data map is for
     * @param codec    the codec used to deserialize the values from JSON
     * @param <T>      the type of the data map
     * @param <R>      the registry the data is for
     */
    public static <T, R> Builder<T, R, DataMapValueRemover.Default<T, R>> builder(ResourceLocation id, ResourceKey<Registry<R>> registry, Codec<T> codec) {
        return new Builder<>(registry, id, codec).remover(DataMapValueRemover.Default.codec());
    }

    /**
     * A builder for {@link DataMapType data map types}.
     *
     * @param <T>  the type of the data
     * @param <R>  the registry the data is for
     * @param <VR> the type of the remover
     */
    public static class Builder<T, R, VR extends DataMapValueRemover<T, R>> {
        private final ResourceKey<Registry<R>> registryKey;
        private final ResourceLocation id;
        private final Codec<T> codec;

        private @Nullable Codec<T> networkCodec;
        private boolean mandatorySync;
        private Codec<VR> remover;
        private DataMapValueMerger<T, R> merger = DataMapValueMerger.defaultMerger();

        private Builder(ResourceKey<Registry<R>> registryKey, ResourceLocation id, Codec<T> codec) {
            this.registryKey = registryKey;
            this.id = id;
            this.codec = codec;
        }

        /**
         * Configures a remover for the data map.
         *
         * @param remover a codec used to decode the remover
         * @param <VR1>   the type of the new remover
         * @return the builder instance
         * @see DataMapValueRemover
         */
        public <VR1 extends DataMapValueRemover<T, R>> Builder<T, R, VR1> remover(Codec<VR1> remover) {
            this.remover = (Codec) remover;
            return (Builder<T, R, VR1>) this;
        }

        /**
         * Marks the data map as synced. <br>
         * A synced data map will be sent to clients that support it.
         *
         * @param networkCodec a codec used to sync the values
         * @param mandatory    if {@code true}, clients that do not support this data map will not be able to connect to the server
         * @return the builder instance
         */
        public Builder<T, R, VR> synced(Codec<T> networkCodec, boolean mandatory) {
            this.mandatorySync = mandatory;
            this.networkCodec = networkCodec;
            return this;
        }

        /**
         * Configures the merger that will handle conflicting values for the same registry object.
         *
         * @param merger a merger that handles conflicting values
         * @return the builder instance
         */
        public Builder<T, R, VR> merger(DataMapValueMerger<T, R> merger) {
            this.merger = merger;
            return this;
        }

        /**
         * {@return a built data map type}
         */
        public DataMapType<T, R, VR> build() {
            return new DataMapType<>(registryKey, id, codec, networkCodec, mandatorySync, remover, merger);
        }
    }
}
