/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.configuration;

import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.payload.CommonVersionPayload;
import org.jetbrains.annotations.ApiStatus;

/**
 * Common Version configuration task. Initiated after registry sync to begin the c:register handshake.
 * The server will start the task, send c:version to the client, and await a reply. Upon reply, we transition to {@link CommonRegisterTask}.
 */
@ApiStatus.Internal
public record CommonVersionTask() implements ICustomConfigurationTask {
    public static final Type TYPE = new Type(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "common_version"));

    @Override
    public Type type() {
        return TYPE;
    }

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        sender.accept(new CommonVersionPayload());
    }
}
