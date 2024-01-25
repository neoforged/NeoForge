/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import java.util.Objects;
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
 * <p>
 * The {@code registryNamespace} is omitted if it is {@value ResourceLocation#DEFAULT_NAMESPACE}. <br>
 * The structure of the json file is as follows:
 *
 * <pre>
 * <code>
 *     {
 *         "replace": false // If true, all previous data will be cleared.
 *         // The values object is a map of registry entry ID / tag to data map values.
 *         "values": {
 *             "someobject:someid": {}, // The object being attached
 *             "#somepath:sometag": {} // Tags are also supported. All objects in the tag will then receieve the same value
 *         },
 *         // Optional object. Entries specified here will be removed after the data of the current json file is attached
 *         "remove": ["minecraft:carrot", "#minecraft:logs"]
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
 * Both datapack registries and normal, built-in registries support data maps.
 *
 * <p>
 * You can access a data map using {@link net.neoforged.neoforge.registries.IRegistryExtension#getDataMap(DataMapType)} and {@link IWithData#getData(DataMapType)}. <br>
 * You can usually go through {@linkplain net.minecraft.core.Holder#getData(DataMapType)} Holder} implementations in order to get the data of an object directly.
 *
 * @see AdvancedDataMapType for more functionality
 */
public sealed class DataMapType<T, R> permits AdvancedDataMapType {
    private final ResourceKey<Registry<R>> registryKey;
    private final ResourceLocation id;
    private final Codec<T> codec;
    private final @Nullable Codec<T> networkCodec;
    private final boolean mandatorySync;

    DataMapType(ResourceKey<Registry<R>> registryKey, ResourceLocation id, Codec<T> codec, @Nullable Codec<T> networkCodec, boolean mandatorySync) {
        Preconditions.checkArgument(networkCodec != null || !mandatorySync, "Mandatory sync cannot be enabled when the attachment isn't synchronized");

        this.registryKey = Objects.requireNonNull(registryKey, "registryKey must not be null");
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.codec = Objects.requireNonNull(codec, "codec must not be null");
        this.networkCodec = networkCodec;
        this.mandatorySync = mandatorySync;
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
    public static <T, R> Builder<T, R> builder(ResourceLocation id, ResourceKey<Registry<R>> registry, Codec<T> codec) {
        return new Builder<>(registry, id, codec);
    }

    /**
     * {@return the key of the registry this data map is for}
     */
    public ResourceKey<Registry<R>> registryKey() {
        return registryKey;
    }

    /**
     * {@return the ID of this data map}
     */
    public ResourceLocation id() {
        return id;
    }

    /**
     * {@return the codec used to decode values}
     */
    public Codec<T> codec() {
        return codec;
    }

    /**
     * {@return the codec used to sync values}
     */
    public @Nullable Codec<T> networkCodec() {
        return networkCodec;
    }

    /**
     * {@return {@code true} if this data map must be present on the client, and {@code false} otherwise}
     */
    public boolean mandatorySync() {
        return mandatorySync;
    }

    /**
     * A builder for {@link DataMapType data map types}.
     *
     * @param <T> the type of the data
     * @param <R> the registry the data is for
     */
    public static sealed class Builder<T, R> permits AdvancedDataMapType.Builder {
        protected final ResourceKey<Registry<R>> registryKey;
        protected final ResourceLocation id;
        protected final Codec<T> codec;

        protected @Nullable Codec<T> networkCodec;
        protected boolean mandatorySync;

        Builder(ResourceKey<Registry<R>> registryKey, ResourceLocation id, Codec<T> codec) {
            this.registryKey = registryKey;
            this.id = id;
            this.codec = codec;
        }

        /**
         * Marks the data map as synced. <br>
         * A synced data map will be sent to clients that support it.
         *
         * @param networkCodec a codec used to sync the values
         * @param mandatory    if {@code true}, clients that do not support this data map will not be able to connect to the server
         * @return the builder instance
         */
        public Builder<T, R> synced(Codec<T> networkCodec, boolean mandatory) {
            this.mandatorySync = mandatory;
            this.networkCodec = networkCodec;
            return this;
        }

        /**
         * {@return a built data map type}
         */
        public DataMapType<T, R> build() {
            return new DataMapType<>(registryKey, id, codec, networkCodec, mandatorySync);
        }
    }
}
