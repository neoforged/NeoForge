/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.custom.payload.SimplePayload;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum PlayNetworkDirection implements INetworkDirection<PlayNetworkDirection> {
    PLAY_TO_SERVER(NetworkEvent.ClientCustomPayloadEvent::new, LogicalSide.CLIENT, ServerboundCustomPayloadPacket.class, 1, (d, i, n) -> new ServerboundCustomPayloadPacket(SimplePayload.outbound(d, i, n))),
    PLAY_TO_CLIENT(NetworkEvent.ServerCustomPayloadEvent::new, LogicalSide.SERVER, ClientboundCustomPayloadPacket.class, 0, (d, i, n) -> new ClientboundCustomPayloadPacket(SimplePayload.outbound(d, i, n)));

    private final BiFunction<ICustomPacketPayloadWithBuffer, NetworkEvent.Context, NetworkEvent> eventSupplier;
    private final LogicalSide logicalSide;
    private final Class<? extends Packet<?>> packetClass;
    private final int otherWay;
    private final Factory<?> factory;

    private static final Reference2ReferenceArrayMap<Class<? extends Packet<?>>, PlayNetworkDirection> packetLookup;

    static {
        packetLookup = Stream.of(values()).
                collect(Collectors.toMap(PlayNetworkDirection::getPacketClass, Function.identity(), (m1, m2)->m1, Reference2ReferenceArrayMap::new));
    }

    private PlayNetworkDirection(BiFunction<ICustomPacketPayloadWithBuffer, NetworkEvent.Context, NetworkEvent> eventSupplier, LogicalSide logicalSide, Class<? extends Packet<?>> clazz, int i, Factory<?> factory)
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

    public static <T extends Packet<?>> PlayNetworkDirection directionForPayload(Class<T> customPacket)
    {
        return packetLookup.get(customPacket);
    }

    @Override
    public PlayNetworkDirection reply() {
        return PlayNetworkDirection.values()[this.otherWay];
    }
    public NetworkEvent getEvent(final ICustomPacketPayloadWithBuffer buffer, final NetworkEvent.Context manager) {
        return this.eventSupplier.apply(buffer, manager);
    }

    @Override
    public LogicalSide getOriginationSide()
    {
        return logicalSide;
    }

    @Override
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
