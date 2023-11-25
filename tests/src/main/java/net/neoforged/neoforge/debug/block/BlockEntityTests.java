package net.neoforged.neoforge.debug.block;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.eventtest.internal.TestsMod;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.registration.RegistrationHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.function.Supplier;

@ForEachTest(groups = BlockTests.GROUP + ".entity")
public class BlockEntityTests {
    private static final Logger LOGGER = LogUtils.getLogger();

    @TestHolder(description = {
            "Tests if the block entity load method is called"
    })
    @GameTest(template = TestsMod.TEMPLATE_3x3)
    static void blockEntityLoad(final DynamicTest test, final RegistrationHelper reg) {
        class TestBlockEntity extends BlockEntity {
            private boolean loaded = false;

            public TestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
                super(type, pos, state);
            }

            @Override
            public void onLoad() {
                LOGGER.info("[BE_ONLOAD] BlockEntity#onLoad at pos {} for {}", worldPosition, this);
                getLevel().setBlockAndUpdate(worldPosition.above(), Blocks.SAND.defaultBlockState());
                loaded = true;
            }

            private boolean first = true;

            public void tick() {
                if (first) {
                    first = false;
                    LOGGER.info("[BE_ONLOAD] TestBlockEntity#tick at pos {} for {}", worldPosition, this);
                    if (!loaded) {
                        throw new IllegalStateException(String.format(Locale.ENGLISH, "BlockEntity at %s ticked before onLoad()!", getBlockPos()));
                    }
                    test.pass();
                }
            }
        }

        class TestBlock extends Block implements EntityBlock {
            private final Supplier<BlockEntityType<TestBlockEntity>> type;

            TestBlock(Block.Properties properties, Supplier<BlockEntityType<TestBlockEntity>> type) {
                super(properties);
                this.type = type;
            }

            @Override
            @Nullable
            public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
                return new TestBlockEntity(type.get(), pos, state);
            }

            @Nullable
            @Override
            public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
                return (beLevel, bePos, beState, be) -> ((TestBlockEntity) be).tick();
            }
        }

        final var block = reg.blocks().registerBlockWithBEType("test_block", TestBlock::new, TestBlockEntity::new, BlockBehaviour.Properties.of())
                .withBlockItem().withDefaultWhiteModel().withLang("Test load block").withColor(0x67fafd);

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.setBlock(new BlockPos(1, 2, 1), block.get()))
                .thenExecuteAfter(5, () -> helper.assertTrue(((TestBlockEntity)helper.getBlockEntity(new BlockPos(1, 2, 1))).loaded, "BE wasn't loaded!"))
                .thenSucceed());
    }

}
