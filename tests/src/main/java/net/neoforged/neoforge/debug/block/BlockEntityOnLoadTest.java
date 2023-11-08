/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.block;

import com.mojang.logging.LogUtils;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Mod("be_onload_test")
public class BlockEntityOnLoadTest {
    private static final boolean ENABLED = true;
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, "be_onload_test");
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, "be_onload_test");
    private static final DeferredRegister<BlockEntityType<?>> BE_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "be_onload_test");

    private static final Holder<Block> TEST_BLOCK = BLOCKS.register("be_onload_testblock", () -> new TestBlock(Properties.of().mapColor(MapColor.SAND)));
    private static final Holder<Item> TEST_BLOCK_ITEM = ITEMS.register("be_onload_testblock", () -> new BlockItem(TEST_BLOCK.get(), new Item.Properties()));
    private static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TestBlockEntity>> TEST_BE_TYPE = BE_TYPES.register("be_onload_testbe", () -> BlockEntityType.Builder.of(TestBlockEntity::new, TEST_BLOCK.get()).build(null));

    public BlockEntityOnLoadTest() {
        if (ENABLED) {
            IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
            BLOCKS.register(modBus);
            ITEMS.register(modBus);
            BE_TYPES.register(modBus);
            modBus.addListener(this::addCreative);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(TEST_BLOCK_ITEM);
    }

    private static class TestBlock extends Block implements EntityBlock {
        public TestBlock(Properties props) {
            super(props);
        }

        @Override
        public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            LOGGER.info("[BE_ONLOAD] Block#use at pos {} for {}", pos, level.getBlockEntity(pos));
            return super.use(state, level, pos, player, hand, hit);
        }

        @Override
        @Nullable
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new TestBlockEntity(pos, state);
        }

        @Nullable
        @Override
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
            return (beLevel, bePos, beState, be) -> ((TestBlockEntity) be).tick();
        }
    }

    private static class TestBlockEntity extends BlockEntity {
        private boolean loaded = false;

        public TestBlockEntity(BlockPos pos, BlockState state) {
            super(TEST_BE_TYPE.get(), pos, state);
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
            }
        }
    }
}
