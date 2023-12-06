/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.block;

import net.minecraft.core.BlockPos;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * This test mod allows a custom scaffolding to move down
 * while sneaking through a method.
 */
@Mod(ScaffoldingTest.MODID)
public class ScaffoldingTest {
    static final String MODID = "scaffolding_test";
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    static final TagKey<Block> SCAFFOLDING = BlockTags.create(new ResourceLocation("neoforge", "scaffolding"));

    static final DeferredBlock<Block> SCAFFOLDING_METHOD_TEST = BLOCKS.register("scaffolding_method_test", () -> new ScaffoldingMethodTestBlock(Properties.of().mapColor(MapColor.SAND).noCollission().sound(SoundType.SCAFFOLDING).dynamicShape()));

    public ScaffoldingTest() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modBus);
        modBus.addListener(this::gatherData);
    }

    private void gatherData(final GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        gen.addProvider(event.includeClient(), new ScaffoldingBlockState(gen.getPackOutput(), MODID, event.getExistingFileHelper()));
    }

    static class ScaffoldingBlockState extends BlockStateProvider {
        public ScaffoldingBlockState(PackOutput output, String modid, ExistingFileHelper exFileHelper) {
            super(output, modid, exFileHelper);
        }

        @Override
        protected void registerStatesAndModels() {
            this.getVariantBuilder(SCAFFOLDING_METHOD_TEST.get()).forAllStatesExcept((state) -> ConfiguredModel.builder().modelFile(state.getValue(ScaffoldingBlock.BOTTOM) ? new ModelFile.ExistingModelFile(new ResourceLocation("block/scaffolding_unstable"), this.models().existingFileHelper) : new ModelFile.ExistingModelFile(new ResourceLocation("block/scaffolding_stable"), this.models().existingFileHelper)).build(), ScaffoldingBlock.DISTANCE, ScaffoldingBlock.WATERLOGGED);
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
