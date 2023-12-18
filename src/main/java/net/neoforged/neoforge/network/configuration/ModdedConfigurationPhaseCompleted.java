/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.configuration;

import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

/**
 * A custom configuration task that is run when the modded configuration phase is completed.
 * @param listener The listener that is used to configure the client.
 */
@ApiStatus.Internal
public record ModdedConfigurationPhaseCompleted(ServerConfigurationPacketListenerImpl listener) implements ICustomConfigurationTask {
    private static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "modded_configuration_phase_completed");
    public static final Type TYPE = new Type(ID);

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        listener().onModdedConfigurationPhaseEnded();
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
