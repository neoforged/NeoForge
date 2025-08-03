/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.attachment;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentSync;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.SyncAttachmentsPayload;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.registration.RegistrationHelper;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

@ForEachTest(groups = "attachment.sync")
public class AttachmentSyncTests {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @TestHolder(description = "Tests that attachment values properly sync to clients")
    static void testAttachmentSyncManual(DynamicTest test, RegistrationHelper reg) {
        var attachment = reg.attachments().register("value", () -> AttachmentType.builder(() -> 0)
                .serialize(Codec.INT.fieldOf("value")).sync(ByteBufCodecs.VAR_INT).build());

        var packetType = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath(reg.modId(), "expect_attachment"));

        class ExpectAttachmentValuePayload implements CustomPacketPayload {
            private final int value;

            private ExpectAttachmentValuePayload(int value) {
                this.value = value;
            }

            @Override
            public Type<? extends CustomPacketPayload> type() {
                return packetType;
            }

            void execute(IPayloadContext context) {
                var found = context.player().getData(attachment);
                if (found == value) {
                    test.pass();
                } else {
                    test.fail("Synced attachment of player " + context.player() + " expected to have value " + value + ", but found " + found);
                }
            }
        }

        test.framework().modEventBus().addListener((final RegisterPayloadHandlersEvent event) -> event.registrar("1")
                .playToClient(
                        packetType,
                        ByteBufCodecs.VAR_INT.map(ExpectAttachmentValuePayload::new, e -> e.value),
                        ExpectAttachmentValuePayload::execute));

