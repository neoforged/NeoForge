package net.neoforged.neoforge.common.extensions;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Function;

public interface IGameTestHelperExtension {
    GameTestHelper self();

    default void useOn(BlockPos pos, ItemStack item, Player player, Direction direction) {
        player.setItemInHand(InteractionHand.MAIN_HAND, item);
        pos = self().absolutePos(pos);
        item.useOn(new UseOnContext(
                self().getLevel(), player, InteractionHand.MAIN_HAND, item, new BlockHitResult(
                pos.getCenter(), direction, pos, false
        )
        ));
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
            throw new GameTestAssertException("Entity " + entity + " value " + valueName + "=" + value  + " is not equal to expected " + expected);
        }
    }

    default ServerPlayer makeTickingMockServerPlayerInLevel(GameType gameType) {
        CommonListenerCookie commonlistenercookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-mock-player"));
        ServerPlayer serverplayer = new ServerPlayer(
                self().getLevel().getServer(), self().getLevel(), commonlistenercookie.gameProfile(), commonlistenercookie.clientInformation()
        ) {
            @Override
            public boolean isSpectator() {
                return gameType == GameType.SPECTATOR;
            }

            @Override
            public boolean isCreative() {
                return gameType == GameType.CREATIVE;
            }
        };
        Connection connection = new Connection(PacketFlow.SERVERBOUND) {
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
        addListener(new GameTestListener() {
            @Override
            public void testStructureLoaded(GameTestInfo info) {

            }

            @Override
            public void testPassed(GameTestInfo p_177494_) {
                disconnect();
            }

            @Override
            public void testFailed(GameTestInfo p_127652_) {
                disconnect();
            }

            private void disconnect() {
                connection.disconnect(Component.literal("test finished"));
            }
        });
        return serverplayer;
    }

    void addListener(final GameTestListener listener);
}
