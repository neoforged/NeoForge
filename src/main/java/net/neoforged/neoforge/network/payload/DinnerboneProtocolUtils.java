/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import com.mojang.logging.LogUtils;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

/**
 * Protocol utilities for communicating over Dinnerbone's protocol.
 */
public final class DinnerboneProtocolUtils {
    public static final Logger LOGGER = LogUtils.getLogger();

    private DinnerboneProtocolUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final StreamCodec<FriendlyByteBuf, Set<ResourceLocation>> CHANNELS_CODEC = StreamCodec.of(
            DinnerboneProtocolUtils::writeChannels,
            DinnerboneProtocolUtils::readChannels);

    /**
     * Reads a set of channels from the buffer.
     * Each channel is a null-terminated string.
     * If a string is not a valid channel, it is ignored.
     *
     * @param buf the buffer
     * @return the channels
     */
    private static Set<ResourceLocation> readChannels(FriendlyByteBuf buf) {
        final StringBuilder builder = new StringBuilder();
        final Set<ResourceLocation> channels = new HashSet<>();

        while (buf.isReadable()) {
            final char c = (char) buf.readByte();
            if (c == '\0') {
                parseAndAddChannel(builder, channels);
            } else {
                builder.append(c);
            }
        }

        parseAndAddChannel(builder, channels);

        return channels;
    }

    private static void parseAndAddChannel(StringBuilder builder, Set<ResourceLocation> channels) {
        if (builder.isEmpty()) {
            return;
        }

        final String channel = builder.toString();
        try {
            channels.add(new ResourceLocation(channel));
        } catch (Exception e) {
            LOGGER.error("Invalid channel: {}", channel, e);
        } finally {
            builder.setLength(0);
        }
    }

    /**
     * Writes a set of channels to the buffer.
     * Each channel is a null-terminated string.
     *
     * @param buf      the buffer
     * @param channels the channels
     */
    private static void writeChannels(FriendlyByteBuf buf, Set<ResourceLocation> channels) {
        for (ResourceLocation channel : channels) {
            for (char c : channel.toString().toCharArray()) {
                buf.writeByte(c);
            }
            buf.writeByte('\0');
        }
    }
}
