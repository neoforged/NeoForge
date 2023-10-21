/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.simple;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.neoforged.neoforge.network.INetworkDirection;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.NetworkInstance;
import net.neoforged.neoforge.network.PacketDistributor;

import net.neoforged.neoforge.network.simple.MessageFunctions.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SimpleChannel
{
    private final NetworkInstance instance;
    private final IndexedMessageCodec indexedCodec;
    private final Optional<Consumer<NetworkEvent.ChannelRegistrationChangeEvent>> registryChangeConsumer;
    private record LoginPacketEntry(LoginPacketGenerator<?> generator, boolean needsResponse) {}
    private final List<LoginPacketEntry> loginPackets = new ArrayList<>();

    public SimpleChannel(NetworkInstance instance) {
        this(instance, Optional.empty());
    }

    private SimpleChannel(NetworkInstance instance, Optional<Consumer<NetworkEvent.ChannelRegistrationChangeEvent>> registryChangeNotify) {
        this.instance = instance;
        this.indexedCodec = new IndexedMessageCodec(instance);
        instance.addListener(this::networkEventListener);
        instance.addGatherListener(this::networkLoginGather);
        this.registryChangeConsumer = registryChangeNotify;
    }

    public SimpleChannel(NetworkInstance instance, Consumer<NetworkEvent.ChannelRegistrationChangeEvent> registryChangeNotify) {
        this(instance, Optional.of(registryChangeNotify));
    }

    private void networkLoginGather(final NetworkEvent.GatherLoginPayloadsEvent gatherEvent) {
        loginPackets.forEach(packetGenerator-> packetGenerator.generator.generate(gatherEvent.isLocal()).forEach(p->{
            FriendlyByteBuf pb = new FriendlyByteBuf(Unpooled.buffer());
            this.indexedCodec.build(p.msg(), pb);
            gatherEvent.add(pb, this.instance.getChannelName(), p.context(), packetGenerator.needsResponse);
        }));
    }
    private void networkEventListener(final NetworkEvent networkEvent)
    {
        if (networkEvent instanceof NetworkEvent.ChannelRegistrationChangeEvent) {
            this.registryChangeConsumer.ifPresent(l->l.accept(((NetworkEvent.ChannelRegistrationChangeEvent) networkEvent)));
        } else {
            this.indexedCodec.consume(networkEvent.getPayload(), networkEvent.getLoginIndex(), networkEvent.getSource());
        }
    }

    public <MSG> int encodeMessage(MSG message, final FriendlyByteBuf target) {
        return this.indexedCodec.build(message, target);
    }

    public <MSG> IndexedMessageCodec.MessageHandler<MSG> registerMessage(int index, Class<MSG> messageType, MessageFunctions.MessageEncoder<MSG> encoder, MessageFunctions.MessageDecoder<MSG> decoder, MessageFunctions.MessageConsumer<MSG> messageConsumer) {
        return registerMessage(index, messageType, encoder, decoder, messageConsumer, Optional.empty());
    }

    public <MSG> IndexedMessageCodec.MessageHandler<MSG> registerMessage(int index, Class<MSG> messageType, MessageFunctions.MessageEncoder<MSG> encoder, MessageFunctions.MessageDecoder<MSG> decoder, MessageFunctions.MessageConsumer<MSG> messageConsumer, final Optional<INetworkDirection<?>> networkDirection) {
        return this.indexedCodec.addCodecIndex(index, messageType, encoder, decoder, messageConsumer, networkDirection);
    }

    private <MSG> INetworkDirection.PacketData toBuffer(MSG msg) {
        final FriendlyByteBuf bufIn = new FriendlyByteBuf(Unpooled.buffer());
        int index = encodeMessage(msg, bufIn);
        return new INetworkDirection.PacketData(bufIn, index);
    }

    public <MSG> void sendToServer(MSG message)
    {
        sendTo(message, Minecraft.getInstance().getConnection().getConnection(), PlayNetworkDirection.PLAY_TO_SERVER);
    }

    public <MSG> void sendTo(MSG message, Connection manager, PlayNetworkDirection direction)
    {
        manager.send(toVanillaPacket(message, direction));
    }

    /**
     * Send a message to the {@link PacketDistributor.PacketTarget} from a {@link PacketDistributor} instance.
     *
     * <pre>
     *     channel.send(PacketDistributor.PLAYER.with(()->player), message)
     * </pre>
     *
     * @param target The curried target from a PacketDistributor
     * @param message The message to send
     * @param <MSG> The type of the message
     */
    public <MSG> void send(PacketDistributor.PacketTarget target, MSG message) {
        target.send(toVanillaPacket(message, target.getDirection()));
    }

    public <MSG> Packet<?> toVanillaPacket(MSG message, PlayNetworkDirection direction)
    {
        return direction.buildPacket(toBuffer(message), instance.getChannelName());
    }

    public <MSG> void reply(MSG msgToReply, NetworkEvent.Context context)
    {
        context.getPacketDispatcher().sendPacket(instance.getChannelName(), toBuffer(msgToReply).buffer());
    }

    /**
     * {@return {@code true} if the channel is present in the given connection}
     */
    public boolean isRemotePresent(Connection manager) {
        return instance.isRemotePresent(manager);
    }

    /**
     * Build a new MessageBuilder. The type should implement {@link java.util.function.IntSupplier} if it is a login
     * packet.
     * @param type Type of message
     * @param id id in the indexed codec
     * @param <M> Type of type
     * @return a MessageBuilder
     */
    public <M> MessageBuilder<M> messageBuilder(final Class<M> type, int id) {
        return MessageBuilder.forType(this, type, id, null);
    }

    /**
     * Build a new MessageBuilder. The type should implement {@link java.util.function.IntSupplier} if it is a login
     * packet.
     * @param type Type of message
     * @param id id in the indexed codec
     * @param direction a impl direction which will be asserted before any processing of this message occurs. Use to
     *                  enforce strict sided handling to prevent spoofing.
     * @param <M> Type of type
     * @return a MessageBuilder
     */
    public <M> MessageBuilder<M> messageBuilder(final Class<M> type, int id, INetworkDirection<?> direction) {
        return MessageBuilder.forType(this, type, id, direction);
    }

    /**
     * Build a new MessageBuilder for a message that implements {@link SimpleMessage}. This will automatically set the
     * {@link MessageBuilder#encoder(MessageEncoder) encoder} and the handler.
     * @param type the type of message
     * @param id the id in the indexed codec
     * @param <M> the type of the message
     * @return a MessageBuilder
     */
    public <M extends SimpleMessage> MessageBuilder<M> simpleMessageBuilder(final Class<M> type, int id) {
        return simpleMessageBuilder(type, id, null);
    }

    /**
     * Build a new MessageBuilder for a message that implements {@link SimpleMessage}. This will automatically set the
     * {@link MessageBuilder#encoder(MessageEncoder) encoder} and the handler.
     * @param type the type of message
     * @param id the id in the indexed codec
     * @param direction a impl direction which will be asserted before any processing of this message occurs. Use to
     *                  enforce strict sided handling to prevent spoofing.
     * @param <M> the type of the message
     * @return a MessageBuilder
     */
    public <M extends SimpleMessage> MessageBuilder<M> simpleMessageBuilder(final Class<M> type, int id, INetworkDirection<?> direction) {
        return messageBuilder(type, id, direction)
                .consumerNetworkThread((msg, context) -> {
                    msg.handleNetworkThread(context);
                    context.enqueueWork(() -> msg.handleMainThread(context));
                })
                .encoder(SimpleMessage::encode);
    }

    /**
     * Build a new MessageBuilder for a login message that implements {@link SimpleLoginMessage}. This will automatically set the
     * {@link MessageBuilder#encoder(MessageEncoder) encoder}, the handler and the {@link MessageBuilder#loginIndex(LoginIndexGetter, LoginIndexSetter) login index setter and getter}.
     * @param type the type of message
     * @param id the id in the indexed codec
     * @param <M> the type of the message
     * @return a MessageBuilder
     */
    public <M extends SimpleLoginMessage> MessageBuilder<M> simpleLoginMessageBuilder(final Class<M> type, int id) {
        return simpleLoginMessageBuilder(type, id, null);
    }

    /**
     * Build a new MessageBuilder for a login message that implements {@link SimpleLoginMessage}. This will automatically set the
     * {@link MessageBuilder#encoder(MessageEncoder) encoder}, the handler and the {@link MessageBuilder#loginIndex(LoginIndexGetter, LoginIndexSetter) login index setter and getter}.
     * @param type the type of message
     * @param id the id in the indexed codec
     * @param direction a impl direction which will be asserted before any processing of this message occurs. Use to
     *                  enforce strict sided handling to prevent spoofing.
     * @param <M> the type of the message
     * @return a MessageBuilder
     */
    public <M extends SimpleLoginMessage> MessageBuilder<M> simpleLoginMessageBuilder(final Class<M> type, int id, INetworkDirection<?> direction) {
        return simpleMessageBuilder(type, id, direction)
                .loginIndex(SimpleLoginMessage::getLoginIndex, SimpleLoginMessage::setLoginIndex);
    }

    public static class MessageBuilder<MSG>  {
        private SimpleChannel channel;
        private Class<MSG> type;
        private int id;
        private MessageEncoder<MSG> encoder;
        private MessageDecoder<MSG> decoder;
        private MessageConsumer<MSG> consumer;
        private LoginIndexGetter<MSG> loginIndexGetter;
        private LoginIndexSetter<MSG> loginIndexSetter;
        private LoginPacketGenerator<MSG> loginPacketGenerators;
        private Optional<INetworkDirection<?>> networkDirection;
        private boolean needsResponse = true;

        private static <MSG> MessageBuilder<MSG> forType(final SimpleChannel channel, final Class<MSG> type, int id, INetworkDirection<?> playNetworkDirection) {
            MessageBuilder<MSG> builder = new MessageBuilder<>();
            builder.channel = channel;
            builder.id = id;
            builder.type = type;
            builder.networkDirection = Optional.ofNullable(playNetworkDirection);
            return builder;
        }

        /**
         * Set the message encoder, which writes this message to a {@link FriendlyByteBuf}.
         * <p>
         * The encoder is called <em>immediately</em> {@linkplain #send(PacketDistributor.PacketTarget, Object) when the
         * packet is sent}. This means encoding typically occurs on the main server/client thread rather than on the
         * network thread.
         * <p>
         * However, this behaviour should not be relied on, and the encoder should try to be thread-safe and not
         * interact with the current game state.
         *
         * @param encoder The message encoder.
         * @return This message builder, for chaining.
         */
        public MessageBuilder<MSG> encoder(MessageEncoder<MSG> encoder) {
            this.encoder = encoder;
            return this;
        }

        /**
         * Set the message decoder, which reads the message from a {@link FriendlyByteBuf}.
         * <p>
         * The decoder is called when the message is received on the network thread. The decoder should not attempt to
         * access or mutate any game state, deferring that until the {@linkplain #consumerMainThread(MessageConsumer)} the
         * message is handled}.
         *
         * @param decoder The message decoder.
         * @return The message builder, for chaining.
         */
        public MessageBuilder<MSG> decoder(MessageDecoder<MSG> decoder) {
            this.decoder = decoder;
            return this;
        }

        public MessageBuilder<MSG> loginIndex(LoginIndexGetter<MSG> loginIndexGetter, LoginIndexSetter<MSG> loginIndexSetter) {
            this.loginIndexGetter = loginIndexGetter;
            this.loginIndexSetter = loginIndexSetter;
            return this;
        }

        public MessageBuilder<MSG> buildLoginPacketList(LoginPacketGenerator<MSG> loginPacketGenerators) {
            this.loginPacketGenerators = loginPacketGenerators;
            return this;
        }

        public MessageBuilder<MSG> markAsLoginPacket() {
            this.loginPacketGenerators = (isLocal) -> {
                try {
                    return Collections.singletonList(new LoginPacket<>(type.getName(), type.getConstructor().newInstance()));
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException("Inaccessible no-arg constructor for message "+type.getName(), e);
                }
            };
            return this;
        }

        /**
         * Marks this packet as not needing a response when sent to the client
         */
        public MessageBuilder<MSG> noResponse() {
            this.needsResponse = false;
            return this;
        }

        /**
         * Set the message consumer, which is called once a message has been decoded. This accepts the decoded message
         * object and the message's context.
         * <p>
         * The consumer is called on the network thread, and so should not interact with most game state by default.
         * {@link NetworkEvent.Context#enqueueWork(Runnable)} can be used to handle the message on the main server or
         * client thread. Alternatively one can use {@link #consumerMainThread(MessageConsumer)} to run the handler on the
         * main thread.
         * <p>
         * The packet is marked as {@link NetworkEvent.Context#setPacketHandled(boolean) handled}. You may manually revert that state if you wish to, but generally
         * you do not want to, as a packet that is not handled will continue to try to find handlers.
         *
         * @param consumer The message consumer.
         * @return The message builder, for chaining.
         * @see #consumerMainThread(MessageConsumer) 
         */
        public MessageBuilder<MSG> consumerNetworkThread(MessageConsumer<MSG> consumer) {
            this.consumer = consumer;
            return this;
        }

        /**
         * Set the message consumer, which is called once a message has been decoded. This accepts the decoded message
         * object and the message's context.
         * <p>
         * Unlike {@link #consumerNetworkThread(MessageConsumer)}, the consumer is called on the main thread, and so can
         * interact with most game state by default.
         * <p>
         * The packet is marked as {@link NetworkEvent.Context#setPacketHandled(boolean) handled}. You may manually revert that state if you wish to, but generally
         * you do not want to, as a packet that is not handled will continue to try to find handlers.
         *
         * @param consumer The message consumer.
         * @return The message builder, for chaining.
         * @see #consumerNetworkThread(MessageConsumer) 
         */
        public MessageBuilder<MSG> consumerMainThread(MessageConsumer<MSG> consumer) {
            this.consumer = (msg, context) -> context.enqueueWork(() -> consumer.handle(msg, context));
            return this;
        }

        public void add() {
            Objects.requireNonNull(this.consumer, () -> "Message of type " + this.type.getName() + " is missing a handler!");

            final IndexedMessageCodec.MessageHandler<MSG> message = this.channel.registerMessage(this.id, this.type, this.encoder, this.decoder, (msg, context) -> {
                context.setPacketHandled(true); // Mark as handled by default, people can mark it as not handled manually
                this.consumer.handle(msg, context);
            }, this.networkDirection);
            if (this.loginIndexSetter != null) {
                message.setLoginIndexSetter(this.loginIndexSetter);
            }
            if (this.loginIndexGetter != null) {
                if (!IntSupplier.class.isAssignableFrom(this.type)) {
                    throw new IllegalArgumentException("Login packet type that does not supply an index as an IntSupplier");
                }
                message.setLoginIndexGetter(this.loginIndexGetter);
            }
            if (this.loginPacketGenerators != null) {
                this.channel.loginPackets.add(new LoginPacketEntry(this.loginPacketGenerators, this.needsResponse));
            }
        }
    }
}
