/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.network.ConnectionProtocol;
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
    private final Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration<?>>> payloads;
    private Optional<String> version = Optional.empty();
    private boolean optional = false;
    private boolean valid = true;

    public ModdedPacketRegistrar(String modId) {
        this.modId = modId;
        this.payloads = new IdentityHashMap<>();
    }

    private ModdedPacketRegistrar(ModdedPacketRegistrar source) {
        this.modId = source.modId;
        this.payloads = source.payloads;
        this.version = source.version;
        this.optional = source.optional;
        this.valid = source.valid;
    }

    public Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration<?>>> getRegistrations() {
        return this.payloads;
    }

    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar play(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, IPayloadHandler<T> handler) {
        play(id, new PayloadRegistration<>(id, reader, handler, version, Optional.empty(), optional));
        return this;
    }

    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar configuration(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, IPayloadHandler<T> handler) {
        configuration(id, new PayloadRegistration<>(id, reader, handler, version, Optional.empty(), optional));
        return this;
    }

    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar play(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, Consumer<DirectionalPayloadHandlerBuilder<T>> handler) {
        DirectionalPayloadHandlerBuilder<T> builder = new DirectionalPayloadHandlerBuilder<>();
        handler.accept(builder);
        DirectionalPayloadHandler<T> innerHandler = builder.build();
        play(id, new PayloadRegistration<>(id, reader, innerHandler, version, innerHandler.flow(), optional));
        return this;
    }

    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar configuration(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, Consumer<DirectionalPayloadHandlerBuilder<T>> handler) {
        DirectionalPayloadHandlerBuilder<T> builder = new DirectionalPayloadHandlerBuilder<>();
        handler.accept(builder);
        DirectionalPayloadHandler<T> innerHandler = builder.build();
        configuration(id, new PayloadRegistration<>(id, reader, innerHandler, version, innerHandler.flow(), optional));
        return this;
    }

    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar common(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, Consumer<DirectionalPayloadHandlerBuilder<T>> handler) {
        DirectionalPayloadHandlerBuilder<T> builder = new DirectionalPayloadHandlerBuilder<>();
        handler.accept(builder);
        DirectionalPayloadHandler<T> innerHandler = builder.build();
        play(id, new PayloadRegistration<>(id, reader, innerHandler, version, innerHandler.flow(), optional));
        configuration(id, new PayloadRegistration<>(id, reader, innerHandler, version, innerHandler.flow(), optional));
        return this;
    }

    private <T extends CustomPacketPayload> void register(ConnectionProtocol protocol, ResourceLocation id, PayloadRegistration<T> reg) {
        if (!this.valid) {
            throw new RegistrationFailedException(id, this.modId, RegistrationFailedException.Reason.INVALID_REGISTRAR);
        }

        Map<ResourceLocation, PayloadRegistration<?>> protocolPayloads = this.payloads.computeIfAbsent(protocol, k -> new HashMap<>());

        if (protocolPayloads.containsKey(id)) {
            throw new RegistrationFailedException(id, this.modId, RegistrationFailedException.Reason.DUPLICATE_ID);
        }

        if (!id.getNamespace().equals(this.modId)) {
            throw new RegistrationFailedException(id, this.modId, RegistrationFailedException.Reason.INVALID_NAMESPACE);
        }

        protocolPayloads.put(id, reg);
    }

    private void configuration(final ResourceLocation id, PayloadRegistration<?> registration) {
        register(ConnectionProtocol.CONFIGURATION, id, registration);
    }

    private void play(final ResourceLocation id, PayloadRegistration<?> registration) {
        register(ConnectionProtocol.PLAY, id, registration);
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
