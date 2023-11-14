/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.simple;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

public final class MessageFunctions {
    /**
     * Functional interface used to encode messages to a byte buffer.
     *
     * @param <MSG> the type of the message that is encoded
     */
    @FunctionalInterface
    public interface MessageEncoder<MSG> {
        /**
         * Encodes the message to the {@code buffer}. <br>
         * This method is usually called as soon as the message is sent, so encoding usually happens on the client/server thread,
         * not on the network thread. <br>
         * However, that should not be relied on, and the encoder should try to be thread-safe, and to not interact with the game state.
         */
        void encode(MSG message, FriendlyByteBuf buffer);
    }

    /**
     * Functional interface used to decode messages from a byte buffer.
     *
     * @param <MSG> the type of the message that is decoded
     */
    @FunctionalInterface
    public interface MessageDecoder<MSG> {
        /**
         * Decodes the message from the {@code buffer}. <br>
         * This method is called on the network thread, when the packet is received. Do <strong>not</strong> handle the message in this method.
         *
         * @return a decoded message
         */
        MSG decode(FriendlyByteBuf buffer);
    }

    /**
     * Functional interface used to handle messages.
     *
     * @param <MSG> the type of the message that is handled
     */
    @FunctionalInterface
    public interface MessageConsumer<MSG> {
        /**
         * Handles the message.
         *
         * @param msg     the message to handle
         * @param context the context containing the direction, the sender and the connection
         */
        void handle(MSG msg, NetworkEvent.Context context);

        default MessageConsumer<MSG> andThen(MessageConsumer<MSG> other) {
            return (msg, context) -> {
                this.handle(msg, context);
                other.handle(msg, context);
            };
        }
    }

    /**
     * Functional interface used to generate a list of login packets to be sent to clients.
     *
     * @param <MSG> the type of the message that is sent
     */
    @FunctionalInterface
    public interface LoginPacketGenerator<MSG> {
        List<MessageFunctions.LoginPacket<MSG>> generate(boolean isLocal);
    }

    @FunctionalInterface
    public interface LoginIndexGetter<MSG> {
        int getLoginIndex(MSG msg);
    }

    @FunctionalInterface
    public interface LoginIndexSetter<MSG> {
        void setLoginIndex(MSG msg, int index);
    }

    public record LoginPacket<MSG>(String context, MSG msg) {}
}
