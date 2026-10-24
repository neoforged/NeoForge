/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;

/**
 * A SessionData is a "special" attachment holder that lives for the lifetime of a game session.
 * <p>
 * On the server, it is bound to the {@link MinecraftServer}. On the client, it's bound to the {@link ClientPacketListener}.
 * It allows you to manage data that is scoped to the session (rather than a particular Level), on both the client and the server.
 * <p>
 * Both synchronized and persistent attachments are supported. If an attachment is both synchronized and persistent, it will only be serialized on the server.
 * 
 * @see {@link ILevelExtension#getSessionData()}
 */
@ApiStatus.NonExtendable
public abstract class SessionData extends AttachmentHolder {
    @ApiStatus.Internal
    protected SessionData() {}
}
