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
import net.neoforged.neoforge.network.handling.IPayloadHandler;

/**
 * The internal implementation of {@link IPayloadRegistrar} for modded packets.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
class ModdedPacketRegistrar implements IPayloadRegistrar {
    private final String modId;
    private final Map<ResourceLocation, PayloadRegistration<?>> configurationPayloads;
    private final Map<ResourceLocation, PayloadRegistration<?>> playPayloads;
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
        this.valid = source.valid;
    }

    public Map<ResourceLocation, PayloadRegistration<?>> getConfigurationRegistrations() {
        return ImmutableMap.copyOf(configurationPayloads);
    }

    public Map<ResourceLocation, PayloadRegistration<?>> getPlayRegistrations() {
        return ImmutableMap.copyOf(playPayloads);
    }

    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar play(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, IPayloadHandler<T> handler) {
        play(id, new PayloadRegistration<>(reader, handler, version, Optional.empty(), optional));
        return this;
    }

    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar configuration(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, IPayloadHandler<T> handler) {
        configuration(id, new PayloadRegistration<>(reader, handler, version, Optional.empty(), optional));
        return this;
    }

    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar play(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, Consumer<DirectionalPayloadHandlerBuilder<T>> handler) {
        DirectionalPayloadHandlerBuilder<T> builder = new DirectionalPayloadHandlerBuilder<>();
        handler.accept(builder);
        DirectionalPayloadHandler<T> innerHandler = builder.build();
        play(id, new PayloadRegistration<>(reader, innerHandler, version, innerHandler.flow(), optional));
        return this;
    }

    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar configuration(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, Consumer<DirectionalPayloadHandlerBuilder<T>> handler) {
        DirectionalPayloadHandlerBuilder<T> builder = new DirectionalPayloadHandlerBuilder<>();
        handler.accept(builder);
        DirectionalPayloadHandler<T> innerHandler = builder.build();
        configuration(id, new PayloadRegistration<>(reader, innerHandler, version, innerHandler.flow(), optional));
        return this;
    }

    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar common(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, Consumer<DirectionalPayloadHandlerBuilder<T>> handler) {
        DirectionalPayloadHandlerBuilder<T> builder = new DirectionalPayloadHandlerBuilder<>();
        handler.accept(builder);
        DirectionalPayloadHandler<T> innerHandler = builder.build();
        play(id, new PayloadRegistration<>(reader, innerHandler, version, innerHandler.flow(), optional));
        configuration(id, new PayloadRegistration<>(reader, innerHandler, version, innerHandler.flow(), optional));
        return this;
    }

    private void configuration(final ResourceLocation id, PayloadRegistration<?> registration) {
        validatePayload(id, configurationPayloads);

        configurationPayloads.put(id, registration);
    }

    private void play(final ResourceLocation id, PayloadRegistration<?> registration) {
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
