/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IConfigurationPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import org.jetbrains.annotations.ApiStatus;

/**
 * The internal implementation of {@link IPayloadRegistrar} for modded packets.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@ApiStatus.Internal
class ModdedPacketRegistrar implements IPayloadRegistrar {

    private final String modId;
    private final Map<ResourceLocation, ConfigurationRegistration<?>> configurationPayloads;
    private final Map<ResourceLocation, PlayRegistration<?>> playPayloads;
    private Optional<String> version = Optional.empty();
    private boolean optional = false;
    private boolean valid = true;

    public ModdedPacketRegistrar(String modId) {
        this.modId = modId;
        playPayloads = Maps.newHashMap();
        configurationPayloads = Maps.newHashMap();
    }

    private ModdedPacketRegistrar(ModdedPacketRegistrar source) {
        this.modId = source.modId;
        this.playPayloads = source.playPayloads;
        this.configurationPayloads = source.configurationPayloads;
        this.version = source.version;
        this.optional = source.optional;
    }

    public Map<ResourceLocation, ConfigurationRegistration<?>> getConfigurationRegistrations() {
        return ImmutableMap.copyOf(configurationPayloads);
    }

    public Map<ResourceLocation, PlayRegistration<?>> getPlayRegistrations() {
        return ImmutableMap.copyOf(playPayloads);
    }

    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar play(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, IPlayPayloadHandler<T> handler) {
        play(
                id, new PlayRegistration<>(
                        reader, handler, version, Optional.empty(), optional));
        return this;
    }

    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar configuration(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, IConfigurationPayloadHandler<T> handler) {
        configuration(
                id, new ConfigurationRegistration<>(
                        reader, handler, version, Optional.empty(), optional));
        return this;
    }

    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar play(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, Consumer<IDirectionAwarePayloadHandlerBuilder<T, IPlayPayloadHandler<T>>> handler) {
        final PlayPayloadHandler.Builder<T> builder = new PlayPayloadHandler.Builder<>();
        handler.accept(builder);
        final PlayPayloadHandler<T> innerHandler = builder.create();
        play(
                id, new PlayRegistration<>(
                        reader, innerHandler, version, innerHandler.flow(), optional));
        return this;
    }

    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar configuration(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, Consumer<IDirectionAwarePayloadHandlerBuilder<T, IConfigurationPayloadHandler<T>>> handler) {
        final ConfigurationPayloadHandler.Builder<T> builder = new ConfigurationPayloadHandler.Builder<>();
        handler.accept(builder);
        final ConfigurationPayloadHandler<T> innerHandler = builder.create();
        configuration(
                id, new ConfigurationRegistration<>(
                        reader, innerHandler, version, innerHandler.flow(), optional));
        return this;
    }

    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar common(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, Consumer<IDirectionAwarePayloadHandlerBuilder<T, IPayloadHandler<T>>> handler) {
        final PayloadHandlerBuilder<T> builder = new PayloadHandlerBuilder<>();
        handler.accept(builder);
        configuration(id, reader, builder::handleConfiguration);
        play(id, reader, builder::handlePlay);
        return this;
    }

    private void configuration(final ResourceLocation id, ConfigurationRegistration<?> registration) {
        validatePayload(id, configurationPayloads);

        configurationPayloads.put(id, registration);
    }

    private void play(final ResourceLocation id, PlayRegistration<?> registration) {
        validatePayload(id, playPayloads);

        playPayloads.put(id, registration);
    }

    private void validatePayload(ResourceLocation id, final Map<ResourceLocation, ?> payloads) {
        if (!valid) {
            throw new RegistrationFailedException(id, modId, RegistrationFailedException.Reason.INVALID_REGISTRAR);
        }

        if (payloads.containsKey(id)) {
            throw new RegistrationFailedException(id, modId, RegistrationFailedException.Reason.DUPLICATE_ID);
        }

        if (!id.getNamespace().equals(modId)) {
            throw new RegistrationFailedException(id, modId, RegistrationFailedException.Reason.INVALID_NAMESPACE);
        }
    }

    @Override
    public IPayloadRegistrar versioned(String version) {
        final ModdedPacketRegistrar clone = new ModdedPacketRegistrar(this);
        clone.version = Optional.of(version);
        return clone;
    }

    @Override
    public IPayloadRegistrar optional() {
        final ModdedPacketRegistrar clone = new ModdedPacketRegistrar(this);
        clone.optional = true;
        return clone;
    }

    public void invalidate() {
        valid = false;
    }
}
