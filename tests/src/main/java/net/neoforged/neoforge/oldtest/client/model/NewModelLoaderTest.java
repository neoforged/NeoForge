/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.DelegateUnbakedModel;
import net.neoforged.neoforge.client.model.ExtendedUnbakedGeometry;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.neoforged.neoforge.client.model.generators.loaders.ObjModelBuilder;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

@Mod(NewModelLoaderTest.MODID)
public class NewModelLoaderTest {
    public static final String MODID = "new_model_loader_test";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static DeferredBlock<Block> obj_block = BLOCKS.registerBlock("obj_block", TestBlock::new, Block.Properties.of().mapColor(MapColor.WOOD).strength(10));

    // Same at obj_block except all the parts in the obj model have the same name,
    // this is a test for neoforged/NeoForge#1755 that was fixed by neoforged/NeoForge#1759
    public static DeferredBlock<Block> obj_block_same_part_names = BLOCKS.registerBlock("obj_block_same_part_names", TestBlock::new, Block.Properties.of().mapColor(MapColor.WOOD).strength(10));

    public static DeferredItem<Item> obj_item = ITEMS.registerItem("obj_block", props -> new BlockItem(obj_block.get(), props.useBlockDescriptionPrefix()) {
        @Override
        public boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
            return armorType == EquipmentSlot.HEAD;
        }
    });

    public static DeferredItem<Item> obj_item_same_part_names = ITEMS.registerItem("obj_block_same_part_names", props -> new BlockItem(obj_block_same_part_names.get(), props));

    public static DeferredItem<Item> custom_transforms = ITEMS.registerSimpleItem("custom_transforms");

    public static DeferredItem<Item> custom_vanilla_loader = ITEMS.registerSimpleItem("custom_vanilla_loader");

    public static DeferredItem<Item> custom_loader = ITEMS.registerSimpleItem("custom_loader");

    public NewModelLoaderTest(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);

        modEventBus.addListener(this::modelRegistry);
        modEventBus.addListener(this::datagen);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            Arrays.asList(
                    obj_item,
                    obj_item_same_part_names,
                    custom_transforms,
                    custom_vanilla_loader,
                    custom_loader).forEach(event::accept);
        }
    }

    public void modelRegistry(ModelEvent.RegisterLoaders event) {
        event.register(ResourceLocation.fromNamespaceAndPath(MODID, "custom_loader"), new TestLoader());
    }

    static class TestBlock extends Block {
        public TestBlock(Properties properties) {
            super(properties);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(BlockStateProperties.HORIZONTAL_FACING);
        }

        @Nullable
        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return defaultBlockState().setValue(
                    BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection());
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
            return Block.box(2, 2, 2, 14, 14, 14);
        }
    }

    static class TestLoader implements UnbakedModelLoader<TestModel> {
        @Override
        public TestModel read(JsonObject jsonObject, JsonDeserializationContext ctx) throws JsonParseException {
            return new TestModel(ctx.deserialize(jsonObject, BlockModel.class));
        }
    }

    static class TestModel extends DelegateUnbakedModel {
        private TestModel(UnbakedModel parent) {
            super(parent);
        }

        @Nullable
        @Override
        public ExtendedUnbakedGeometry geometry() {
            return TestModel::bake;
        }

        private static QuadCollection bake(TextureSlots textures, ModelBaker baker, ModelState state, ModelDebugName debugName, ContextMap additionalProperties) {
            TextureAtlasSprite texture = baker.sprites().resolveSlot(textures, TextureSlot.PARTICLE.getId(), debugName);

            var quadBaker = new QuadBakingVertexConsumer();

            quadBaker.setDirection(Direction.UP);
            quadBaker.setSprite(texture);

            quadBaker.addVertex(0, 1, 0.5f).setColor(255, 255, 255, 255).setUv(texture.getU(0), texture.getV(0)).setOverlay(0).setNormal(0, 0, 0);
            quadBaker.addVertex(0, 0, 0.5f).setColor(255, 255, 255, 255).setUv(texture.getU(0), texture.getV(16)).setOverlay(0).setNormal(0, 0, 0);
            quadBaker.addVertex(1, 0, 0.5f).setColor(255, 255, 255, 255).setUv(texture.getU(16), texture.getV(16)).setOverlay(0).setNormal(0, 0, 0);
            quadBaker.addVertex(1, 1, 0.5f).setColor(255, 255, 255, 255).setUv(texture.getU(16), texture.getV(0)).setOverlay(0).setNormal(0, 0, 0);

            return new QuadCollection.Builder().addUnculledFace(quadBaker.bakeQuad()).build();
        }
    }

    private void datagen(GatherDataEvent.Client event) {
        DataGenerator gen = event.getGenerator();
        final PackOutput output = gen.getPackOutput();

        // Let blockstate provider see generated item models by passing its existing file helper
        gen.addProvider(true, new ModelGen(output));
    }

    private static class ModelGen extends ModelProvider {
        public ModelGen(PackOutput output) {
            super(output, MODID);
        }

        @Override
        protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
            createModelAndBlockState(obj_block, "sugar_glider", blockModels);
            createModelAndBlockState(obj_block_same_part_names, "sugar_glider_same_part_names", blockModels);
        }

        private void createModelAndBlockState(DeferredBlock<Block> block, String objModel, BlockModelGenerators blockModels) {
            var qrTexture = TextureSlot.create("qr");

            blockModels.createHorizontallyRotatedBlock(block.value(), TexturedModel.ORIENTABLE.updateTemplate(template -> template.extend()
                    .customLoader(ObjModelBuilder::new, loader -> loader
                            .modelLocation(ResourceLocation.fromNamespaceAndPath("new_model_loader_test", "models/item/" + objModel + ".obj"))
                            .flipV(true))
                    .requiredTextureSlot(qrTexture)
                    .build())
                    .updateTexture(textures -> textures
                            .put(qrTexture, TextureMapping.getBlockTexture(Blocks.OAK_PLANKS))
                            .copySlot(qrTexture, TextureSlot.PARTICLE)));
        }

        @Override
        protected Stream<? extends Holder<Block>> getKnownBlocks() {
            return Stream.of(obj_block, obj_block_same_part_names);
        }

        @Override
        protected Stream<? extends Holder<Item>> getKnownItems() {
            return Stream.of(obj_item, obj_item_same_part_names);
        }
    }
}
