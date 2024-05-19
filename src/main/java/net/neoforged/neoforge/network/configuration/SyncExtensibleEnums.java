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
import net.neoforged.neoforge.network.payload.ExtensibleEnumDataPayload;
import org.jetbrains.annotations.ApiStatus;

/**
 * Syncs extensible Enums and verifies that they match
 */
@ApiStatus.Internal
public record SyncExtensibleEnums(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
    private static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "sync_extensible_enums");
    public static final Type TYPE = new Type(ID);

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        if (listener.hasChannel(ExtensibleEnumDataPayload.TYPE)) {
            sender.accept(ExtensibleEnumDataPayload.getOrCreateInstance());
        }
        listener.finishCurrentTask(TYPE);
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
