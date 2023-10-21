/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.custom.payload.SimpleQueryPayload;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum LoginNetworkDirection implements INetworkDirection<LoginNetworkDirection>
{
    LOGIN_TO_SERVER(NetworkEvent.ClientCustomPayloadLoginEvent::new, LogicalSide.CLIENT, ServerboundCustomQueryAnswerPacket.class, 1, (d, i, n) -> new ServerboundCustomQueryAnswerPacket(i, SimpleQueryPayload.outbound(d, i, n))),
    LOGIN_TO_CLIENT(NetworkEvent.ServerCustomPayloadLoginEvent::new, LogicalSide.SERVER, ClientboundCustomQueryPacket.class, 0, (d, i, n) -> new ClientboundCustomQueryPacket(i, SimpleQueryPayload.outbound(d, i, n)));

    private final BiFunction<IForgeCustomQueryPayload, NetworkEvent.Context, NetworkEvent> eventSupplier;
    private final LogicalSide logicalSide;
    private final Class<? extends Packet<?>> packetClass;
    private final int otherWay;
    private final Factory<?> factory;

    private static final Reference2ReferenceArrayMap<Class<? extends Packet<?>>, LoginNetworkDirection> PACKET_LOOKUP = Stream.of(values()).
            collect(Collectors.toMap(LoginNetworkDirection::getPacketClass, Function.identity(), (m1, m2)->m1, Reference2ReferenceArrayMap::new));

    private LoginNetworkDirection(BiFunction<IForgeCustomQueryPayload, NetworkEvent.Context, NetworkEvent> eventSupplier, LogicalSide logicalSide, Class<? extends Packet<?>> clazz, int i, Factory<?> factory)
    {
        this.eventSupplier = eventSupplier;
        this.logicalSide = logicalSide;
        this.packetClass = clazz;
        this.otherWay = i;
        this.factory = factory;
    }

    private Class<? extends Packet<?>> getPacketClass() {
        return packetClass;
    }

    public static <T extends Packet<?>> LoginNetworkDirection directionForPayload(Class<T> customPacket)
    {
        return PACKET_LOOKUP.get(customPacket);
    }

    public LoginNetworkDirection reply() {
        return LoginNetworkDirection.values()[this.otherWay];
    }
    public NetworkEvent getEvent(final IForgeCustomQueryPayload buffer, final NetworkEvent.Context context) {
        return this.eventSupplier.apply(buffer, context);
    }

    public LogicalSide getOriginationSide()
    {
        return logicalSide;
    }

    public LogicalSide getReceptionSide() { return reply().logicalSide; };

    @Override
    public Packet<?> buildPacket(PacketData packetData, ResourceLocation channelName)
    {
        return this.factory.create(packetData.buffer(), packetData.index(), channelName);
    }

    private interface Factory<T extends Packet<?>> {
        T create(FriendlyByteBuf data, int index, ResourceLocation channelName);
    }
}
