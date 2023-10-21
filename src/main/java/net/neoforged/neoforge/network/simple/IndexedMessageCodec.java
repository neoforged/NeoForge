/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.simple;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.HandshakeHandler;
import net.neoforged.neoforge.network.INetworkDirection;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.NetworkHooks;
import net.neoforged.neoforge.network.NetworkInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.Objects;
import java.util.Optional;

public class IndexedMessageCodec
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker SIMPLENET = MarkerManager.getMarker("SIMPLENET");
    private final Short2ObjectArrayMap<MessageHandler<?>> indicies = new Short2ObjectArrayMap<>();
    private final Object2ObjectArrayMap<Class<?>, MessageHandler<?>> types = new Object2ObjectArrayMap<>();
    private final NetworkInstance networkInstance;

    public IndexedMessageCodec() {
        this(null);
    }
    public IndexedMessageCodec(final NetworkInstance instance) {
        this.networkInstance = instance;
    }

    @SuppressWarnings("unchecked")
    public <MSG> MessageHandler<MSG> findMessageType(final MSG msgToReply) {
        return (MessageHandler<MSG>) types.get(msgToReply.getClass());
    }

    @SuppressWarnings("unchecked")
    <MSG> MessageHandler<MSG> findIndex(final short i) {
        return (MessageHandler<MSG>) indicies.get(i);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    class MessageHandler<MSG>
    {
        private final Optional<MessageFunctions.MessageEncoder<MSG>> encoder;
        private final Optional<MessageFunctions.MessageDecoder<MSG>> decoder;
        private final int index;
        private final MessageFunctions.MessageConsumer<MSG> messageConsumer;
        private final Class<MSG> messageType;
        private final Optional<INetworkDirection<?>> networkDirection;
        private Optional<MessageFunctions.LoginIndexSetter<MSG>> loginIndexSetter;
        private Optional<MessageFunctions.LoginIndexGetter<MSG>> loginIndexGetter;

        public MessageHandler(int index, Class<MSG> messageType, MessageFunctions.MessageEncoder<MSG> encoder, MessageFunctions.MessageDecoder<MSG> decoder, MessageFunctions.MessageConsumer<MSG> messageConsumer, final Optional<INetworkDirection<?>> networkDirection)
        {
            this.index = index;
            this.messageType = messageType;
            this.encoder = Optional.ofNullable(encoder);
            this.decoder = Optional.ofNullable(decoder);
            this.messageConsumer = messageConsumer;
            this.networkDirection = networkDirection;
            this.loginIndexGetter = Optional.empty();
            this.loginIndexSetter = Optional.empty();
            indicies.put((short)(index & 0xff), this);
            types.put(messageType, this);
        }

        void setLoginIndexSetter(MessageFunctions.LoginIndexSetter<MSG> loginIndexSetter)
        {
            this.loginIndexSetter = Optional.of(loginIndexSetter);
        }

        Optional<MessageFunctions.LoginIndexSetter<MSG>> getLoginIndexSetter() {
            return this.loginIndexSetter;
        }

        void setLoginIndexGetter(MessageFunctions.LoginIndexGetter<MSG> loginIndexGetter) {
            this.loginIndexGetter = Optional.of(loginIndexGetter);
        }

        public Optional<MessageFunctions.LoginIndexGetter<MSG>> getLoginIndexGetter() {
            return this.loginIndexGetter;
        }

        MSG newInstance() {
            try {
                return messageType.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                LOGGER.error("Invalid login message", e);
                throw new RuntimeException(e);
            }
        }
    }

    private static <M> void tryDecode(FriendlyByteBuf payload, NetworkEvent.Context context, int payloadIndex, MessageHandler<M> codec)
    {
        codec.decoder.map(d->d.decode(payload)).
                map(p->{
                    // Only run the loginIndex function for payloadIndexed packets (login)
                    if (payloadIndex != Integer.MIN_VALUE)
                    {
                        codec.getLoginIndexSetter().ifPresent(f-> f.setLoginIndex(p, payloadIndex));
                    }
                    return p;
                }).ifPresent(m -> codec.messageConsumer.handle(m, context));
    }

    private static <M> int tryEncode(FriendlyByteBuf target, M message, MessageHandler<M> codec) {
        codec.encoder.ifPresent(encoder->{
            target.writeByte(codec.index & 0xff);
            encoder.encode(message, target);
        });
        return codec.loginIndexGetter.orElse(m -> Integer.MIN_VALUE).getLoginIndex(message);
    }

    public <MSG> int build(MSG message, FriendlyByteBuf target)
    {
        @SuppressWarnings("unchecked")
        MessageHandler<MSG> messageHandler = (MessageHandler<MSG>)types.get(message.getClass());
        if (messageHandler == null) {
            LOGGER.error(SIMPLENET, "Received invalid message {} on channel {}", message.getClass().getName(), Optional.ofNullable(networkInstance).map(NetworkInstance::getChannelName).map(Objects::toString).orElse("MISSING CHANNEL"));
            throw new IllegalArgumentException("Invalid message "+message.getClass().getName());
        }
        return tryEncode(target, message, messageHandler);
    }

    void consume(FriendlyByteBuf payload, int payloadIndex, NetworkEvent.Context context) {
        if (payload == null || !payload.isReadable()) {
            LOGGER.error(SIMPLENET, "Received empty payload on channel {}", Optional.ofNullable(networkInstance).map(NetworkInstance::getChannelName).map(Objects::toString).orElse("MISSING CHANNEL"));
            if (!HandshakeHandler.packetNeedsResponse(context.getNetworkManager(), payloadIndex))
            {
                context.setPacketHandled(true); //don't disconnect if the corresponding S2C packet that was not recognized on the client doesn't require a proper response
            }
            return;
        }
        short discriminator = payload.readUnsignedByte();
        final MessageHandler<?> messageHandler = indicies.get(discriminator);
        if (messageHandler == null) {
            LOGGER.error(SIMPLENET, "Received invalid discriminator byte {} on channel {}", discriminator, Optional.ofNullable(networkInstance).map(NetworkInstance::getChannelName).map(Objects::toString).orElse("MISSING CHANNEL"));
            return;
        }
        NetworkHooks.validatePacketDirection(context.getDirection(), messageHandler.networkDirection, context.getNetworkManager());
        tryDecode(payload, context, payloadIndex, messageHandler);
    }

    <MSG> MessageHandler<MSG> addCodecIndex(int index, Class<MSG> messageType, MessageFunctions.MessageEncoder<MSG> encoder, MessageFunctions.MessageDecoder<MSG> decoder, MessageFunctions.MessageConsumer<MSG> messageConsumer, final Optional<INetworkDirection<?>> networkDirection) {
        return new MessageHandler<>(index, messageType, encoder, decoder, messageConsumer, networkDirection);
    }
}
