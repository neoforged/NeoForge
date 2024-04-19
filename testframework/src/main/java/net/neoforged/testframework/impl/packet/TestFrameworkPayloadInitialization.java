/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl.packet;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.testframework.impl.MutableTestFramework;

@ApiStatus.Internal
public record TestFrameworkPayloadInitialization(MutableTestFramework framework) {
    @SubscribeEvent
    public void onNetworkSetup(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playBidirectional(ChangeStatusPayload.ID,
                StreamCodec.of((RegistryFriendlyByteBuf buf, ChangeStatusPayload packet) -> packet.write(buf), buf -> ChangeStatusPayload.decode(framework, buf)),
                (payload, context) -> payload.handle(context));

        registrar.playBidirectional(ChangeEnabledPayload.ID,
                StreamCodec.of((buf, packet) -> packet.write(buf), buf -> ChangeEnabledPayload.decode(framework, buf)),
                (payload, context) -> payload.handle(context));
    }
}
