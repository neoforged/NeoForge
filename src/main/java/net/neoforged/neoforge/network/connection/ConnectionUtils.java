/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.connection;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.ApiStatus;

/**
 * Utility class for storing and retrieving {@link Connection} objects from {@link ChannelHandlerContext} objects.
 */
public class ConnectionUtils {

    private ConnectionUtils() {
        throw new IllegalStateException("Tried to create utility class!");
    }

    private static AttributeKey<Connection> ATTRIBUTE_CONNECTION = AttributeKey.valueOf("neoforge:connection");
    
    /**
     * Gets the {@link Connection} object from the {@link ChannelHandlerContext} object.
     *
     * @param connection The {@link ChannelHandlerContext} object.
     * @return The {@link Connection} object.
     */
    public static Connection getConnection(ChannelHandlerContext connection) {
        return connection.attr(ATTRIBUTE_CONNECTION).get();
    }
    
    /**
     * Sets the {@link Connection} object to the {@link ChannelHandlerContext} object.
     *
     * @param connection The {@link ChannelHandlerContext} object.
     * @param value The {@link Connection} object.
     */
    @ApiStatus.Internal
    public static void setConnection(ChannelHandlerContext connection, Connection value) {
        connection.attr(ATTRIBUTE_CONNECTION).set(value);
    }
    
    /**
     * Removes the {@link Connection} object from the {@link ChannelHandlerContext} object.
     *
     * @param connection The {@link ChannelHandlerContext} object.
     */
    @ApiStatus.Internal
    public static void removeConnection(ChannelHandlerContext connection) {
        connection.attr(ATTRIBUTE_CONNECTION).remove();
    }
}
