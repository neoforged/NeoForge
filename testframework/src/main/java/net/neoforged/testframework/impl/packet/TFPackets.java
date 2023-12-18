/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl.packet;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.event.RegisterPacketHandlerEvent;
import net.neoforged.neoforge.network.registration.registrar.IPayloadRegistrar;
import net.neoforged.testframework.impl.MutableTestFramework;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record TFPackets(MutableTestFramework framework) {

    @SubscribeEvent
    public void onNetworkSetup(final RegisterPacketHandlerEvent event) {

        final IPayloadRegistrar registrar = event.registrar(NeoForgeVersion.MOD_ID);

        registrar.play(ChangeStatusPacket.ID, buf -> ChangeStatusPacket.decode(framework, buf), (context, payload) -> context.workHandler().submitAsync(() -> {
            payload.handle(context);
        }));
        registrar.play(ChangeEnabledPacket.ID, buf -> ChangeEnabledPacket.decode(framework, buf), (context, payload) -> context.workHandler().submitAsync(() -> {
            payload.handle(context);
        }));
    }
}
