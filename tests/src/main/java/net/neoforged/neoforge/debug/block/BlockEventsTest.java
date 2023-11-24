package net.neoforged.neoforge.debug.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.eventtest.internal.TestsMod;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(groups = BlockEventsTest.GROUP)
public class BlockEventsTest {
    public static final String GROUP = "level.block";

    @GameTest(template = TestsMod.TEMPLATE_3x3)
    @TestHolder(description = "Tests if the entity place event is fired")
    public static void onBlockEntityPlaced(final DynamicTest test) {
        test.whenEnabled(listeners -> listeners.forge()
                .addListener((final BlockEvent.EntityPlaceEvent event) -> {
                    if (event.getPlacedBlock().getBlock() == Blocks.CHEST && event.getPlacedAgainst().getBlock() != Blocks.DIAMOND_BLOCK) {
                        event.setCanceled(true);
                        test.pass();
                    }
                }));
        test.onGameTest(helper -> helper
                .startSequence()
                .thenExecute(() -> helper.setBlock(new BlockPos(1, 0, 1), Blocks.BAMBOO_BLOCK))
                .thenExecute(() -> new ItemStack(Items.CHEST).useOn(new UseOnContext(
                        helper.getLevel(), helper.makeMockPlayer(), InteractionHand.MAIN_HAND, new ItemStack(Items.CHEST),
                        new BlockHitResult(helper.absoluteVec(new Vec3(1, 1, 1)), Direction.UP, helper.absolutePos(new BlockPos(1, 1, 1)), false)
                )))
                .thenIdle(3)
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.CHEST, new BlockPos(1, 1, 1)))

                .thenIdle(3)

                .thenExecute(() -> helper.setBlock(new BlockPos(1, 0, 1), Blocks.DIAMOND_BLOCK))
                .thenExecute(() -> new ItemStack(Items.CHEST).useOn(new UseOnContext(
                        helper.getLevel(), helper.makeMockPlayer(), InteractionHand.MAIN_HAND, new ItemStack(Items.CHEST),
                        new BlockHitResult(helper.absoluteVec(new Vec3(1, 1, 1)), Direction.UP, helper.absolutePos(new BlockPos(1, 1, 1)), false)
                )))
                .thenIdle(3)
                .thenExecute(() -> helper.assertBlockPresent(Blocks.CHEST, new BlockPos(1, 1, 1)))

                .thenSucceed());
    }
}
