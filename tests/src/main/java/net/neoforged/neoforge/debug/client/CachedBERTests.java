package net.neoforged.neoforge.debug.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.cached.CacheableBERenderingPipeline;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@ForEachTest(side = Dist.CLIENT, groups = {"client.event", "event"})
public class CachedBERTests {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.Blocks.createBlocks("neotests_cached_ber");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "neotests_cached_ber");

    public static final DeferredBlock<Block> THE_BLOCK = BLOCKS.register(
        "not_enough_vertexes",
        () -> new TheBlock(
            BlockBehaviour.Properties.of()
                .noCollission()
                .noOcclusion()
                .lightLevel(state -> 15)
        )
    );

    public static final Supplier<BlockEntityType<TheBlockEntity>> THE_BE = BLOCK_ENTITY_TYPES.register(
        "not_enough_vertexes",
        () -> BlockEntityType.Builder.of(
                TheBlockEntity::new,
                THE_BLOCK.get()
            )
            .build(null)
    );

    @TestHolder(description = "Register a block with cached BER which adds lots of vertexes")
    static void registerBlock(final DynamicTest test) {
        BLOCKS.register(test.framework().modEventBus());
        BLOCK_ENTITY_TYPES.register(test.framework().modEventBus());
        test.framework().modEventBus().addListener(CachedBERTests::clientSetup);
    }

    static private void clientSetup(final FMLClientSetupEvent event) {
        BlockEntityRenderers.register(THE_BE.get(), TheRenderer::new);
    }

    public static class TheBlock extends BaseEntityBlock {

        protected TheBlock(Properties p_49224_) {
            super(p_49224_);
            registerDefaultState(getStateDefinition().any().setValue(BlockStateProperties.ENABLED, false));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(BlockStateProperties.ENABLED);
        }

        @Override
        protected ItemInteractionResult useItemOn(ItemStack p_316304_, BlockState state, Level level, BlockPos pos, Player p_316132_, InteractionHand p_316595_, BlockHitResult p_316140_) {
            level.setBlockAndUpdate(
                pos,
                state.setValue(BlockStateProperties.ENABLED, !state.getValue(BlockStateProperties.ENABLED))
            );
            if (!level.isClientSide) return ItemInteractionResult.SUCCESS;
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null) return ItemInteractionResult.SUCCESS;
            if (!level.getBlockState(pos).getValue(BlockStateProperties.ENABLED)) {
                CacheableBERenderingPipeline.getInstance().update(be);
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("RenderMode: BER"));
            } else {
                CacheableBERenderingPipeline.getInstance().update(be);
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("RenderMode: Cached BER"));
            }
            return ItemInteractionResult.SUCCESS;
        }

        @Override
        protected MapCodec<? extends BaseEntityBlock> codec() {
            return Block.simpleCodec(TheBlock::new);
        }

        @Override
        public @Nullable BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
            return new TheBlockEntity(p_153215_, p_153216_);
        }
    }

    public static class TheBlockEntity extends BlockEntity {

        public TheBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
            super(THE_BE.get(), p_155229_, p_155230_);
        }
    }

    public static class TheRenderer implements BlockEntityRenderer<TheBlockEntity> {

        public TheRenderer(BlockEntityRendererProvider.Context ctx) {

        }

        @Override
        public void render(
            TheBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
        ) {
            Level level = blockEntity.getLevel();
            BlockPos pos = blockEntity.getBlockPos();
            if (level == null) return;
            BlockState blockState = level.getBlockState(pos);
            if (!blockState.is(THE_BLOCK.get())) return;
            if (blockState.getValue(BlockStateProperties.ENABLED)) return;
            renderManyTorches(poseStack, bufferSource, packedLight, packedOverlay);
            if (bufferSource instanceof MultiBufferSource.BufferSource buffer) {
                buffer.endLastBatch();
            }
        }

        private void renderManyTorches(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
            BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
            for (int dx = 0; dx < 32; dx++) {
                for (int dz = 0; dz < 32; dz++) {
                    poseStack.pushPose();
                    poseStack.translate(dx * 0.25, 1, dz * 0.25);
                    dispatcher.renderSingleBlock(
                        Blocks.TORCH.defaultBlockState(),
                        poseStack,
                        bufferSource,
                        packedLight,
                        packedOverlay,
                        ModelData.EMPTY,
                        RenderType.cutout()
                    );
                    poseStack.popPose();
                }
            }
        }

        @Override
        public void renderCached(
            TheBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            float partialTick,
            int packedLight,
            int packedOverlay
        ) {
            Level level = blockEntity.getLevel();
            BlockPos pos = blockEntity.getBlockPos();
            if (level == null) return;
            BlockState blockState = level.getBlockState(pos);
            if (!blockState.is(THE_BLOCK.get())) return;
            if (!blockState.getValue(BlockStateProperties.ENABLED)) return;
            renderManyTorches(poseStack, bufferSource, packedLight, packedOverlay);
        }
    }
}