        test.whenEnabled(listeners -> {
            var value = new Random().nextInt(Integer.MAX_VALUE);
            listeners.forge().addListener((final PlayerTickEvent.Post tickEvent) -> {
                if (tickEvent.getEntity() instanceof ServerPlayer sp && sp.getData(attachment) != value) {
                    sp.setData(attachment, value);
                    PacketDistributor.sendToPlayer(sp, new ExpectAttachmentValuePayload(value));
                }
            });
        });
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @SuppressWarnings("DuplicatedCode")
    @TestHolder(description = "Gametest that tests if attachments sync properly in different scenarios")
    static void testAttachmentSync(DynamicTest test, RegistrationHelper reg) {
        var blacklistedPlayer = reg.attachments().register("sync_blacklist", () -> AttachmentType.builder(() -> false)
                .serialize(Codec.BOOL.fieldOf("value")).build());
        var intAttachment = reg.attachments().register("int", () -> AttachmentType.builder(() -> 0)
                .serialize(Codec.INT.fieldOf("value")).sync(ByteBufCodecs.VAR_INT).build());
        var mutableIntAttachment = reg.attachments().register("mutable_int", () -> AttachmentType.builder(() -> new MutableInt(23))
                .serialize(Codec.INT.fieldOf("value").xmap(MutableInt::new, MutableInt::getValue))
                .sync((h, p) -> !Boolean.TRUE.equals(p.getExistingDataOrNull(blacklistedPlayer)), ByteBufCodecs.VAR_INT.map(MutableInt::new, MutableInt::getValue)).build());

        class TestHelper extends ExtendedGameTestHelper {
            public TestHelper(GameTestInfo info) {
                super(info);
            }

            public int randomInt() {
                return new Random().nextInt(Integer.MAX_VALUE);
            }

            public void expectTarget(SyncAttachmentsPayload payload, SyncAttachmentsPayload.Target target) {
                assertValueEqual(
                        target,
                        payload.target(),
                        "attachment payload target");
            }

            public Holder holder() {
                return new Holder();
            }

            class Holder extends AttachmentHolder {
                public void readFrom(SyncAttachmentsPayload payload) {
                    AttachmentSync.receiveSyncedDataAttachments(
                            this,
                            getLevel().registryAccess(),
                            payload.types(),
                            payload.syncPayload());
                }

                public <T> void assertEqual(Supplier<AttachmentType<T>> type, @Nullable T value) {
                    if (value == null) {
                        assertFalse(this.hasData(type), "Has data for attachment " + type.get());
                    } else {
                        assertValueEqual(value, getData(type), "attachment value of type " + type.get());
                    }
                }
            }
        }

        test.onGameTest(TestHelper.class, helper -> {
            var player = helper.makeTickingMockServerPlayerInCorner(GameType.CREATIVE);
            var feetPos = helper.relativePos(player.blockPosition()).below(1);

            player.clearOutboundPackets();

            helper.startSequence()
                    // Test that players receive updates for changes to their own data
                    .thenExecute(() -> {
                        var testValue = helper.randomInt();
                        player.setData(intAttachment, testValue);

                        var payload = player.requireOutboundPayload(SyncAttachmentsPayload.class);
                        helper.expectTarget(payload, new SyncAttachmentsPayload.EntityTarget(player.getId()));

                        var holder = helper.holder();
                        holder.readFrom(payload);
                        holder.assertEqual(intAttachment, testValue);

                        player.clearOutboundPackets();
                    })
                    // Test that players receive updates for changes to the chunk they're in
                    .thenExecute(() -> {
                        var chunk = helper.getLevel().getChunkAt(player.blockPosition());
                        var testValue = helper.randomInt();
                        chunk.setData(intAttachment, testValue);

                        var payload = player.requireOutboundPayload(SyncAttachmentsPayload.class);
                        helper.expectTarget(payload, new SyncAttachmentsPayload.ChunkTarget(player.chunkPosition()));

                        var holder = helper.holder();
                        holder.readFrom(payload);
                        holder.assertEqual(intAttachment, testValue);

                        player.clearOutboundPackets();
                    })
                    // Test that players receive updates for changes to block entities in tracked chunks
                    .thenExecute(() -> {
                        var testValue = helper.randomInt();
                        helper.setBlock(feetPos, Blocks.FURNACE);
                        var be = helper.getBlockEntity(feetPos, FurnaceBlockEntity.class);
                        be.setData(intAttachment, testValue);

                        var payload = player.requireOutboundPayload(SyncAttachmentsPayload.class);
                        helper.expectTarget(payload, new SyncAttachmentsPayload.BlockEntityTarget(helper.absolutePos(feetPos)));

                        var holder = helper.holder();
                        holder.readFrom(payload);
                        holder.assertEqual(intAttachment, testValue);

                        player.clearOutboundPackets();
                    })
                    .thenMap(() -> helper.spawnWithNoFreeWill(EntityType.PIG, helper.relativePos(player.blockPosition())))
                    // Test that players receive updates for entities in tracked chunks
                    .thenExecute(entity -> {
                        var testValue = helper.randomInt();
                        entity.setData(intAttachment, testValue);

                        var payload = player.requireOutboundPayload(SyncAttachmentsPayload.class);
                        helper.expectTarget(payload, new SyncAttachmentsPayload.EntityTarget(entity.getId()));

                        var holder = helper.holder();
                        holder.readFrom(payload);
                        holder.assertEqual(intAttachment, testValue);

                        player.clearOutboundPackets();
                    })
                    // Test that players receive initial login packets
                    .thenMap(entity -> Pair.of(entity, helper.makeTickingMockServerPlayerInCorner(GameType.CREATIVE)))
                    // Wait 1 tick to let the player tick so that it starts tracking the other entities
                    .thenIdle(1)
                    // Resume flushing after idling else packets won't get sent immediately
                    .thenExecute(pair -> player.connection.resumeFlushing())
                    .thenExecute(pair -> {
                        var entity = pair.getFirst();
                        var newPlayer = pair.getSecond();
                        helper.assertTrue(
                                newPlayer.getOutboundPayloads(SyncAttachmentsPayload.class)
                                        .map(SyncAttachmentsPayload::target)
                                        .toList()
                                        .containsAll(List.of(
                                                new SyncAttachmentsPayload.BlockEntityTarget(helper.absolutePos(feetPos)),
                                                new SyncAttachmentsPayload.ChunkTarget(player.chunkPosition()),
                                                new SyncAttachmentsPayload.EntityTarget(player.getId()),
                                                new SyncAttachmentsPayload.EntityTarget(entity.getId()))),
                                "Expected to find that player received all sync payloads");
                        newPlayer.disconnectGameTest();
                    })
                    .thenMap(Pair::getFirst)
                    // Test that removing data causes players to receive packets
                    .thenExecute(entity -> {
                        entity.removeData(intAttachment);

                        var payload = player.requireOutboundPayload(SyncAttachmentsPayload.class);
                        helper.expectTarget(payload, new SyncAttachmentsPayload.EntityTarget(entity.getId()));

                        var holder = helper.holder();
                        holder.setData(intAttachment, 1);
                        holder.readFrom(payload);
                        holder.assertEqual(intAttachment, null);

                        player.clearOutboundPackets();
                    })
                    // Test that manual syncs send the packets to players that track the entity
                    .thenExecute(entity -> {
                        var attachment = entity.getData(mutableIntAttachment);
                        var holder = helper.holder();
                        var payload = player.requireOutboundPayload(SyncAttachmentsPayload.class);
                        holder.readFrom(payload);
                        // Default value is expected to send a packet too
                        holder.assertEqual(mutableIntAttachment, new MutableInt(23));

                        player.clearOutboundPackets();

                        attachment.setValue(30);
                        helper.assertTrue(
                                player.getOutboundPayloads(SyncAttachmentsPayload.class).count() == 0,
                                "Expected player to receive no sync payloads for mutable attachment having its inner value updated");

                        entity.syncData(mutableIntAttachment);

                        payload = player.requireOutboundPayload(SyncAttachmentsPayload.class);
                        holder.readFrom(payload);
                        holder.assertEqual(mutableIntAttachment, new MutableInt(30));

                        player.clearOutboundPackets();
                    })
                    // Test that values are not synced if the sync predicate returns false
                    .thenExecute(entity -> {
                        player.setData(blacklistedPlayer, true);
                        entity.setData(mutableIntAttachment, new MutableInt(4));
                        helper.assertTrue(
                                player.getOutboundPayloads(SyncAttachmentsPayload.class).count() == 0,
                                "Expected player not to receive a sync payload as the predicate should've returned false");
                    })
                    .thenSucceed();
        });
    }
}
