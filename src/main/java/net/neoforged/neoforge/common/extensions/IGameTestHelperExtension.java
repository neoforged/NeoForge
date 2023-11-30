/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;

import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestPlayer;
import net.neoforged.neoforge.gametest.ParametrizedGameTestSequence;
import org.jetbrains.annotations.Nullable;

public interface IGameTestHelperExtension {
    GameTestHelper self();

    default void useOn(BlockPos pos, ItemStack item, Player player, Direction direction) {
        player.setItemInHand(InteractionHand.MAIN_HAND, item);
        pos = self().absolutePos(pos);
        item.useOn(new UseOnContext(
                self().getLevel(), player, InteractionHand.MAIN_HAND, item, new BlockHitResult(
                        pos.getCenter(), direction, pos, false)));
    }

    default void useBlock(BlockPos pos, Player player, ItemStack item) {
        player.setItemInHand(InteractionHand.MAIN_HAND, item);
        self().useBlock(pos, player);
    }

    default void useBlock(BlockPos pos, Player player, ItemStack item, Direction direction) {
        player.setItemInHand(InteractionHand.MAIN_HAND, item);

        BlockPos blockpos = self().absolutePos(pos);
        BlockState blockstate = self().getLevel().getBlockState(blockpos);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(blockpos), direction, blockpos, true);
        InteractionResult interactionresult = blockstate.use(self().getLevel(), player, InteractionHand.MAIN_HAND, hit);
        if (!interactionresult.consumesAction()) {
            UseOnContext useoncontext = new UseOnContext(player, InteractionHand.MAIN_HAND, hit);
            player.getItemInHand(InteractionHand.MAIN_HAND).useOn(useoncontext);
        }
    }

    default <T, E extends Entity> void assertEntityProperty(E entity, Function<E, T> function, String valueName, T expected, BiPredicate<T, T> tester) {
        final T value = function.apply(entity);
        if (!tester.test(value, expected)) {
            throw new GameTestAssertException("Entity " + entity + " value " + valueName + "=" + value + " is not equal to expected " + expected);
        }
    }

    default GameTestPlayer makeTickingMockServerPlayerInCorner(GameType gameType) {
        return makeTickingMockServerPlayerInLevel(gameType).moveToCorner();
    }

    default GameTestPlayer makeTickingMockServerPlayerInLevel(GameType gameType) {
        final CommonListenerCookie commonlistenercookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-mock-player"));
        final GameTestPlayer serverplayer = new GameTestPlayer(self().getLevel().getServer(), self().getLevel(), commonlistenercookie.gameProfile(), commonlistenercookie.clientInformation(), self());
        final Connection connection = new Connection(PacketFlow.SERVERBOUND) {
            @Override
            public void tick() {
                super.tick();
                serverplayer.resetLastActionTime();
            }
        };
        EmbeddedChannel embeddedchannel = new EmbeddedChannel(connection);
        embeddedchannel.attr(Connection.ATTRIBUTE_SERVERBOUND_PROTOCOL).set(ConnectionProtocol.PLAY.codec(PacketFlow.SERVERBOUND));
        self().getLevel().getServer().getPlayerList().placeNewPlayer(connection, serverplayer, commonlistenercookie);
        self().getLevel().getServer().getConnection().getConnections().add(connection);
        self().testInfo.addListener(serverplayer);
        serverplayer.gameMode.changeGameModeForPlayer(gameType);
        return serverplayer;
    }

    default Stream<BlockPos> blocksBetween(int x, int y, int z, int length, int height, int width) {
        final AABB bounds = new AABB(self().absolutePos(new BlockPos(x, y, z)), self().absolutePos(new BlockPos(x + length, y + height, z + width)));
        return BlockPos.MutableBlockPos.betweenClosedStream(bounds);
    }

    @Nullable
    default <T extends BlockEntity> T getBlockEntity(BlockPos pos, Class<T> type) {
        final var be = self().getBlockEntity(pos);
        if (be == null) return null;
        if (!type.isInstance(be)) {
            throw new GameTestAssertPosException("Expected block entity of type " + type + " but was " + be.getClass(), this.self().absolutePos(pos), pos, self().getTick());
        }
        return type.cast(be);
    }

    @Nullable
    default <T extends BlockEntity> T getBlockEntity(int x, int y, int z, Class<T> type) {
        return getBlockEntity(new BlockPos(x, y, z), type);
    }

    default <T extends BlockEntity> T requireBlockEntity(BlockPos pos, Class<T> type) {
        final var be = getBlockEntity(pos, type);
        if (be == null) {
            throw new GameTestAssertPosException("Expected block entity of type " + type + " but there was none", this.self().absolutePos(pos), pos, self().getTick());
        }
        return be;
    }

    default <T extends BlockEntity> T requireBlockEntity(int x, int y, int z, Class<T> type) {
        return requireBlockEntity(new BlockPos(x, y, z), type);
    }

    default <T> ParametrizedGameTestSequence<T> startSequence(Supplier<T> value) {
        return new ParametrizedGameTestSequence<>(self().testInfo, self().startSequence(), value);
    }

    default void killAllEntitiesOfClass(Class<?>... types) {
        for (Class<?> type : types) {
            self().killAllEntitiesOfClass(type);
        }
    }

    default void assertItemEntityCountIsAtLeast(Item item, BlockPos pos, double range, int lowerLimit) {
        final BlockPos blockpos = self().absolutePos(pos);
        final List<ItemEntity> list = self().getLevel().getEntities(EntityType.ITEM, new AABB(blockpos).inflate(range), Entity::isAlive);
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
                    self().getTick()
            );
        }
    }
}
