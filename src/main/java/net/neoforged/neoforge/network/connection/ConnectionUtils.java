/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.connection;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import net.minecraft.network.Connection;

public class ConnectionUtils {

    private ConnectionUtils() {
        throw new IllegalStateException("Tried to create utility class!");
    }

    private static AttributeKey<Connection> ATTRIBUTE_CONNECTION = AttributeKey.valueOf("neoforge:connection");

    public static Connection getConnection(ChannelHandlerContext connection) {
        return connection.attr(ATTRIBUTE_CONNECTION).get();
    }

    public static void setConnection(ChannelHandlerContext connection, Connection value) {
        connection.attr(ATTRIBUTE_CONNECTION).set(value);
    }

    public static void removeConnection(ChannelHandlerContext connection) {
        connection.attr(ATTRIBUTE_CONNECTION).remove();
    }
}
