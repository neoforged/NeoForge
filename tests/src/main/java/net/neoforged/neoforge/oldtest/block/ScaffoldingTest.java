/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.block;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * This test mod allows a custom scaffolding to move down
 * while sneaking through a method.
 */
@Mod(ScaffoldingTest.MODID)
public class ScaffoldingTest {
    static final String MODID = "scaffolding_test";
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    static final TagKey<Block> SCAFFOLDING = BlockTags.create(ResourceLocation.fromNamespaceAndPath("neoforge", "scaffolding"));

    static final DeferredBlock<Block> SCAFFOLDING_METHOD_TEST = BLOCKS.registerBlock("scaffolding_method_test", ScaffoldingTest.ScaffoldingMethodTestBlock::new, Properties.of().mapColor(MapColor.SAND).noCollision().sound(SoundType.SCAFFOLDING).dynamicShape());
    static final DeferredItem<BlockItem> SCAFFOLDING_METHOD_TEST_ITEM = ITEMS.registerSimpleBlockItem(SCAFFOLDING_METHOD_TEST);

    public ScaffoldingTest(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(this::gatherData);
    }

    private void gatherData(final GatherDataEvent.Client event) {
        DataGenerator gen = event.getGenerator();
        event.addProvider(new ScaffoldingModels(gen.getPackOutput()));
    }

    private static final class ScaffoldingModels extends ModelProvider {
        public ScaffoldingModels(PackOutput output) {
            super(output, MODID);
        }

        @Override
        protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
            var stableModel = ModelLocationUtils.getModelLocation(SCAFFOLDING_METHOD_TEST.value(), "_stable");
            var unstableModel = ModelLocationUtils.getModelLocation(SCAFFOLDING_METHOD_TEST.value(), "_unstable");
            blockModels.registerSimpleItemModel(SCAFFOLDING_METHOD_TEST.value(), stableModel);
            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(SCAFFOLDING_METHOD_TEST.value()).with(BlockModelGenerators.createBooleanModelDispatch(BlockStateProperties.BOTTOM, BlockModelGenerators.plainVariant(unstableModel), BlockModelGenerators.plainVariant(stableModel))));
        }
    }

    static class ScaffoldingMethodTestBlock extends ScaffoldingBlock {
        public ScaffoldingMethodTestBlock(Properties properties) {
            super(properties);
        }

        @Override
        public boolean isScaffolding(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
            return true;
        }
    }
}
