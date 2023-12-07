/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.model;

import com.mojang.math.Transformation;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Test mod that demos most Forge-provided model loaders in a single block + item, as well as in-JSON render states
 * and the refactored immutable ModelData managed by the client level. The block can be found in the decoration tab.
 * <p>
 * Additionally, some fields in the JSON have deprecated names, so those MUST be updated in 1.20, or the model will
 * break. They have all been annotated accordingly.
 * <ul>
 * <li>As a block: Composite loader, using 3 child element models, each with a different render type,
 * some using vanilla's elements loader, and some Forge's</li>
 * <li>In the right hand: Fluid container with lava (emissive)</li>
 * <li>In the left hand: Multi-layer item with chainmail chestplate + emissive bow</li>
 * </ul>
 * <p>
 * Clicking on the upper half of the block will make the model move up by a bit, and clicking on the lower half will
 * move it down.
 */
@Mod(MegaModelTest.MOD_ID)
public class MegaModelTest {
    public static final String MOD_ID = "mega_model_test";
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MOD_ID);

    private static final String REG_NAME = "test_block";
    public static final DeferredBlock<Block> TEST_BLOCK = BLOCKS.register(REG_NAME, TestBlock::new);
    public static final DeferredItem<BlockItem> TEST_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(TEST_BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> TEST_BLOCK_ENTITY = BLOCK_ENTITIES.register(REG_NAME, () -> new BlockEntityType<>(
            TestBlock.Entity::new, Set.of(TEST_BLOCK.get()), null));

    public MegaModelTest() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(TEST_BLOCK_ITEM);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onModelBakingCompleted(ModelEvent.ModifyBakingResult event) {
            var name = new ModelResourceLocation(MOD_ID, REG_NAME, "");
            event.getModels().computeIfPresent(name, (n, m) -> new TransformingModelWrapper(m));
        }

    }

    private static class TestBlock extends Block implements EntityBlock {
        public TestBlock() {
            super(Properties.of().mapColor(MapColor.STONE));
        }

        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new Entity(pos, state);
        }

        @Override
        public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            var entity = level.getBlockEntity(pos);
            if (entity instanceof Entity e) {
                e.y += Mth.sign(hit.getLocation().y - pos.getY() - 0.5);
                e.requestModelDataUpdate();
                level.sendBlockUpdated(pos, state, state, 8);
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            return super.use(state, level, pos, player, hand, hit);
        }

        public static class Entity extends BlockEntity {
            public int y = 0;

            public Entity(BlockPos pos, BlockState state) {
                super(TEST_BLOCK_ENTITY.get(), pos, state);
            }

            @Override
            public @NotNull ModelData getModelData() {
                return ModelData.builder().with(TestData.PROPERTY, new TestData(new Transformation(
                        new Vector3f(0, y * 0.2f, 0),
                        new Quaternionf(1f, 1f, 1f, 1f),
                        Transformation.identity().getScale(),
                        new Quaternionf(1f, 1f, 1f, 1f)))).build();
            }
        }
    }

    private record TestData(Transformation transform) {
        public static final ModelProperty<TestData> PROPERTY = new ModelProperty<>();
    }

    private static class TransformingModelWrapper extends BakedModelWrapper<BakedModel> {
        public TransformingModelWrapper(BakedModel originalModel) {
            super(originalModel);
        }

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
            var quads = super.getQuads(state, side, rand, data, renderType);
            if (!data.has(TestData.PROPERTY))
                return quads;
            return QuadTransformers.applying(data.get(TestData.PROPERTY).transform()).process(quads);
        }
    }
}
