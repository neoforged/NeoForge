/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.attachment;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
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
    @EmptyTemplate
    @TestHolder(description = "Gametest that tests if attachments sync properly in different scenarios")
    static void testAttachmentSync(DynamicTest test, RegistrationHelper reg) {
        var intAttachment = reg.attachments().register("int", () -> AttachmentType.builder(() -> 0)
                .serialize(Codec.INT.fieldOf("value")).sync(ByteBufCodecs.VAR_INT).build());

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

            public class Holder extends AttachmentHolder {
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

            // Test that players receive updates for changes to their own data
            {
                var testValue = helper.randomInt();
                player.setData(intAttachment, testValue);

                var payload = player.requireOutboundPayload(SyncAttachmentsPayload.class);
                helper.expectTarget(payload, new SyncAttachmentsPayload.EntityTarget(player.getId()));

                var holder = helper.holder();
                holder.readFrom(payload);
                holder.assertEqual(intAttachment, testValue);

                player.clearOutboundPackets();
            }

            // Test that players receive updates for changes to the chunk they're in
            {
                var chunk = helper.getLevel().getChunkAt(player.blockPosition());
                var testValue = helper.randomInt();
                chunk.setData(intAttachment, testValue);

                var payload = player.requireOutboundPayload(SyncAttachmentsPayload.class);
                helper.expectTarget(payload, new SyncAttachmentsPayload.ChunkTarget(player.chunkPosition()));

                var holder = helper.holder();
                holder.readFrom(payload);
                holder.assertEqual(intAttachment, testValue);

                player.clearOutboundPackets();
            }

            helper.succeed();
        });
    }
}
