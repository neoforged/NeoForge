/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity;

import java.lang.reflect.Method;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.payload.AdvancedAddEntityPayload;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.registration.RegistrationHelper;
import org.jspecify.annotations.Nullable;

@ForEachTest(groups = EntityTests.GROUP)
public class EntityTests {
    public static final String GROUP = "level.entity";

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if custom fence gates without wood types work, allowing for the use of the vanilla block for non-wooden gates")
    static void customSpawnLogic(final DynamicTest test, final RegistrationHelper reg) {
        Supplier<AttributeSupplier.Builder> attr = () -> AttributeSupplier.builder()
                .add(Attributes.MAX_HEALTH, 1);
        final var usingForgeAdvancedSpawn = reg.entityTypes().registerEntityType("complex_spawn", CustomComplexSpawnEntity::new, MobCategory.AMBIENT, builder -> builder.sized(1, 1))
                .withLang("Custom complex spawn egg").withAttributes(attr).withRenderer(() -> NoopRenderer::new);
        final var usingCustomPayloadsSpawn = reg.entityTypes().registerEntityType("adapted_spawn", AdaptedSpawnEntity::new, MobCategory.AMBIENT, builder -> builder.sized(1, 1))
                .withLang("Adapted complex spawn egg").withAttributes(attr).withRenderer(() -> NoopRenderer::new);
        final var simpleSpawn = reg.entityTypes().registerEntityType("simple_spawn", SimpleEntity::new, MobCategory.AMBIENT, builder -> builder.sized(1, 1))
                .withLang("Simple spawn egg").withAttributes(attr).withRenderer(() -> NoopRenderer::new);

        reg.eventListeners().accept((Consumer<RegisterPayloadHandlersEvent>) event -> event.registrar("1")
                .playToClient(EntityTests.CustomSyncPayload.TYPE, CustomSyncPayload.STREAM_CODEC, (payload, context) -> {}));

        test.onGameTest(helper -> {
            helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                    .thenExecute(() -> helper.spawn(usingForgeAdvancedSpawn.get(), new BlockPos(1, 1, 1)))

                    // Check if forge payload was sent
                    .thenExecute(player -> helper.assertTrue(
                            player.getOutboundPayloads(AdvancedAddEntityPayload.class)
                                    .findAny().isPresent(),
                            "Advanced payload for custom spawn was not send"))
                    .thenSucceed();
            helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                    .thenExecute(() -> helper.spawn(usingCustomPayloadsSpawn.get(), new BlockPos(1, 1, 1)))

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
        public CustomComplexSpawnEntity(EntityType<?> type, Level level) {
            super(type, level);
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder builder) {}

        @Override
        protected void readAdditionalSaveData(ValueInput data) {}

        @Override
        protected void addAdditionalSaveData(ValueOutput data) {}

        @Override
        public void writeSpawnData(RegistryFriendlyByteBuf buffer) {}

        @Override
        public void readSpawnData(RegistryFriendlyByteBuf additionalData) {}

        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
            return false;
        }
    }

    public static final class AdaptedSpawnEntity extends Entity {
        public AdaptedSpawnEntity(EntityType<?> type, Level level) {
            super(type, level);
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder builder) {}

        @Override
        protected void readAdditionalSaveData(ValueInput data) {}

        @Override
        protected void addAdditionalSaveData(ValueOutput data) {}

        @Override
        public void sendPairingData(ServerPlayer serverPlayer, Consumer<CustomPacketPayload> bundleBuilder) {
            bundleBuilder.accept(new CustomSyncPayload());
        }

        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
            return false;
        }
    }

    public static final class SimpleEntity extends Entity {
        public SimpleEntity(EntityType<?> type, Level level) {
            super(type, level);
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder builder) {}

        @Override
        protected void readAdditionalSaveData(ValueInput data) {}

