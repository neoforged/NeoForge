/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.model;

import com.mojang.math.Transformation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.common.util.TransformationHelper;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Mod(TRSRTransformerTest.MODID)
public class TRSRTransformerTest {
    public static final String MODID = "trsr_transformer_test";
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    private static final DeferredBlock<Block> TEST_BLOCK = BLOCKS.registerBlock("test", Block::new, Block.Properties.of().mapColor(MapColor.STONE));
    @SuppressWarnings("unused")
    private static final DeferredItem<BlockItem> TEST_ITEM = ITEMS.registerSimpleBlockItem(TEST_BLOCK);

    public TRSRTransformerTest(IEventBus modEventBus) {
        if (FMLEnvironment.getDist().isClient()) {
            modEventBus.addListener(TRSRTransformerTest::onModelBake);
        }
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(TRSRTransformerTest::addCreative);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(TEST_ITEM);
    }

    private static void onModelBake(ModelEvent.ModifyBakingResult e) {
        Map<BlockState, BlockStateModel> models = e.getBakingResult().blockStateModels();
        for (BlockState state : models.keySet()) {
            if (state.is(TEST_BLOCK)) {
                models.put(state, new MyBakedModel(models.get(state)));
            }
        }
    }

    private static class MyBakedModel extends DelegateBlockStateModel {
        private static final Direction[] DIRECTIONS = Arrays.copyOfRange(Direction.values(), 0, 7);
        private static final IQuadTransformer TRANSFORMER = QuadTransformers.applying(Util.make(() -> {
            Quaternionf rot = TransformationHelper.quatFromXYZ(new Vector3f(0, 45, 0), true);
            Vector3f translation = new Vector3f(0, 0.33f, 0);
            return new Transformation(translation, rot, null, null).blockCenterToCorner();
        }));

        public MyBakedModel(BlockStateModel base) {
            super(base);
        }

        @Override
        public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
            for (BlockModelPart part : delegate.collectParts(level, pos, state, random)) {
                QuadCollection.Builder builder = new QuadCollection.Builder();
                for (Direction side : DIRECTIONS) {
                    for (BakedQuad quad : part.getQuads(side)) {
                        quad = TRANSFORMER.process(quad);
                        if (side == null) {
                            builder.addUnculledFace(quad);
                        } else {
                            builder.addCulledFace(side, quad);
                        }
                    }
                }
                parts.add(new SimpleModelWrapper(builder.build(), part.useAmbientOcclusion(), part.particleIcon(), part.getRenderType(state)));
            }
        }
    }
}
