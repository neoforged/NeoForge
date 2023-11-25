package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

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
}
