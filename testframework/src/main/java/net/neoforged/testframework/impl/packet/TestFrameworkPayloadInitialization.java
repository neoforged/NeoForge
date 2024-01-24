/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl.packet;

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

        registrar.play(ChangeStatusPayload.ID, buf -> ChangeStatusPayload.decode(framework, buf), (payload, context) -> context.workHandler().submitAsync(() -> payload.handle(context)));
        registrar.play(ChangeEnabledPayload.ID, buf -> ChangeEnabledPayload.decode(framework, buf), (payload, context) -> context.workHandler().submitAsync(() -> payload.handle(context)));
    }
}
