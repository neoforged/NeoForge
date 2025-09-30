/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

/**
 * Test mod for the custom transform types feature.
 * This test mod adds an item that should be held in the main hand, while another item is in the offhand.
 * When right-clicked on a block, the item will "hang" the stack from the offhand slot on the wall.
 * In addition, a replacement model for the stick is provided in the resources.
 * If the feature is working, the stick will have a custom transform while hanging on walls,
 * when compared to similar items such as a fishing rod.
 * Editing that model json will reflect ingame after a resource reload (F3+T).
 */
@Mod(CustomItemDisplayContextTest.MODID)
public class CustomItemDisplayContextTest {
    public static final String MODID = "custom_transformtype_test";

    @EventBusSubscriber(value = Dist.CLIENT, modid = MODID)
    private static class RendererEvents {
        public static final ItemDisplayContext HANGING = ItemDisplayContext.valueOf("NEOTESTS_HANGING");

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ITEM_HANGER_BE.get(), ItemHangerBlockEntityRenderer::new);
        }

        private static class ItemHangerBlockEntityRenderer implements BlockEntityRenderer<ItemHangerBlockEntity, ItemHangerRenderState> {
            private final ItemModelResolver itemModelResolver;

            public ItemHangerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
                this.itemModelResolver = context.itemModelResolver();
            }

            @Override
            public void submit(ItemHangerRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
                poseStack.pushPose();

                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(renderState.facing.getRotation());
                poseStack.translate(-0.5, -0.5, -0.5);

                renderState.item.submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);

                poseStack.popPose();
            }

            @Override
            public ItemHangerRenderState createRenderState() {
                return new ItemHangerRenderState();
            }

            @Override
            public void extractRenderState(ItemHangerBlockEntity blockEntity, ItemHangerRenderState renderState, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
                BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);

                renderState.facing = blockEntity.getBlockState().getValue(ItemHangerBlock.FACING);

                ItemStackRenderState stackRenderState = new ItemStackRenderState();
                itemModelResolver.updateForTopItem(stackRenderState, blockEntity.heldItem, HANGING, blockEntity.level(), blockEntity, 0);
                renderState.item = stackRenderState;
            }
        }

        private static final class ItemHangerRenderState extends BlockEntityRenderState {
            Direction facing;
            ItemStackRenderState item;
        }
    }

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);

    public static final DeferredBlock<Block> ITEM_HANGER_BLOCK = BLOCKS.registerBlock("item_hanger", ItemHangerBlock::new, BlockBehaviour.Properties.of().noCollision().noOcclusion().noLootTable());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemHangerBlockEntity>> ITEM_HANGER_BE = BLOCK_ENTITY_TYPES.register("item_hanger", () -> new BlockEntityType<>(ItemHangerBlockEntity::new, ITEM_HANGER_BLOCK.get()));
    public static final DeferredItem<Item> ITEM_HANGER_ITEM = ITEMS.registerItem("item_hanger", props -> new ItemHangerItem(ITEM_HANGER_BLOCK.get(), props));

    public CustomItemDisplayContextTest(IEventBus modBus) {
        modBus.addListener(this::gatherData);
        BLOCKS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
            event.accept(ITEM_HANGER_ITEM);
    }

    public void gatherData(GatherDataEvent.Client event) {
        DataGenerator gen = event.getGenerator();
        final PackOutput output = gen.getPackOutput();
        gen.addProvider(true, new ModelGen(output));
    }

    private static final class ModelGen extends ModelProvider {
        public ModelGen(PackOutput output) {
            super(output, MODID);
        }

        @Override
        protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
            blockModels.createHorizontallyRotatedBlock(ITEM_HANGER_BLOCK.value(), TexturedModel.ORIENTABLE);

            ModelTemplates.FLAT_HANDHELD_ROD_ITEM.extend()
                    .transform(RendererEvents.HANGING, transform -> transform
                            .rotation(62, 180 - 33, 40)
                            .translation(-2.25f, 1.5f, -0.25f)
                            .scale(0.48f))
                    .build()
                    .create(Items.STICK, TextureMapping.layer0(Items.STICK), itemModels.modelOutput);
        }
    }

    private static class ItemHangerBlock extends HorizontalDirectionalBlock implements EntityBlock {
        public static final MapCodec<ItemHangerBlock> CODEC = simpleCodec(ItemHangerBlock::new);

        public ItemHangerBlock(BlockBehaviour.Properties properties) {
            super(properties);
            registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
        }

        @Override
        protected MapCodec<ItemHangerBlock> codec() {
            return CODEC;
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING);
        }

        @Nullable
        @Override
        public BlockState getStateForPlacement(BlockPlaceContext ctx) {
            return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
        }

        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new ItemHangerBlockEntity(pos, state);
        }

        @Deprecated
        @Override
        public RenderShape getRenderShape(BlockState state) {
            return RenderShape.MODEL;
        }
    }

    private static class ItemHangerBlockEntity extends BlockEntity implements ItemOwner {
        private ItemStack heldItem;

        public ItemHangerBlockEntity(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState) {
            super(type, blockPos, blockState);
        }

        public ItemHangerBlockEntity(BlockPos blockPos, BlockState blockState) {
            this(ITEM_HANGER_BE.get(), blockPos, blockState);
        }

        @Override
        public CompoundTag getUpdateTag(HolderLookup.Provider holderLookup) {
            return saveWithoutMetadata(holderLookup);
        }

        @Nullable
        @Override
        public Packet<ClientGamePacketListener> getUpdatePacket() {
            return ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag);
        }

        @Override
        public void onDataPacket(Connection net, ValueInput input) {
            handleUpdateTag(input);
        }

        @Override
        protected void saveAdditional(ValueOutput output) {
            super.saveAdditional(output);
            if (heldItem != null) {
                output.store("item", ItemStack.CODEC, heldItem);
            }
        }

        @Override
        public void loadAdditional(ValueInput input) {
            super.loadAdditional(input);
            heldItem = input.read("item", ItemStack.CODEC).orElse(null);
        }

        @Override
        public Level level() {
            return getLevel();
        }

        @Override
        public Vec3 position() {
            return getBlockPos().getCenter();
        }

        @Override
        public float getVisualRotationYInDegrees() {
            return 0;
        }
    }

    private static class ItemHangerItem extends BlockItem {
        public ItemHangerItem(Block block, Item.Properties properties) {
            super(block, properties);
        }

        @Override
        protected boolean placeBlock(BlockPlaceContext ctx, BlockState state) {
            Player player = ctx.getPlayer();
            if (player == null)
                return false;
            var hand = ctx.getHand();
            if (hand != InteractionHand.MAIN_HAND) {
                return false;
            }
            if (!super.placeBlock(ctx, state))
                return false;
            if (ctx.getLevel().getBlockEntity(ctx.getClickedPos()) instanceof ItemHangerBlockEntity be) {
                be.heldItem = player.getItemInHand(InteractionHand.OFF_HAND);
            }
            return true;
        }
    }
}
