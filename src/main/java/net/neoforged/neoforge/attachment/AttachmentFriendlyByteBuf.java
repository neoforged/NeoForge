/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.connection.ConnectionType;

public class AttachmentFriendlyByteBuf<THolder extends IAttachmentHolder<?>> extends RegistryFriendlyByteBuf {
    final THolder attachmentHolder;

    public AttachmentFriendlyByteBuf(ByteBuf buffer, RegistryAccess registryAccess, THolder attachmentHolder) {
        super(buffer, registryAccess, ConnectionType.NEOFORGE);
        this.attachmentHolder = attachmentHolder;
    }
}
