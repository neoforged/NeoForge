/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.level;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.level.AlterGroundEvent;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.gametest.GameTestPlayer;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;

@ForEachTest(groups = { LevelTests.GROUP + ".event", "event" })
public class LevelEventTests {
    // Offsets, in chunks, of the far away chunks that the tests of this class load. They keep these chunks apart from each other
    // and far from the test structures, which the game test server lays out in rows of at most ten structures of a few blocks each
    /** Generated for the first time by {@link #chunkLoadEvent}, then loaded again. */
    private static final ChunkPos CHUNK_LOAD_EVENT_OFFSET = new ChunkPos(200, 200);
    /** Loaded and modified by {@link #chunkSaveEvent}, which waits for it to unload and be saved. */
    private static final ChunkPos CHUNK_SAVE_EVENT_OFFSET = new ChunkPos(-200, -200);
    /** Forced to load by {@link #forcedChunkTickets} through a ticket, then released. */
    private static final ChunkPos FORCED_CHUNK_OFFSET = new ChunkPos(300, 300);

    /** The chunk at the given offset from the chunk of the test. */
    private static ChunkPos farChunk(final ExtendedGameTestHelper helper, final ChunkPos offset) {
        ChunkPos testChunk = ChunkPos.containing(helper.absolutePos(BlockPos.ZERO));
        return new ChunkPos(testChunk.x() + offset.x(), testChunk.z() + offset.z());
    }

