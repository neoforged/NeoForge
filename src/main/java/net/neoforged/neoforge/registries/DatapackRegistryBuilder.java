/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryValidator;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public final class DatapackRegistryBuilder<T> {
    private final boolean reloadable;
    @Nullable
    private ResourceKey<Registry<T>> key;
    @Nullable
    private Codec<T> codec;
    @Nullable
    private Codec<T> networkCodec;
    private RegistryValidator<T> validator = RegistryValidator.none();
    private Consumer<RegistryBuilder<T>> configurator = _ -> {};

    DatapackRegistryBuilder(boolean reloadable) {
        this.reloadable = reloadable;
    }

    /// Specify the root registry key of the new datapack registry.
    ///
    /// @param key The key of the registry
    /// @return this builder
    public DatapackRegistryBuilder<T> key(ResourceKey<Registry<T>> key) {
        this.key = key;
        return this;
    }

    /// Specify the codec to be used for loading data from datapacks on servers.
    ///
    /// @param codec The codec to use
    /// @return this builder
    public DatapackRegistryBuilder<T> codec(Codec<T> codec) {
        this.codec = codec;
        return this;
    }

    /// Specify the codec to be used for syncing loaded data to clients.
    ///
    /// If a network codec is specified, clients must have this datapack registry/mod
    /// when joining a server that has this datapack registry/mod.
    /// The data will be synced using the network codec and accessible via [ClientPacketListener#registryAccess()].
    ///
    /// @param networkCodec The network codec to use
    /// @return this builder
    /// @throws IllegalArgumentException if this builder is for a reloadable datapack registry, which do not support syncing
    public DatapackRegistryBuilder<T> networkCodec(Codec<T> networkCodec) {
        if (this.reloadable) {
            throw new IllegalArgumentException("Reloadable datapack registries cannot be synced");
        }
        this.networkCodec = networkCodec;
        return this;
    }

    /// Specify a validator to check the datapack registry after it was loaded.
    ///
    /// @param validator The validator to use
    /// @return this builder
    public DatapackRegistryBuilder<T> validator(RegistryValidator<T> validator) {
        this.validator = validator;
        return this;
    }

    /// Specify a consumer that configures the provided [RegistryBuilder].
    ///
    /// @param configurator The registry configurator
    /// @return this builder
    public DatapackRegistryBuilder<T> configurator(Consumer<RegistryBuilder<T>> configurator) {
        this.configurator = configurator;
        return this;
    }

    NewDatapackRegistryEvent.RegistryData<T> build() {
        Objects.requireNonNull(this.key, "No registry key specified");
        Objects.requireNonNull(this.codec, "No loading codec specified");
        return new NewDatapackRegistryEvent.RegistryData<>(new RegistryDataLoader.RegistryData<>(this.key, this.codec, this.validator, this.configurator), this.networkCodec);
    }
}
