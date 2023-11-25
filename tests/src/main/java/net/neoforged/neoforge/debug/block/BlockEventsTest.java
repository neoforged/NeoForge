package net.neoforged.neoforge.debug.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ToolActions;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.neoforge.eventtest.internal.TestsMod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;

import java.util.Objects;

@ForEachTest(groups = BlockEventsTest.GROUP)
public class BlockEventsTest {
    public static final String GROUP = "level.block";

    @GameTest(template = TestsMod.TEMPLATE_3x3)
    @TestHolder(description = "Tests if the entity place event is fired")
    public static void entityPlacedEvent(final DynamicTest test) {
        test.whenEnabled(listeners -> listeners.forge()
                .addListener((final BlockEvent.EntityPlaceEvent event) -> {
                    if (event.getPlacedBlock().getBlock() == Blocks.CHEST && event.getPlacedAgainst().getBlock() != Blocks.DIAMOND_BLOCK) {
                        event.setCanceled(true);
                    }
                    test.pass();
                }));
        test.onGameTest(helper -> helper
                .startSequence()
                .thenExecute(() -> helper.setBlock(new BlockPos(1, 1, 1), Blocks.BAMBOO_BLOCK))
                .thenExecute(() -> new ItemStack(Items.CHEST).useOn(new UseOnContext(
                        helper.getLevel(), helper.makeMockPlayer(), InteractionHand.MAIN_HAND, new ItemStack(Items.CHEST),
                        new BlockHitResult(helper.absoluteVec(new Vec3(1, 2, 1)), Direction.UP, helper.absolutePos(new BlockPos(1, 2, 1)), false)
                )))
                .thenIdle(3)
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.CHEST, new BlockPos(1, 1, 1)))

                .thenIdle(3)

