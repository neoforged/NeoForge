/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.EntityItemPickupEvent;

public class GameTestPlayer extends ServerPlayer implements GameTestListener {
    private final GameTestHelper helper;

    public GameTestPlayer(MinecraftServer server, ServerLevel level, GameProfile profile, ClientInformation information, GameTestHelper helper) {
        super(server, level, profile, information);
        this.helper = helper;
    }

    public GameTestPlayer moveToCorner() {
        moveTo(helper.absoluteVec(new BlockPos(0, helper.testInfo.getStructureName().endsWith("_floor") ? 2 : 1, 0).getCenter()).subtract(0, 0.5, 0));
        return this;
    }

    public GameTestPlayer moveToCentre() {
        moveTo(helper.absoluteVec(new BlockPos(helper.testInfo.getStructureSize().getX() / 2, helper.testInfo.getStructureName().endsWith("_floor") ? 2 : 1, helper.testInfo.getStructureSize().getX() / 2).getCenter()).subtract(0, 0.5, 0));
        return this;
    }

    public GameTestPlayer preventItemPickup() {
        subscribe((final EntityItemPickupEvent event) -> {
            if (event.getEntity() == this) event.setCanceled(true);
        });
        return this;
    }

    @Override
    public void testStructureLoaded(GameTestInfo i) {

    }

    @Override
    public void testPassed(GameTestInfo i) {
        disconnectGameTest();
    }

    @Override
    public void testFailed(GameTestInfo i) {
        disconnectGameTest();
    }

    private final List<Consumer<? extends Event>> listeners = new ArrayList<>();

    public void subscribe(Consumer<? extends Event> listener) {
        this.listeners.add(listener);
        NeoForge.EVENT_BUS.addListener(listener);
    }

    private void disconnectGameTest() {
        connection.disconnect(Component.literal("Test finished"));
        this.listeners.forEach(NeoForge.EVENT_BUS::unregister);
        this.listeners.clear();
    }

    @SuppressWarnings("unchecked")
    private Stream<Packet<? extends ClientCommonPacketListener>> outboundPackets() {
        return ((EmbeddedChannel) connection.connection.channel()).outboundMessages().stream()
                .filter(Packet.class::isInstance).map(obj -> (Packet<? extends ClientCommonPacketListener>) obj)
                .flatMap((Function<Packet<? extends ClientCommonPacketListener>, Stream<? extends Packet<? extends ClientCommonPacketListener>>>) packet -> {
                    if (!(packet instanceof ClientboundBundlePacket clientboundBundlePacket)) return Stream.of(packet);

                    return StreamSupport.stream(clientboundBundlePacket.subPackets().spliterator(), false)
                            .map(obj -> (Packet<? extends ClientCommonPacketListener>) obj);
                });
    }

    public <T extends Packet<? extends ClientCommonPacketListener>> Stream<T> getOutboundPackets(Class<T> type) {
        return outboundPackets().filter(type::isInstance).map(type::cast);
    }

    public <T extends CustomPacketPayload> Stream<T> getOutboundPayloads(Class<T> type) {
        return getOutboundPackets(ClientboundCustomPayloadPacket.class)
                .map(ClientboundCustomPayloadPacket::payload)
                .filter(type::isInstance)
                .map(type::cast);
    }
}
