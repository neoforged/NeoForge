/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.gametest;

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
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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
        };
        EmbeddedChannel embeddedchannel = new EmbeddedChannel(connection);
        embeddedchannel.attr(Connection.ATTRIBUTE_SERVERBOUND_PROTOCOL).set(ConnectionProtocol.PLAY.codec(PacketFlow.SERVERBOUND));
        this.getLevel().getServer().getPlayerList().placeNewPlayer(connection, serverplayer, commonlistenercookie);
        this.getLevel().getServer().getConnection().getConnections().add(connection);
        this.testInfo.addListener(serverplayer);
        serverplayer.gameMode.changeGameModeForPlayer(gameType);
        return serverplayer;
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
}
