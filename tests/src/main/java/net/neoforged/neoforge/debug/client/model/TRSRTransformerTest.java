/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client.model;

import com.mojang.math.Transformation;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.DistExecutor;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.util.TransformationHelper;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Mod(TRSRTransformerTest.MODID)
public class TRSRTransformerTest {
    public static final String MODID = "trsr_transformer_test";
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    private static final DeferredBlock<Block> TEST_BLOCK = BLOCKS.register("test", () -> new Block(Block.Properties.of().mapColor(MapColor.STONE)));
    @SuppressWarnings("unused")
    private static final DeferredItem<Item> TEST_ITEM = ITEMS.register("test", () -> new BlockItem(TEST_BLOCK.get(), new Item.Properties()));

    public TRSRTransformerTest() {
        final IEventBus mod = FMLJavaModLoadingContext.get().getModEventBus();
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> mod.addListener(this::onModelBake));
        BLOCKS.register(mod);
        ITEMS.register(mod);
        mod.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(TEST_ITEM);
    }

    public void onModelBake(ModelEvent.ModifyBakingResult e) {
        for (ResourceLocation id : e.getModels().keySet()) {
            if (MODID.equals(id.getNamespace()) && "test".equals(id.getPath())) {
                e.getModels().put(id, new MyBakedModel(e.getModels().get(id)));
            }
        }
    }

    public class MyBakedModel implements IDynamicBakedModel {
        private final BakedModel base;

        public MyBakedModel(BakedModel base) {
            this.base = base;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
            Quaternionf rot = TransformationHelper.quatFromXYZ(new Vector3f(0, 45, 0), true);
            Vector3f translation = new Vector3f(0, 0.33f, 0);

            Transformation trans = new Transformation(translation, rot, null, null).blockCenterToCorner();
            var transformer = QuadTransformers.applying(trans);

            return transformer.process(base.getQuads(state, side, rand, data, renderType));
        }

        @Override
        public boolean useAmbientOcclusion() {
            return base.useAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return base.isGui3d();
        }

        @Override
        public boolean usesBlockLight() {
            return base.usesBlockLight();
        }

        @Override
        public boolean isCustomRenderer() {
            return base.isCustomRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return base.getParticleIcon();
        }

        @Override
        public ItemOverrides getOverrides() {
            return base.getOverrides();
        }
    }
}
