/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.gametest;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.jetbrains.annotations.Nullable;

public class ExtendedGameTestHelper extends GameTestHelper {
    public ExtendedGameTestHelper(GameTestInfo info) {
        super(info);
    }

    @Override
    public ExtendedSequence startSequence() {
        final var sq = new ExtendedSequence(testInfo);
        testInfo.sequences.add(sq);
        return sq;
    }

    public void useOn(BlockPos pos, ItemStack item, Player player, Direction direction) {
        player.setItemInHand(InteractionHand.MAIN_HAND, item);
        pos = this.absolutePos(pos);
        item.useOn(new UseOnContext(
                this.getLevel(), player, InteractionHand.MAIN_HAND, item, new BlockHitResult(
                        pos.getCenter(), direction, pos, false)));
    }

    public void useBlock(BlockPos pos, Player player, ItemStack item) {
        player.setItemInHand(InteractionHand.MAIN_HAND, item);
        this.useBlock(pos, player);
    }

    public void useBlock(BlockPos pos, Player player, ItemStack item, Direction direction) {
        player.setItemInHand(InteractionHand.MAIN_HAND, item);

        BlockPos blockpos = this.absolutePos(pos);
        BlockState blockstate = this.getLevel().getBlockState(blockpos);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(blockpos), direction, blockpos, true);
        InteractionResult interactionresult = blockstate.use(this.getLevel(), player, InteractionHand.MAIN_HAND, hit);
        if (!interactionresult.consumesAction()) {
            UseOnContext useoncontext = new UseOnContext(player, InteractionHand.MAIN_HAND, hit);
            player.getItemInHand(InteractionHand.MAIN_HAND).useOn(useoncontext);
        }
    }

    public <T, E extends Entity> void assertEntityProperty(E entity, Function<E, T> function, String valueName, T expected, BiPredicate<T, T> tester) {
        final T value = function.apply(entity);
        if (!tester.test(value, expected)) {
            throw new GameTestAssertException("Entity " + entity + " value " + valueName + "=" + value + " is not equal to expected " + expected);
        }
    }

    public GameTestPlayer makeTickingMockServerPlayerInCorner(GameType gameType) {
        return makeTickingMockServerPlayerInLevel(gameType).moveToCorner();
    }

    public GameTestPlayer makeTickingMockServerPlayerInLevel(GameType gameType) {
        final CommonListenerCookie commonlistenercookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-mock-player"));
        final GameTestPlayer serverplayer = new GameTestPlayer(this.getLevel().getServer(), this.getLevel(), commonlistenercookie.gameProfile(), commonlistenercookie.clientInformation(), this);
        final Connection connection = new Connection(PacketFlow.SERVERBOUND) {
            @Override
            public void tick() {
                super.tick();
                serverplayer.resetLastActionTime();
            }

            @Override
            public boolean isMemoryConnection() {
                return true;
            }
        };
        EmbeddedChannel embeddedchannel = new EmbeddedChannel(connection);
        embeddedchannel.attr(Connection.ATTRIBUTE_SERVERBOUND_PROTOCOL).set(ConnectionProtocol.PLAY.codec(PacketFlow.SERVERBOUND));
        embeddedchannel.attr(Connection.ATTRIBUTE_CLIENTBOUND_PROTOCOL).set(ConnectionProtocol.PLAY.codec(PacketFlow.CLIENTBOUND));
        NetworkRegistry.getInstance().configureMockConnection(connection);
        this.getLevel().getServer().getPlayerList().placeNewPlayer(connection, serverplayer, commonlistenercookie);
        this.getLevel().getServer().getConnection().getConnections().add(connection);
        this.testInfo.addListener(serverplayer);
        serverplayer.gameMode.changeGameModeForPlayer(gameType);
        serverplayer.setYRot(180);
        serverplayer.connection.chunkSender.sendNextChunks(serverplayer);
        serverplayer.connection.chunkSender.onChunkBatchReceivedByClient(64f);
        return serverplayer;
    }

    public Player makeOpMockPlayer(int commandLevel) {
        return new Player(this.getLevel(), BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "test-mock-player")) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return true;
            }

            @Override
            public boolean isLocalPlayer() {
                return true;
            }

            @Override
            protected int getPermissionLevel() {
                return commandLevel;
            }
        };
    }

    public Stream<BlockPos> blocksBetween(int x, int y, int z, int length, int height, int width) {
        final AABB bounds = AABB.encapsulatingFullBlocks(this.absolutePos(new BlockPos(x, y, z)), this.absolutePos(new BlockPos(x + length, y + height, z + width)));
        return BlockPos.MutableBlockPos.betweenClosedStream(bounds);
    }

    @Nullable
    public <T extends BlockEntity> T getBlockEntity(BlockPos pos, Class<T> type) {
        final var be = this.getBlockEntity(pos);
        if (be == null) return null;
        if (!type.isInstance(be)) {
            throw new GameTestAssertPosException("Expected block entity of type " + type + " but was " + be.getClass(), this.absolutePos(pos), pos, this.getTick());
        }
        return type.cast(be);
    }

    @Nullable
    public <T extends BlockEntity> T getBlockEntity(int x, int y, int z, Class<T> type) {
        return getBlockEntity(new BlockPos(x, y, z), type);
    }

    public <T extends BlockEntity> T requireBlockEntity(BlockPos pos, Class<T> type) {
        final var be = getBlockEntity(pos, type);
        if (be == null) {
            throw new GameTestAssertPosException("Expected block entity of type " + type + " but there was none", this.absolutePos(pos), pos, this.getTick());
        }
        return be;
    }

    public <T extends BlockEntity> T requireBlockEntity(int x, int y, int z, Class<T> type) {
        return requireBlockEntity(new BlockPos(x, y, z), type);
    }

    public <T> ParametrizedGameTestSequence<T> startSequence(Supplier<T> value) {
        return new ParametrizedGameTestSequence<>(this.testInfo, this.startSequence(), value);
    }

    public void killAllEntitiesOfClass(Class<?>... types) {
        for (Class<?> type : types) {
            this.killAllEntitiesOfClass(type);
        }
    }

    public void assertItemEntityCountIsAtLeast(Item item, BlockPos pos, double range, int lowerLimit) {
        final BlockPos blockpos = this.absolutePos(pos);
        final List<ItemEntity> list = this.getLevel().getEntities(EntityType.ITEM, new AABB(blockpos).inflate(range), Entity::isAlive);
        int count = 0;

        for (final ItemEntity itementity : list) {
            ItemStack itemstack = itementity.getItem();
            if (itemstack.is(item)) {
                count += itemstack.getCount();
            }
        }

        if (count < lowerLimit) {
            throw new GameTestAssertPosException(
                    "Expected at least " + lowerLimit + " " + item.getDescription().getString() + " items to exist (found " + count + ")",
                    blockpos,
                    pos,
                    this.getTick());
        }
    }

    public void boneMeal(BlockPos pos, Player player) {
        useOn(pos, Items.BONE_MEAL.getDefaultInstance(), player, Direction.UP);
    }

    public void boneMeal(int x, int y, int z, Player player) {
        boneMeal(new BlockPos(x, y, z), player);
    }

    /**
     * To be used alongside {@link net.minecraft.gametest.framework.GameTestSequence#thenWaitUntil(Runnable)}
     */
    public void boneMealUntilGrown(int x, int y, int z, Player player) {
        boneMeal(x, y, z, player);
        assertBlockState(new BlockPos(x, y, z), state -> !(state.getBlock() instanceof BonemealableBlock), () -> "Crop didn't grow");
    }

    public void assertContainerEmpty(int x, int y, int z) {
        assertContainerEmpty(new BlockPos(x, y, z));
    }

    public void assertContainerContains(int x, int y, int z, Item item) {
        assertContainerContains(new BlockPos(x, y, z), item);
    }

    public void pulseRedstone(int x, int y, int z, long delay) {
        pulseRedstone(new BlockPos(x, y, z), delay);
    }

    public void assertPlayerHasItem(Player player, Item item) {
        assertTrue(player.getInventory().hasAnyOf(Set.of(item)), "Player doesn't have '" + BuiltInRegistries.ITEM.getKey(item) + "' in their inventory!");
    }

    public void requireDifficulty(final Difficulty difficulty) {
        final var oldDifficulty = getLevel().getServer().getWorldData().getDifficulty();
        if (oldDifficulty != difficulty) {
            getLevel().getServer().setDifficulty(difficulty, true);
            addEndListener(passed -> getLevel().getServer().setDifficulty(oldDifficulty, true));
        }
    }

    public void addEndListener(Consumer<Boolean> listener) {
        testInfo.addListener(new GameTestListener() {
            @Override
            public void testStructureLoaded(GameTestInfo info) {}

            @Override
            public void testPassed(GameTestInfo info) {
                listener.accept(true);
            }

            @Override
            public void testFailed(GameTestInfo info) {
                listener.accept(false);
            }
        });
    }

    public <T> T catchException(final ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable throwable) {
            throw new GameTestAssertException(throwable.getMessage());
        }
    }

    public void catchException(final ThrowingRunnable run) {
        try {
            run.run();
        } catch (Throwable throwable) {
            throw new GameTestAssertException(throwable.getMessage());
        }
    }

    public <T extends Entity> T requireEntityAt(EntityType<T> type, int x, int y, int z) {
        return requireEntityAt(type, new BlockPos(x, y, z));
    }

    public <T extends Entity> T requireEntityAt(EntityType<T> type, BlockPos pos) {
        final var inRange = getEntities(type, pos, 2);
        assertTrue(inRange.size() == 1, "Only one entity must be present at " + pos);
        return inRange.get(0);
    }

    @CanIgnoreReturnValue
    public <T extends Entity> T knockbackResistant(T entity) {
        addTemporaryListener((final LivingKnockBackEvent event) -> {
            if (event.getEntity().getUUID().equals(entity.getUUID())) {
                event.setCanceled(true);
            }
        });
        return entity;
    }

    public <T extends Event> void addTemporaryListener(Consumer<T> event) {
        NeoForge.EVENT_BUS.addListener(event);
        addEndListener(success -> NeoForge.EVENT_BUS.unregister(event));
    }

    public <E extends LivingEntity> void assertMobEffectPresent(E entity, MobEffect effect, String testName) {
        assertEntityProperty(entity, e -> e.hasEffect(effect), testName);
    }

    public <E extends LivingEntity> void assertMobEffectAbsent(E entity, MobEffect effect, String testName) {
        assertEntityProperty(entity, e -> !e.hasEffect(effect), testName);
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
