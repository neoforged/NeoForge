/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.block;

import java.util.Locale;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.special.SkullSpecialRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialBlockModelRendererEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Adds a blaze head block and item to test the event for registering a custom skull model and to demonstrate the proper way to register a custom mob skull.
 *
 * To test it, locate "blaze_head", and test the item model, the model in the players head slot, the model in an armor stand, the wall block model, and the floor block model.
 * Should appear identical in shape to vanilla heads such as zombies or skeletons, but use the blaze skin.
 */
@Mod(CustomHeadTest.MODID)
public class CustomHeadTest {
    static final String MODID = "custom_head_test";
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);
    private static final DeferredBlock<Block> BLAZE_HEAD = BLOCKS.registerBlock("blaze_head", props -> new CustomSkullBlock(SkullType.BLAZE, props), BlockBehaviour.Properties.of().strength(1.0F));
    private static final DeferredBlock<Block> BLAZE_HEAD_WALL = BLOCKS.registerBlock("blaze_wall_head", props -> new CustomWallSkullBlock(SkullType.BLAZE, props.strength(1.0F).overrideLootTable(BLAZE_HEAD.get().getLootTable())), BlockBehaviour.Properties.of());
    private static final DeferredItem<Item> BLAZE_HEAD_ITEM = ITEMS.registerItem("blaze_head", props -> new StandingAndWallBlockItem(BLAZE_HEAD.get(), BLAZE_HEAD_WALL.get(), Direction.DOWN, props.rarity(Rarity.UNCOMMON).equippableUnswappable(EquipmentSlot.HEAD)));
    private static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CustomSkullBlockEntity>> CUSTOM_SKULL = BLOCK_ENTITIES.register("custom_skull", () -> new BlockEntityType<>(CustomSkullBlockEntity::new, BLAZE_HEAD.get(), BLAZE_HEAD_WALL.get()));

    public CustomHeadTest(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(BLAZE_HEAD_ITEM);
    }

    private static class CustomSkullBlock extends SkullBlock {
        public CustomSkullBlock(Type type, Properties props) {
            super(type, props);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new CustomSkullBlockEntity(pos, state);
        }
    }

    private static class CustomWallSkullBlock extends WallSkullBlock {
        public CustomWallSkullBlock(SkullBlock.Type type, Properties props) {
            super(type, props);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new CustomSkullBlockEntity(pos, state);
        }
    }

    private static class CustomSkullBlockEntity extends SkullBlockEntity {
        public CustomSkullBlockEntity(BlockPos pos, BlockState state) {
            super(pos, state);
        }

        @Override
        public BlockEntityType<?> getType() {
            return CUSTOM_SKULL.get();
        }
    }

    private enum SkullType implements SkullBlock.Type {
        BLAZE;

        SkullType() {
            TYPES.put(getSerializedName(), this);
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = MODID)
    private static class ClientEvents {
        static final ModelLayerLocation BLAZE_HEAD_LAYER = new ModelLayerLocation(BLAZE_HEAD.getId(), "main");

        @SubscribeEvent
        static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(BLAZE_HEAD_LAYER, Lazy.of(SkullModel::createMobHeadLayer));
        }

        @SubscribeEvent
        static void registerLayerDefinitions(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(CUSTOM_SKULL.get(), SkullBlockRenderer::new);
        }

        @SubscribeEvent
        static void registerSkullModel(EntityRenderersEvent.CreateSkullModels event) {
            event.registerSkullModel(SkullType.BLAZE, ClientEvents.BLAZE_HEAD_LAYER, ResourceLocation.withDefaultNamespace("textures/entity/blaze.png"));
        }

        @SubscribeEvent
        static void registerSpecialBlockRenderer(RegisterSpecialBlockModelRendererEvent event) {
            event.register(BLAZE_HEAD.get(), new SkullSpecialRenderer.Unbaked(SkullType.BLAZE));
        }
    }
}