                .thenExecute(() -> helper.setBlock(new BlockPos(1, 1, 1), Blocks.DIAMOND_BLOCK))
                .thenExecute(() -> new ItemStack(Items.CHEST).useOn(new UseOnContext(
                        helper.getLevel(), helper.makeMockPlayer(), InteractionHand.MAIN_HAND, new ItemStack(Items.CHEST),
                        new BlockHitResult(helper.absoluteVec(new Vec3(1, 2, 1)), Direction.UP, helper.absolutePos(new BlockPos(1, 2, 1)), false)
                )))
                .thenIdle(3)
                .thenExecute(() -> helper.assertBlockPresent(Blocks.CHEST, new BlockPos(1, 2, 1)))
                .thenSucceed());
    }

    @GameTest(template = TestsMod.TEMPLATE_3x3)
    @TestHolder(description = "Tests if the block modification event is fired")
    public static void blockModificationEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final BlockEvent.BlockToolModificationEvent event) -> {
            if (event.getToolAction() == ToolActions.AXE_STRIP) {
                if (event.getLevel().getBlockState(event.getContext().getClickedPos()).is(Blocks.ACACIA_LOG)) {
                    event.setCanceled(true);
                } else if (event.getFinalState().is(Blocks.DIAMOND_BLOCK) && event.getContext().getClickedFace() == Direction.UP) {
                    event.setFinalState(Blocks.EMERALD_BLOCK.defaultBlockState());
                }
            }
            test.pass();
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.setBlock(new BlockPos(1, 1, 1), Blocks.ACACIA_LOG))
                .thenExecute(() -> helper.useOn(new BlockPos(1, 1, 1), Items.DIAMOND_AXE.getDefaultInstance(), helper.makeMockPlayer(), Direction.UP))
                .thenExecuteAfter(1, () -> helper.assertBlockPresent(Blocks.ACACIA_LOG, new BlockPos(1, 1, 1)))

                .thenIdle(3)

                .thenExecute(() -> helper.setBlock(new BlockPos(1, 2, 1), Blocks.DIAMOND_BLOCK))
                .thenExecute(() -> helper.useOn(new BlockPos(1, 2, 1), Items.DIAMOND_AXE.getDefaultInstance(), helper.makeMockPlayer(), Direction.UP))
                .thenExecuteAfter(1, () -> helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(1, 2, 1)))
                .thenSucceed());
    }

    @GameTest(template = TestsMod.TEMPLATE_3x3_FLOOR)
    @TestHolder(description = "Tests if the neighbor notify event is fired")
    public static void neighborNotifyEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final BlockEvent.NeighborNotifyEvent event) -> {
            if (event.getState().getBlock() == Blocks.COMPARATOR) {
                event.setCanceled(true);
            }
            test.pass();
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.setBlock(new BlockPos(1, 2, 0), Blocks.COMPOSTER))
                .thenExecute(() -> helper.setBlock(new BlockPos(1, 2, 1), Blocks.COMPARATOR))
                .thenExecute(() -> helper.setBlock(new BlockPos(1, 2, 2), Blocks.REDSTONE_LAMP))

                .thenExecute(() -> helper.useBlock(new BlockPos(1, 2, 0), helper.makeMockPlayer(), Items.ACACIA_LEAVES.getDefaultInstance()))
                .thenExecuteAfter(5, () -> helper.assertBlockProperty(new BlockPos(1, 2, 2), RedstoneLampBlock.LIT, false)) // We haven't triggered a neighbour update (yet)

                .thenExecuteAfter(1, () -> helper.getLevel().setBlock(helper.absolutePos(new BlockPos(1, 3, 2)), Blocks.IRON_BLOCK.defaultBlockState(), 11)) // Now we should trigger an update
                .thenExecuteAfter(5, () -> helper.assertBlockProperty(new BlockPos(1, 2, 2), RedstoneLampBlock.LIT, true))
                .thenSucceed());
    }

    @GameTest(template = "neotests:farmland_trample", timeoutTicks = 150)
    @TestHolder(description = "Tests if the farmland trample event is fired")
    public static void farmlandTrampleEvent(final DynamicTest test) {
        test.registerGameTestTemplate(StructureTemplateBuilder.withSize(3, 4, 3)
                .placeSustainedWater(1, 1, 1, Blocks.FARMLAND.defaultBlockState()));

        test.eventListeners().forge().addListener((final BlockEvent.FarmlandTrampleEvent event) -> {
            if (event.getEntity().getType() != EntityType.GOAT) {
                event.setCanceled(true);
            }
            test.pass();
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.spawnWithNoFreeWill(EntityType.SHEEP, new BlockPos(0, 5, 1).getCenter()))
                .thenExecuteAfter(40, () -> helper.assertBlockPresent(Blocks.FARMLAND, new BlockPos(0, 2, 1)))
                .thenExecute(() -> helper.killAllEntitiesOfClass(Sheep.class))
                .thenIdle(20)

                .thenExecute(() -> helper.spawnWithNoFreeWill(EntityType.GOAT, new BlockPos(1, 5, 0).getCenter()))
                .thenExecuteAfter(40, () -> helper.assertBlockPresent(Blocks.DIRT, new BlockPos(1, 2, 0)))
                .thenExecute(() -> helper.killAllEntitiesOfClass(Goat.class))
                .thenSucceed());
    }

    @TestHolder(description = {
            "This test blocks pistons from moving cobblestone at all except indirectly.",
            "This test adds a block that moves upwards when pushed by a piston.",
            "This test mod makes black wool pushed by a piston drop after being pushed."
    })
    @GameTest(template = "neotests:piston_event")
    static void pistonEvent(final DynamicTest test) {
        final DeferredRegister.Blocks blocks = DeferredRegister.createBlocks("neotests_piston_event");
        blocks.register(test.framework().modEventBus());
        final var shiftOnPistonMove = blocks.registerBlock("shift_on_piston_move", BlockBehaviour.Properties.of());

        test.registerGameTestTemplate(() -> StructureTemplateBuilder.withSize(3, 5, 3)
                .placeFloorLever(1, 1, 1, false)
                .set(1, 0, 2, Blocks.PISTON.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.UP))
                .set(1, 1, 2, Blocks.BLACK_WOOL.defaultBlockState())
                .set(1, 2, 2, shiftOnPistonMove.get().defaultBlockState())

                .set(2, 0, 1, Blocks.STICKY_PISTON.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.UP))
                .set(2, 2, 1, Blocks.COBBLESTONE.defaultBlockState())

                .set(1, 0, 0, Blocks.PISTON.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.UP))
                .set(1, 1, 0, Blocks.COBBLESTONE.defaultBlockState()));

        test.eventListeners().forge().addListener((final PistonEvent.Pre event) -> {
            if (!(event.getLevel() instanceof Level level)) return;

            if (event.getPistonMoveType() == PistonEvent.PistonMoveType.EXTEND) {
                final PistonStructureResolver pistonHelper = Objects.requireNonNull(event.getStructureHelper());

                if (pistonHelper.resolve()) {
                    for (BlockPos newPos : pistonHelper.getToPush()) {
                        final BlockState state = event.getLevel().getBlockState(newPos);
                        if (state.getBlock() == Blocks.BLACK_WOOL) {
                            Block.dropResources(state, level, newPos);
                            level.setBlockAndUpdate(newPos, Blocks.AIR.defaultBlockState());
                        }
                    }
                }

                // Make the block move up and out of the way so long as it won't replace the piston
                final BlockPos pushedBlockPos = event.getFaceOffsetPos().relative(event.getDirection());
                if (level.getBlockState(pushedBlockPos).is(shiftOnPistonMove.get()) && event.getDirection() != Direction.DOWN) {
                    level.setBlockAndUpdate(pushedBlockPos, Blocks.AIR.defaultBlockState());
                    level.setBlockAndUpdate(pushedBlockPos.above(), shiftOnPistonMove.get().defaultBlockState());
                }

                // Block pushing cobblestone (directly, indirectly works)
                event.setCanceled(event.getLevel().getBlockState(event.getFaceOffsetPos()).getBlock() == Blocks.COBBLESTONE);
            } else {
                final boolean isSticky = event.getLevel().getBlockState(event.getPos()).getBlock() == Blocks.STICKY_PISTON;

                // Offset twice to see if retraction will pull cobblestone
                event.setCanceled(event.getLevel().getBlockState(event.getFaceOffsetPos().relative(event.getDirection())).getBlock() == Blocks.COBBLESTONE && isSticky);
            }
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.pullLever(1, 2, 1))
                .thenIdle(10)

                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.PISTON_HEAD, 1, 2, 2)) // The piston should've extended
                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.AIR, 1, 3, 2)) // This is where the shift block WOULD be
                .thenWaitUntil(0, () -> helper.assertBlockPresent(shiftOnPistonMove.get(), 1, 4, 2)) // Shift block should move upwards

                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.COBBLESTONE, 1, 2, 0))

                .thenIdle(20)
                .thenExecute(() -> helper.pullLever(1, 2, 1))
                .thenIdle(10)
                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.COBBLESTONE, 2, 3, 1))
                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.PISTON_HEAD, 2, 2, 1))

                .thenExecute(test::pass)
                .thenSucceed());
    }
}