    @GameTest
    @EmptyTemplate(value = "9x9x9", floor = true)
    @TestHolder(description = "Tests if the sapling grow tree event is fired, replacing spruce with birch")
    static void saplingGrowTreeEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final BlockGrowFeatureEvent event) -> {
            if (event.getFeature() != null && event.getFeature().is(TreeFeatures.SPRUCE)) {
                event.setFeature(TreeFeatures.BIRCH_BEES_005);
            }
            test.pass();
        });

        test.onGameTest(helper -> helper.startSequence(helper::makeMockPlayer)
                .thenExecute(() -> helper.setBlock(4, 1, 4, Blocks.DIRT))
                .thenExecute(() -> helper.setBlock(4, 2, 4, Blocks.SPRUCE_SAPLING))
                .thenWaitUntil(player -> helper.boneMealUntilGrown(4, 2, 4, player))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.BIRCH_LOG, 4, 2, 4))
                .thenSucceed());
    }

    @GameTest
    @TestHolder(description = "Tests if the alter ground event is fired, replacing podzol with redstone blocks")
    static void alterGroundEvent(final DynamicTest test) {
        test.registerGameTestTemplate(StructureTemplateBuilder.withSize(16, 32, 16)
                .fill(0, 0, 0, 15, 0, 15, Blocks.GRASS_BLOCK.defaultBlockState())
                .set(7, 1, 7, Blocks.SPRUCE_SAPLING.defaultBlockState())
                .set(8, 1, 7, Blocks.SPRUCE_SAPLING.defaultBlockState())
                .set(7, 1, 8, Blocks.SPRUCE_SAPLING.defaultBlockState())
                .set(8, 1, 8, Blocks.SPRUCE_SAPLING.defaultBlockState()));

        test.eventListeners().forge().addListener((final AlterGroundEvent event) -> {
            final AlterGroundEvent.StateProvider old = event.getStateProvider();
            event.setStateProvider((level, rand, pos) -> {
                final BlockState state = old.getState(level, rand, pos);
                return state != null && state.is(Blocks.PODZOL) ? Blocks.REDSTONE_BLOCK.defaultBlockState() : state;
            });
        });

        test.onGameTest(helper -> helper.startSequence(helper::makeMockPlayer)
                .thenWaitUntil(player -> helper.boneMealUntilGrown(7, 1, 7, player))
                .thenExecute(player -> helper.assertTrue(
                        helper.blocksBetween(0, 0, 0, 16, 1, 16).anyMatch(pos -> helper.getLevel().getBlockState(pos).is(Blocks.REDSTONE_BLOCK)),
                        "No redstone blocks have been placed!"))
                .thenExecute(player -> helper.assertTrue(
                        helper.blocksBetween(0, 0, 0, 16, 1, 16)
                                .noneMatch(pos -> helper.getLevel().getBlockState(pos).is(Blocks.PODZOL)),
                        "Podzol was still placed!"))
                .thenSucceed());
    }

    /**
     * Tests {@link VanillaGameEvent} by listening for and printing out any uses of shears in the overworld.
     */
    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests the vanilla game event by hurting entities that are sheared in the overworld")
    static void vanillaGameEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final VanillaGameEvent event) -> {
            if (event.getVanillaEvent().is(GameEvent.SHEAR) && event.getLevel().dimension() == Level.OVERWORLD) {
                final var entities = event.getLevel().getEntitiesOfClass(Entity.class, new AABB(BlockPos.containing(event.getEventPosition())), e -> e instanceof Shearable);
                entities.get(0).hurt(event.getLevel().damageSources().generic(), event.getCause() == null ? 1 : 3);
                test.pass();
            }
        });

        test.onGameTest(helper -> helper.startSequence(() -> helper.spawnWithNoFreeWill(EntityTypes.SHEEP, new BlockPos(1, 2, 1)))
                .thenExecute(sheep -> sheep.setColor(DyeColor.BLACK))
                .thenExecute(sheep -> sheep.setSheared(false))

                // Prepare a dispenser to shear the sheep in the second phase
                .thenSequence(sequence -> sequence
                        .thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP)))
                        .thenMap(() -> helper.getBlockEntity(1, 1, 1, DispenserBlockEntity.class))
                        .thenExecute(dispenser -> dispenser.setItem(1, Items.SHEARS.getDefaultInstance())))

                .thenIdle(5)
                .thenExecute(sheep -> Items.SHEARS.getDefaultInstance().interactLivingEntity(
                        helper.makeMockPlayer(), sheep, InteractionHand.MAIN_HAND)) // Make a player shear the sheep
                .thenExecute(() -> helper.assertItemEntityPresent(Items.WOOL.black(), new BlockPos(1, 2, 1), 2)) // Make sure wool was dropped
                .thenExecute(sheep -> helper.assertEntityProperty(sheep, Sheep::getHealth, "health", 8f - 3f)) // player did it, so hurt by 3

                .thenExecuteAfter(5, sheep -> {
                    // Prepare the sheep; reset its color and its state
                    sheep.setColor(DyeColor.BLUE);
                    sheep.setSheared(false);
                })
                .thenIdle(5)

                // Power the dispenser
                .thenExecute(() -> helper.setBlock(2, 1, 1, Blocks.REDSTONE_BLOCK))
                .thenIdle(5)
                .thenExecute(() -> helper.assertItemEntityPresent(Items.WOOL.blue(), new BlockPos(1, 2, 1), 2)) // Make sure wool was dropped
                .thenExecute(sheep -> helper.assertEntityProperty(sheep, Sheep::getHealth, "health", (8f - 3f) - 1f)) // dispenser did it, so hurt by 1

                .thenIdle(5)
                .thenExecute(() -> helper.killAllEntitiesOfClass(Sheep.class, ItemEntity.class))

                .thenSucceed());
    }

    // A few attempts, in case the chunk picked below exists from an earlier run of a kept level
    @GameTest(attempts = 3)
    @EmptyTemplate
    @TestHolder(description = "Tests if the ChunkEvent.Load event tells freshly generated chunks apart from chunks loaded again")
    static void chunkLoadEvent(final DynamicTest test) {
        AtomicReference<ChunkPos> target = new AtomicReference<>();
        List<Boolean> loads = new ArrayList<>();
        test.eventListeners().forge().addListener((final ChunkEvent.Load event) -> {
            if (event.getLevel() instanceof ServerLevel && event.getChunk().getPos().equals(target.get())) {
                loads.add(event.isNewChunk());
            }
        });

        test.onGameTest(helper -> {
            ServerLevel level = helper.getLevel();
            // A chunk that was never generated: far from the test, and shifted by the current time in milliseconds, so that a
            // level kept between runs, like the one of a client, does not have it from an earlier run either, and so that each
            // attempt of the test picks another chunk
            ChunkPos farChunk = farChunk(helper, CHUNK_LOAD_EVENT_OFFSET);
            ChunkPos pos = new ChunkPos(farChunk.x(), farChunk.z() + (int) (System.currentTimeMillis() % 10000));
            target.set(pos);
            loads.clear();
            helper.assertFalse(level.getChunkSource().hasChunk(pos.x(), pos.z()), "Expected the far chunk not to be loaded yet");

            helper.startSequence()
                    .thenExecute(() -> {
                        level.getChunk(pos.x(), pos.z());
                        helper.assertValueEqual(loads, List.of(true), "Expected a single load event, for a new chunk; events are " + loads);
                    })
                    // The temporary ticket of getChunk expires, which lets the chunk go
                    .thenWaitUntil(() -> helper.assertFalse(level.getChunkSource().hasChunk(pos.x(), pos.z()), "Expected the far chunk to be unloaded again"))
                    .thenExecute(() -> {
                        level.getChunk(pos.x(), pos.z());
                        helper.assertValueEqual(loads, List.of(true, false), "Expected a second load event, not for a new chunk; events are " + loads);
                    })
                    .thenSucceed();
        });
    }

    private static boolean hasWatchEvent(final List<? extends ChunkWatchEvent> events, final ServerPlayer player, final ChunkPos pos) {
        return events.stream().anyMatch(event -> event.getPlayer() == player && event.getPos().equals(pos));
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the ChunkWatchEvent fires when a player starts watching a chunk, when it is sent to them and when they stop watching it")
    static void chunkWatchEvent(final DynamicTest test) {
        List<ChunkWatchEvent.Watch> watched = new ArrayList<>();
        List<ChunkWatchEvent.Sent> sent = new ArrayList<>();
        List<ChunkWatchEvent.UnWatch> unwatched = new ArrayList<>();
        test.eventListeners().forge().addListener((final ChunkWatchEvent.Watch event) -> watched.add(event));
        test.eventListeners().forge().addListener((final ChunkWatchEvent.Sent event) -> sent.add(event));
        test.eventListeners().forge().addListener((final ChunkWatchEvent.UnWatch event) -> unwatched.add(event));

        test.onGameTest(helper -> {
            ChunkPos testChunk = ChunkPos.containing(helper.absolutePos(BlockPos.ZERO));
            GameTestPlayer player = helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL).moveToCentre();
            helper.assertTrue(hasWatchEvent(watched, player, testChunk), "Expected the player to start watching the chunk of the test");

            helper.startSequence()
                    .thenWaitUntil(() -> helper.assertTrue(hasWatchEvent(sent, player, testChunk), "Expected the chunk of the test to be sent to the player"))
                    .thenExecute(() -> {
                        // Shrink the view distance to its minimum and move six chunks away, which is further than the view distance and the border ring of chunks tracked around it
                        ClientInformation information = player.clientInformation();
                        player.connection.handleClientInformation(new ServerboundClientInformationPacket(new ClientInformation(
                                information.language(),
                                2,
                                information.chatVisibility(),
                                information.chatColors(),
                                information.modelCustomisation(),
                                information.mainHand(),
                                information.textFilteringEnabled(),
                                information.allowsListing(),
                                information.particleStatus())));
                        player.snapTo(player.getX() + 6 * 16, player.getY(), player.getZ());
                        helper.assertTrue(hasWatchEvent(unwatched, player, testChunk), "Expected the player to stop watching the chunk of the test");
                    })
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the ChunkDataEvent.Save event fires with the level of the chunk when chunks are saved")
    static void chunkSaveEvent(final DynamicTest test) {
        AtomicReference<ChunkPos> target = new AtomicReference<>();
        AtomicReference<ChunkDataEvent.Save> fired = new AtomicReference<>();
        test.eventListeners().forge().addListener((final ChunkDataEvent.Save event) -> {
            if (event.getChunk().getPos().equals(target.get())) {
                fired.set(event);
            }
        });

        test.onGameTest(helper -> {
            ServerLevel level = helper.getLevel();
            // A chunk far enough from the test and from the players to unload once nothing holds it anymore
            ChunkPos pos = farChunk(helper, CHUNK_SAVE_EVENT_OFFSET);
            target.set(pos);
            // Load the chunk and modify it, so it has unsaved changes, then let it go: the temporary ticket of getChunk expires and
            // the chunk is saved as it unloads. This is the regular path of a save; saving through the chunk source instead
            // skips chunks that are not ready to be saved yet, or that were saved recently
            level.getChunk(pos.x(), pos.z());
            level.setBlockAndUpdate(pos.getWorldPosition().atY(level.getMinY() + 1), Blocks.STONE.defaultBlockState());

            helper.startSequence()
                    .thenWaitUntil(() -> helper.assertTrue(fired.get() != null, "Expected the far chunk to be saved once unloaded"))
                    .thenExecute(() -> {
                        ChunkDataEvent.Save event = fired.get();
                        helper.assertTrue(event.getLevel() == level, "Expected the level of the chunk; level is " + event.getLevel());
                        helper.assertNotNull(event.getData(), "Expected the serialized data of the chunk");
                    })
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the LevelEvent.PotentialSpawns event can add spawns to what the natural spawner considers, and cancel them")
    static void potentialSpawnsEvent(final DynamicTest test) {
        final MobSpawnSettings.SpawnerData pigSpawn = new MobSpawnSettings.SpawnerData(EntityTypes.PIG, ConstantInt.of(1));
        AtomicReference<BlockPos> expectedPos = new AtomicReference<>();
        AtomicBoolean cancel = new AtomicBoolean(false);
        AtomicReference<LevelEvent.PotentialSpawns> fired = new AtomicReference<>();
        test.eventListeners().forge().addListener((final LevelEvent.PotentialSpawns event) -> {
            if (event.getMobCategory() == MobCategory.MONSTER && event.getPos().equals(expectedPos.get())) {
                fired.set(event);
                if (cancel.get()) {
                    event.setCanceled(true);
                } else {
                    event.addSpawnerData(new Weighted<>(pigSpawn, 10));
                }
            }
        });

        test.onGameTest(helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
            expectedPos.set(pos);
            // The natural spawner asks for the potential spawns of a position through this method, when picking a mob to spawn
            Method canSpawnMobAt = helper.catchException(() -> ObfuscationReflectionHelper.findMethod(NaturalSpawner.class, "canSpawnMobAt", ServerLevel.class, StructureManager.class, ChunkGenerator.class, MobCategory.class, MobSpawnSettings.SpawnerData.class, BlockPos.class));
            BooleanSupplier pigCanSpawn = () -> helper.catchException(() -> (boolean) canSpawnMobAt.invoke(null, level, level.structureManager(), level.getChunkSource().getGenerator(), MobCategory.MONSTER, pigSpawn, pos));

            boolean canSpawn = pigCanSpawn.getAsBoolean();
            LevelEvent.PotentialSpawns event = fired.get();
            helper.assertNotNull(event, "Event should fire when the natural spawner looks the potential spawns up");
            helper.assertTrue(event.getLevel() == level, "Expected the level of the spawn; level is " + event.getLevel());
            helper.assertTrue(event.getSpawnerDataList().stream().anyMatch(data -> data.value().equals(pigSpawn)), "Expected the added spawn in the list; list is " + event.getSpawnerDataList());
            // Pigs are never among the monster spawns of a biome, so the natural spawner can only accept this spawn through the event
            helper.assertTrue(canSpawn, "Expected the pig spawn added by the event to be a potential monster spawn");

            cancel.set(true);
            helper.assertFalse(pigCanSpawn.getAsBoolean(), "Expected no potential spawns when the event is cancelled");
            helper.succeed();
        });
    }

    private static final TicketController CHUNK_TICKETS = new TicketController(Identifier.fromNamespaceAndPath("neotests", "forced_chunk_tickets"));

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if a ticket controller registered through the RegisterTicketControllersEvent can force a chunk to be loaded, with or without natural spawning, and release it", enabledByDefault = true)
    static void forcedChunkTickets(final DynamicTest test) {
        // Controllers are registered during mod loading, so this test is enabled by default to have its listener in place
        test.eventListeners().mod().addListener((final RegisterTicketControllersEvent event) -> event.register(CHUNK_TICKETS));

        test.onGameTest(helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos owner = helper.absolutePos(BlockPos.ZERO);
            ChunkPos pos = farChunk(helper, FORCED_CHUNK_OFFSET);
            // The chunk map lets mobs spawn naturally in the chunks flagged here, even without a player nearby
            TicketStorage tickets = level.getChunkSource().getDataStorage().computeIfAbsent(TicketStorage.TYPE);
            helper.assertFalse(level.getChunkSource().hasChunk(pos.x(), pos.z()), "Expected the far chunk not to be loaded yet");

            helper.assertTrue(CHUNK_TICKETS.forceChunk(level, owner, pos.x(), pos.z(), true, false), "Expected the chunk to be forced");
            helper.assertTrue(ForcedChunkManager.hasForcedChunks(level), "Expected the level to have forced chunks");

            helper.startSequence()
                    .thenWaitUntil(() -> helper.assertTrue(level.getChunkSource().hasChunk(pos.x(), pos.z()), "Expected the forced chunk to be loaded"))
                    .thenExecute(() -> helper.assertFalse(tickets.shouldForceNaturalSpawning(pos), "Expected no natural spawning in a chunk forced without it"))
                    .thenExecute(() -> helper.assertTrue(CHUNK_TICKETS.forceChunk(level, owner, pos.x(), pos.z(), false, false), "Expected the chunk to be released"))
                    .thenWaitUntil(() -> helper.assertFalse(level.getChunkSource().hasChunk(pos.x(), pos.z()), "Expected the released chunk to be unloaded"))
                    // Forced again, with natural spawning this time
                    .thenExecute(() -> helper.assertTrue(CHUNK_TICKETS.forceChunk(level, owner, pos.x(), pos.z(), true, true), "Expected the chunk to be forced with natural spawning"))
                    .thenWaitUntil(() -> helper.assertTrue(level.getChunkSource().hasChunk(pos.x(), pos.z()), "Expected the chunk forced with natural spawning to be loaded"))
                    .thenExecute(() -> helper.assertTrue(tickets.shouldForceNaturalSpawning(pos), "Expected natural spawning in a chunk forced with it"))
                    .thenExecute(() -> helper.assertTrue(CHUNK_TICKETS.forceChunk(level, owner, pos.x(), pos.z(), false, true), "Expected the chunk forced with natural spawning to be released"))
                    .thenWaitUntil(() -> helper.assertFalse(level.getChunkSource().hasChunk(pos.x(), pos.z()), "Expected the released chunk to be unloaded again"))
                    .thenExecute(() -> helper.assertFalse(tickets.shouldForceNaturalSpawning(pos), "Expected no natural spawning once the chunk is released"))
                    .thenSucceed();
        });
    }
}
