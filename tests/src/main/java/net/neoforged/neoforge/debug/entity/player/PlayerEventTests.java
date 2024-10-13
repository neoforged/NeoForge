/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.player;

import java.util.Objects;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.TeleportTransition;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.StatAwardEvent;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PermissionsChangedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTestPlayer;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = { PlayerTests.GROUP + ".event", "event" })
public class PlayerEventTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the player NameFormat event allows changing a player's name")
    static void playerNameEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final PlayerEvent.NameFormat event) -> {
            if (event.getEntity().getGameProfile().getName().equals("test-mock-player")) {
                event.setDisplayname(Component.literal("hello world"));
            }
            test.pass();
        });

        test.onGameTest(helper -> {
            helper.assertEntityProperty(
                    helper.makeMockPlayer(),
                    player -> player.getDisplayName().getString(),
                    "display name",
                    "hello world");
            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the UseItemOnBlockEvent fires, cancelling item logic if dirt is placed on top of dispenser")
    static void useItemOnBlockEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final UseItemOnBlockEvent event) -> {
            UseOnContext context = event.getUseOnContext();
            Level level = context.getLevel();
            // cancel item logic if dirt is placed on top of dispenser
            if (event.getUsePhase() == UseItemOnBlockEvent.UsePhase.ITEM_AFTER_BLOCK) {
                ItemStack stack = context.getItemInHand();
                Item item = stack.getItem();
                if (item instanceof BlockItem blockItem && blockItem.getBlock() == Blocks.DIRT) {
                    BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());
                    if (level.getBlockState(placePos.below()).getBlock() == Blocks.DISPENSER) {
                        if (!level.isClientSide) {
                            context.getPlayer().displayClientMessage(Component.literal("Can't place dirt on dispenser"), false);
                        }
                        test.pass();
                        event.cancelWithResult(InteractionResult.SUCCESS);
                    }
                }
            }
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.DISPENSER))
                .thenExecute(() -> helper.useOn(
                        new BlockPos(1, 1, 1),
                        Items.DIRT.getDefaultInstance(),
                        helper.makeMockPlayer(),
                        Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.DIRT, 1, 2, 1))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the on entity interact event is fired")
    static void entityInteractEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final PlayerInteractEvent.EntityInteractSpecific event) -> {
            if (event.getTarget().getType() == EntityType.ILLUSIONER) {
                String oldName = event.getTarget().getName().getString();
                event.getTarget().setCustomName(Component.literal(oldName + " entityInteractEventTest"));
            }
        });

        test.onGameTest(helper -> {
            Mob illusioner = helper.spawnWithNoFreeWill(EntityType.ILLUSIONER, 1, 1, 1);
            helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                    .thenExecute(player -> player.connection.handleInteract(ServerboundInteractPacket.createInteractionPacket(illusioner, player.isShiftKeyDown(), InteractionHand.MAIN_HAND, helper.absoluteVec(new BlockPos(1, 1, 1).getCenter()))))
                    .thenExecute(player -> helper.assertTrue(illusioner.getName().getString().contains("entityInteractEventTest"), "Illager name did not get changed on player interact"))
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the ItemPickupEvent fires")
    public static void itemPickupEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((ItemEntityPickupEvent.Post event) -> {
            if (event.getOriginalStack().is(Items.MELON_SEEDS)) {
                // If the event is fired and detects pickup of melon seeds, the test will be considered pass
                // and the player will get pumpkin seeds too
                event.getPlayer().addItem(new ItemStack(Items.PUMPKIN_SEEDS));
                test.pass();
            }
        });

        test.onGameTest(helper -> {
            // Spawn a player at the centre of the test
            final GameTestPlayer player = helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL)
                    .moveToCentre();
            helper.spawnItem(Items.MELON_SEEDS, 1, 2, 1);

            helper.startSequence()
                    // Wait until the player picked up the seeds
                    .thenWaitUntil(() -> helper.assertPlayerHasItem(player, Items.MELON_SEEDS))
                    // Check for pumpkin seeds in the player's inventory
                    .thenExecute(() -> helper.assertPlayerHasItem(player, Items.PUMPKIN_SEEDS))
                    // All assertions were true, so the test is a success!
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the PlayerChangeGameModeEvent is fired and can modify the outcome")
    static void playerChangeGameModeEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final PlayerEvent.PlayerChangeGameModeEvent event) -> {
            // Only affect the players with a custom name to not interfere with other tests
            if (!Objects.equals(event.getEntity().getCustomName(), Component.literal("gamemode-changes"))) {
                return;
            }

            // prevent changing to SURVIVAL
            if (event.getNewGameMode() == GameType.SURVIVAL) {
                event.setCanceled(true);
            } else if (event.getNewGameMode() == GameType.SPECTATOR) {
                // when changing to SPECTATOR, change to SURVIVAL instead
                event.setNewGameMode(GameType.SURVIVAL);
            }
        });

        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInLevel(GameType.CREATIVE).moveToCorner())
                .thenExecute(player -> player.setCustomName(Component.literal("gamemode-changes")))

                .thenExecute(player -> player.setGameMode(GameType.SURVIVAL))
                // Prevent changing to survival
                .thenExecute(player -> helper.assertTrue(player.gameMode.getGameModeForPlayer() == GameType.CREATIVE, "Event was not cancelled"))

                // Actually change to spectator
                .thenExecute(player -> player.setGameMode(GameType.SPECTATOR))
                .thenExecute(player -> helper.assertTrue(player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL, "Event did not modify game mode"))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the PermissionsChangedEvent is fired, by preventing players from being de-op'd")
    static void permissionsChangedEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final PermissionsChangedEvent event) -> {
            if (Objects.equals(event.getEntity().getCustomName(), Component.literal("permschangedevent")) && event.getOldLevel() == Commands.LEVEL_ADMINS) {
                event.setCanceled(true);
                test.pass();
            }
        });

        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInLevel(GameType.CREATIVE).moveToCorner())
                .thenExecute(player -> player.setCustomName(Component.literal("permschangedevent")))
                // Make sure the player isn't OP by default
                .thenExecute(player -> player.getServer().getPlayerList().getOps().add(new ServerOpListEntry(
                        player.getGameProfile(), Commands.LEVEL_ADMINS, true)))
                .thenExecute(player -> player.getServer().getPlayerList().deop(player.getGameProfile()))
                .thenExecute(player -> helper.assertTrue(player.getServer().getProfilePermissions(player.getGameProfile()) == Commands.LEVEL_ADMINS, "Player was de-op'd"))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the StatsAwardEvent properly modifies stats stored per player")
    static void changeStatAward(final DynamicTest test, final RegistrationHelper reg) {
        test.eventListeners().forge().addListener(EventPriority.NORMAL, false, StatAwardEvent.class, event -> {
            //when damage is dealt, instead record this stat as a bell ring
            if (event.getStat().equals(Stats.CUSTOM.get(Stats.DAMAGE_TAKEN)))
                event.setStat(Stats.CUSTOM.get(Stats.BELL_RING));
        });
        test.eventListeners().forge().addListener(EventPriority.NORMAL, false, StatAwardEvent.class, event -> {
            //when awarded stats for breeding, multiply the value by 10
            if (event.getStat().equals(Stats.CUSTOM.get(Stats.ANIMALS_BRED)))
                event.setValue(event.getValue() * 10);
        });

        test.onGameTest(helper -> {
            GameTestPlayer player = helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL);
            //Award a damage stat, which we are listening for in order to change the stat
            player.awardStat(Stats.CUSTOM.get(Stats.DAMAGE_TAKEN), 100);
            //Award an animal breed stat, which we are listining for in order to multiply the value
            player.awardStat(Stats.CUSTOM.get(Stats.ANIMALS_BRED), 1);
            ServerStatsCounter stats = player.level().getServer().getPlayerList().getPlayerStats(player);
            //if our damage stat is changed to bell ring and our animal breed stat is multiplied by ten, the test passes
            if (stats.getValue(Stats.CUSTOM.get(Stats.BELL_RING)) == 100 && stats.getValue(Stats.CUSTOM.get(Stats.ANIMALS_BRED)) == 10)
                helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if ArmorHurtEvent fires and prevents armor damage.")
    static void armorHurtEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final ArmorHurtEvent event) -> {
            if (event.getEntity() instanceof Player player && player.getItemBySlot(EquipmentSlot.CHEST).getItem().equals(Items.DIAMOND_CHESTPLATE))
                event.setNewDamage(EquipmentSlot.CHEST, 5);
        });

        test.onGameTest(helper -> {
            DamageSource source = new DamageSource(helper.getLevel().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.MOB_ATTACK));
            helper.startSequence(() -> helper.makeMockPlayer(GameType.SURVIVAL))
                    .thenExecute(player -> player.invulnerableTime = 0)
                    .thenExecute(player -> player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE)))
                    .thenExecute(player -> player.hurt(source, 10F))
                    .thenWaitUntil(player -> helper.assertTrue(player.getItemBySlot(EquipmentSlot.CHEST).getItem().equals(Items.DIAMOND_CHESTPLATE)
                            && player.getItemBySlot(EquipmentSlot.CHEST).getDamageValue() == 5,
                            "Armor hurt not applied. %s actual but expected 5f".formatted(player.getItemBySlot(EquipmentSlot.CHEST).getDamageValue())))
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the PlayerRespawnPositionEvent fires correctly and can change where the player respawns")
    static void playerRespawnPositionEvent(final DynamicTest test, final RegistrationHelper reg) {
        test.eventListeners().forge().addListener((final PlayerRespawnPositionEvent event) -> {
            // Only affect the players with a custom name to not interfere with other tests
            if (!Objects.equals(event.getEntity().getCustomName(), Component.literal("respawn-position-test"))) {
                return;
            }

            var oldTransition = event.getTeleportTransition();
            var newTransition = new TeleportTransition(oldTransition.newLevel(),
                    event.getEntity().position().relative(Direction.SOUTH, 1),
                    oldTransition.deltaMovement(),
                    oldTransition.xRot(),
                    oldTransition.yRot(),
                    oldTransition.missingRespawnBlock(),
                    oldTransition.asPassenger(),
                    oldTransition.relatives(),
                    oldTransition.postTeleportTransition());
            event.setTeleportTransition(newTransition);
        });

        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(player -> player.setCustomName(Component.literal("respawn-position-test")))
                .thenExecute(player -> player.setRespawnPosition(player.getRespawnDimension(), helper.absolutePos(new BlockPos(0, 1, 0)), 0, false, true))
                .thenExecute(player -> Objects.requireNonNull(player.getServer()).getPlayerList().respawn(player, false, Entity.RemovalReason.KILLED))
                .thenExecute(() -> helper.assertEntityPresent(EntityType.PLAYER, new BlockPos(0, 1, 1)))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the PlayerRespawnEvent fires correctly and can modify the player after respawning")
    static void playerRespawnEvent(final DynamicTest test, final RegistrationHelper reg) {
        test.eventListeners().forge().addListener((final PlayerEvent.PlayerRespawnEvent event) -> {
            event.getEntity().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE));
        });

        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(player -> player.setRespawnPosition(player.getRespawnDimension(), helper.absolutePos(new BlockPos(0, 1, 1)), 0, true, true))
                .thenExecute(player -> Objects.requireNonNull(player.getServer()).getPlayerList().respawn(player, false, Entity.RemovalReason.KILLED))
                .thenExecute(() -> helper.assertEntityIsHolding(new BlockPos(0, 1, 1), EntityType.PLAYER, Items.APPLE))
                .thenSucceed());
    }
}
