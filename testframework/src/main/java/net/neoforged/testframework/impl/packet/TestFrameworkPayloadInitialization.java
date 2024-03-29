/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.testframework.impl.MutableTestFramework;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record TestFrameworkPayloadInitialization(MutableTestFramework framework) {
    @SubscribeEvent
    public void onNetworkSetup(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar(NeoForgeVersion.MOD_ID);

        registrar.play(ChangeStatusPayload.ID, StreamCodec.of((RegistryFriendlyByteBuf buf, ChangeStatusPayload packet) -> packet.write(buf), buf -> ChangeStatusPayload.decode(framework, buf)), (payload, context) -> context.workHandler().submitAsync(() -> payload.handle(context)));
        registrar.play(ChangeEnabledPayload.ID, StreamCodec.of((buf, packet) -> packet.write(buf), buf -> ChangeEnabledPayload.decode(framework, buf)), (payload, context) -> context.workHandler().submitAsync(() -> payload.handle(context)));
    }
}
