package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

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
}
