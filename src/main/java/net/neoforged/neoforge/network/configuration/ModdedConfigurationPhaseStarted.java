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
import org.jetbrains.annotations.NotNull;

/**
 * Custom configuration task that is run to indicate that the modded configuration phase has started.
 * 
 * @param listener The listener that is handling the configuration.
 */
@ApiStatus.Internal
public record ModdedConfigurationPhaseStarted(ServerConfigurationPacketListenerImpl listener) implements ICustomConfigurationTask {
    private static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "modded_configuration_phase_started");
    public static final Type TYPE = new Type(ID);

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        listener().onModdedConfigurationPhaseStarted();
    }

    @Override
    public @NotNull Type type() {
        return TYPE;
    }
}