        @Override
        protected void addAdditionalSaveData(ValueOutput data) {}

        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
            return false;
        }
    }

    public record CustomSyncPayload() implements CustomPacketPayload {
        private static final CustomPacketPayload.Type<CustomSyncPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("test", "custom_sync_payload"));
        private static final StreamCodec<FriendlyByteBuf, CustomSyncPayload> STREAM_CODEC = StreamCodec.unit(new EntityTests.CustomSyncPayload());

        @Override
        public CustomPacketPayload.Type<CustomSyncPayload> type() {
            return TYPE;
        }
    }

    private static boolean hasGoal(final Mob mob, final Class<? extends Goal> type) {
        return mob.goalSelector.getAvailableGoals().stream().anyMatch(goal -> type.isInstance(goal.getGoal()));
    }

    private static boolean canUseGoal(final Mob mob, final Class<? extends Goal> type) {
        return mob.goalSelector.getAvailableGoals().stream().filter(goal -> type.isInstance(goal.getGoal())).anyMatch(WrappedGoal::canUse);
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if ranged mobs use modded bows and crossbows, and not only the vanilla ones")
    static void rangedMobsUseModdedWeapons(final DynamicTest test, final RegistrationHelper reg) {
        final var moddedBow = reg.items().registerItem("modded_bow", properties -> new BowItem(properties.durability(384))).withLang("Modded bow");
        final var moddedCrossbow = reg.items().registerItem("modded_crossbow", properties -> new CrossbowItem(properties.durability(326))).withLang("Modded crossbow");

        test.onGameTest(helper -> {
            Skeleton skeleton = helper.spawnWithNoFreeWill(EntityTypes.SKELETON, 1, 2, 1);
            skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STICK));
            skeleton.reassessWeaponGoal();
            helper.assertFalse(hasGoal(skeleton, RangedBowAttackGoal.class), "Expected a skeleton without a bow not to use bow attacks");
            skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(moddedBow.get()));
            skeleton.reassessWeaponGoal();
            helper.assertTrue(hasGoal(skeleton, RangedBowAttackGoal.class), "Expected a skeleton holding a modded bow to use bow attacks");

            // Spawned with its goals, as removing the free will of a mob clears them; the crossbow goal also needs a valid target
            Pillager pillager = helper.spawn(EntityTypes.PILLAGER, 1, 2, 1);
            pillager.setTarget(helper.makeMockPlayer(GameType.SURVIVAL));
            pillager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STICK));
            helper.assertFalse(canUseGoal(pillager, RangedCrossbowAttackGoal.class), "Expected a pillager without a crossbow not to use crossbow attacks");
            pillager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(moddedCrossbow.get()));
            helper.assertTrue(canUseGoal(pillager, RangedCrossbowAttackGoal.class), "Expected a pillager holding a modded crossbow to use crossbow attacks");

            helper.killAllEntities();
            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate(value = "7x4x7", floor = true)
    @TestHolder(description = "Tests if an item can hide the gaze of a player from an enderman through IItemExtension#isGazeDisguise, depending on the player and on the entity looked at")
    static void gazeDisguiseItem(final DynamicTest test, final RegistrationHelper reg) {
        final var enderMask = reg.items().registerItem("ender_mask", properties -> new Item(properties.humanoidArmor(ArmorMaterials.LEATHER, ArmorType.HELMET)) {
            @Override
            public boolean isGazeDisguise(ItemStack stack, Player player, @Nullable LivingEntity entity) {
                return entity instanceof Enderman && player.experienceLevel >= 10;
            }
        }).withLang("Ender mask");

        test.onGameTest(helper -> {
            Enderman enderman = helper.spawnWithNoFreeWill(EntityTypes.ENDERMAN, 1, 2, 3);
            LivingEntity pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, 3, 2, 5);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setPos(helper.absoluteVec(new Vec3(5.5, 2, 3.5)));
            player.experienceLevel = 10;
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(enderMask.get()));

            helper.assertFalse(LivingEntity.PLAYER_NOT_WEARING_DISGUISE_ITEM_FOR_TARGET.test(player, enderman), "Expected the mask to hide the gaze of the player from the enderman");
            helper.assertTrue(LivingEntity.PLAYER_NOT_WEARING_DISGUISE_ITEM_FOR_TARGET.test(player, pig), "Expected the mask not to hide the gaze of the player from a pig");
            player.experienceLevel = 0;
            helper.assertTrue(LivingEntity.PLAYER_NOT_WEARING_DISGUISE_ITEM_FOR_TARGET.test(player, enderman), "Expected the mask not to hide the gaze of a player below level 10");
            player.experienceLevel = 10;

            // The check of the enderman itself, which decides whether a player looking at it makes it angry
            Method isBeingStaredBy = helper.catchException(() -> ObfuscationReflectionHelper.findMethod(Enderman.class, "isBeingStaredBy", Player.class));
            BooleanSupplier stared = () -> {
                player.lookAt(EntityAnchorArgument.Anchor.EYES, enderman.getEyePosition());
                return helper.catchException(() -> (boolean) isBeingStaredBy.invoke(enderman, player));
            };
            helper.assertFalse(stared.getAsBoolean(), "Expected the enderman not to be stared at by a player wearing the mask");
            player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            helper.assertTrue(stared.getAsBoolean(), "Expected the enderman to be stared at by a player without the mask");
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CARVED_PUMPKIN));
            helper.assertFalse(stared.getAsBoolean(), "Expected the carved pumpkin to still hide the gaze of the player, through the vanilla tag");

            helper.killAllEntities();
            helper.succeed();
        });
    }
}
