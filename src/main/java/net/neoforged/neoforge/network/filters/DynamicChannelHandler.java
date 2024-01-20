/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.filters;

import io.netty.channel.ChannelHandler;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.ApiStatus;

/**
 * An extension to the netty {@link ChannelHandler} interface that allows for
 * dynamic injection of handlers into the pipeline, based on whether they are needed
 * on the current connection or not.
 */
@ApiStatus.Internal
public interface DynamicChannelHandler extends ChannelHandler {
    boolean isNecessary(Connection manager);
}
