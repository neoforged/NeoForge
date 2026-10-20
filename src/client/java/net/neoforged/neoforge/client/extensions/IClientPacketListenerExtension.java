/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.neoforge.attachment.SessionData;

/**
 * Extension interface for {@link ClientPacketListener}.
 */
public interface IClientPacketListenerExtension {
    /**
     * {@return the session attachments of this connection}
     * <p>
     * A new {@link SessionData} is created per connection (to any world / server / lan world).
     * 
     * @see SessionData
     */
    SessionData getSessionData();
}
