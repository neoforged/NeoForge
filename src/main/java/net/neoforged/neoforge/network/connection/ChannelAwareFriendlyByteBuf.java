/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class ChannelAwareFriendlyByteBuf extends FriendlyByteBuf {
    private final ChannelHandlerContext context;

    public ChannelAwareFriendlyByteBuf(ByteBuf p_130051_, ChannelHandlerContext context) {
        super(p_130051_);
        this.context = context;
    }

    @Nullable
    public static ChannelHandlerContext unwrapContext(ByteBuf buf) {
        while (buf != null) {
            if (buf instanceof ChannelAwareFriendlyByteBuf extraContextByteBuf) {
                return extraContextByteBuf.context;
            }

            buf = buf.unwrap();
        }

        return null;
    }
}
