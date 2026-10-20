/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.attachment.SessionData;

/**
 * Extension interface for {@link MinecraftServer}.
 */
public interface IMinecraftServerExtension {
    /**
     * {@return the session attachments of this server}
     * <p>
     * Available as soon as the server is constructed.
     *
     * @see SessionData
     */
    SessionData getSessionData();
}
