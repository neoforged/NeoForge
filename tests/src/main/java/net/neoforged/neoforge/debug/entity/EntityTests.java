/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity;

import java.util.function.Consumer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.payload.AdvancedAddEntityPayload;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;
import org.jetbrains.annotations.NotNull;

@ForEachTest(groups = EntityTests.GROUP)
public class EntityTests {
    public static final String GROUP = "level.entity";

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if custom fence gates without wood types work, allowing for the use of the vanilla block for non-wooden gates")
    static void customSpawnLogic(final DynamicTest test, final RegistrationHelper reg) {
        final var complexSpawn = reg.entityTypes().registerType("complex_spawn", () -> EntityType.Builder.of(CustomComplexSpawnEntity::new, MobCategory.AMBIENT)
                .sized(1, 1)).withLang("Custom complex spawn egg").withRenderer(() -> NoopRenderer::new);
        final var adaptedSpawn = reg.entityTypes().registerType("adapted_spawn", () -> EntityType.Builder.of(AdaptedSpawnEntity::new, MobCategory.AMBIENT)
                .sized(1, 1)).withLang("Adapted complex spawn egg").withRenderer(() -> NoopRenderer::new);
        final var simpleSpawn = reg.entityTypes().registerType("simple_spawn", () -> EntityType.Builder.of(SimpleEntity::new, MobCategory.AMBIENT)
                .sized(1, 1)).withLang("Simple spawn egg").withRenderer(() -> NoopRenderer::new);

        reg.eventListeners().accept((Consumer<RegisterPayloadHandlerEvent>) event -> event.registrar("test")
                .play(CustomSyncPayload.ID, CustomSyncPayload::new, (payload, context) -> {}));

        test.onGameTest(helper -> {
            helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                    .thenExecute(() -> helper.spawn(complexSpawn.get(), new BlockPos(1, 1, 1)))

                    // Check if forge payload was sent
                    .thenExecute(player -> helper.assertTrue(
                            player.getOutboundPayloads(AdvancedAddEntityPayload.class)
                                    .findAny().isPresent(),
                            "Advanced payload for custom spawn was not send"))
                    .thenSucceed();
            helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                    .thenExecute(() -> helper.spawn(adaptedSpawn.get(), new BlockPos(1, 1, 1)))

                    // Check if custom payload was sent
                    .thenExecute(player -> helper.assertTrue(
                            player.getOutboundPayloads(CustomSyncPayload.class)
                                    .findAny().isPresent(),
                            "Custom sync payload for adapted spawn was not send"))
                    .thenSucceed();
            helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                    .thenExecute(() -> helper.spawn(simpleSpawn.get(), new BlockPos(1, 1, 1)))

                    // Check if custom payload was sent
                    .thenExecute(player -> helper.assertTrue(
                            player.getOutboundPayloads(AdvancedAddEntityPayload.class)
                                    .findAny().isEmpty(),
                            "Advanced payload for custom spawn was send"))
                    .thenExecute(player -> helper.assertTrue(
                            player.getOutboundPayloads(CustomSyncPayload.class)
                                    .findAny().isEmpty(),
                            "Custom sync payload for custom spawn was send"))
                    .thenSucceed();
        });
    }

    public static final class CustomComplexSpawnEntity extends Entity implements IEntityWithComplexSpawn {

        public CustomComplexSpawnEntity(EntityType<?> p_19870_, Level p_19871_) {
            super(p_19870_, p_19871_);
        }

        @Override
        protected void defineSynchedData() {

        }

        @Override
        protected void readAdditionalSaveData(@NotNull CompoundTag p_20052_) {

        }

        @Override
        protected void addAdditionalSaveData(@NotNull CompoundTag p_20139_) {

        }

        @Override
        public void writeSpawnData(FriendlyByteBuf buffer) {

        }

        @Override
        public void readSpawnData(FriendlyByteBuf additionalData) {

        }
    }

    public static final class AdaptedSpawnEntity extends Entity {

        public AdaptedSpawnEntity(EntityType<?> p_19870_, Level p_19871_) {
            super(p_19870_, p_19871_);
        }

        @Override
        protected void defineSynchedData() {

        }

        @Override
        protected void readAdditionalSaveData(@NotNull CompoundTag p_20052_) {

        }

        @Override
        protected void addAdditionalSaveData(@NotNull CompoundTag p_20139_) {

        }

        @Override
        public void sendPairingData(ServerPlayer serverPlayer, Consumer<CustomPacketPayload> bundleBuilder) {
            bundleBuilder.accept(new CustomSyncPayload());
        }
    }

    public static final class SimpleEntity extends Entity {

        public SimpleEntity(EntityType<?> p_19870_, Level p_19871_) {
            super(p_19870_, p_19871_);
        }

        @Override
        protected void defineSynchedData() {

        }

        @Override
        protected void readAdditionalSaveData(CompoundTag p_20052_) {

        }

        @Override
        protected void addAdditionalSaveData(CompoundTag p_20139_) {

        }
    }

    public record CustomSyncPayload() implements CustomPacketPayload {

        private static final ResourceLocation ID = new ResourceLocation("test", "custom_sync_payload");

        public CustomSyncPayload(FriendlyByteBuf buf) {
            this();
        }

        @Override
        public void write(@NotNull FriendlyByteBuf buf) {}

        @Override
        public ResourceLocation id() {
            return ID;
        }
    }
}
