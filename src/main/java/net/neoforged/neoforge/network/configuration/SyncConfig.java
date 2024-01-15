/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.configuration;

import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.ConfigSync;
import org.jetbrains.annotations.ApiStatus;

/**
 * Configuration task that syncs the config files to the client
 * 
 * @param listener the listener to indicate to that the task is complete
 */
@ApiStatus.Internal
public record SyncConfig(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
    private static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "sync_config");
    public static Type TYPE = new Type(ID);

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        ConfigSync.INSTANCE.syncConfigs().forEach(sender);
        listener().finishCurrentTask(type());
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
