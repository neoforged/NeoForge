/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.Nullable;

/**
 * Extension interface for {@link BakedModel}.
 */
public interface IBakedModelExtension {
    private BakedModel self() {
        return (BakedModel) this;
    }

    /**
     * A null {@link RenderType} is used for the breaking overlay as well as non-standard rendering, so models should return all their quads.
     */
    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        return self().getQuads(state, side, rand);
    }

    /**
     * @deprecated Use {@link #useAmbientOcclusion(BlockState, ModelData, RenderType)} instead
     */
    @Deprecated(forRemoval = true)
    default boolean useAmbientOcclusion(BlockState state) {
        return self().useAmbientOcclusion();
    }

    /**
     * @deprecated Use {@link #useAmbientOcclusion(BlockState, ModelData, RenderType)} instead
     */
    @Deprecated(forRemoval = true)
    default boolean useAmbientOcclusion(BlockState state, RenderType renderType) {
        return self().useAmbientOcclusion(state);
    }

    /**
     * Controls the AO behavior for all quads of this model. The default behavior is to use AO unless the block emits light,
     * {@link TriState#TRUE} and {@link TriState#FALSE} force AO to be enabled and disabled respectively, regardless of
     * the block emitting light or not. {@link BakedQuad#hasAmbientOcclusion()} can be used to disable AO for a specific
     * quad even if this method says otherwise.
     * <p>
     * This method cannot force AO if the global smooth lighting video setting is disabled.
     *
     * @param state      the block state this model is being rendered for
     * @param data       the model data used to render this model
     * @param renderType the render type the model is being rendered with
     * @return {@link TriState#TRUE} to force-enable AO, {@link TriState#FALSE} to force-disable AO or {@link TriState#DEFAULT} to use vanilla AO behavior
     */
    default TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) {
        return useAmbientOcclusion(state, renderType) ? TriState.DEFAULT : TriState.FALSE;
    }

    /**
     * Applies a transform for the given {@link ItemTransforms.TransformType} and {@code applyLeftHandTransform}, and
     * returns the model to be rendered.
     */
    default BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        self().getTransforms().getTransform(transformType).apply(applyLeftHandTransform, poseStack);
        return self();
    }

    default ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        return modelData;
    }

    default TextureAtlasSprite getParticleIcon(ModelData data) {
        return self().getParticleIcon();
    }

    /**
     * Gets the set of {@link RenderType render types} to use when drawing this block in the level.
     * Supported types are those returned by {@link RenderType#chunkBufferLayers()}.
     * <p>
     * By default, defers query to {@link ItemBlockRenderTypes}.
     */
    default ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return ItemBlockRenderTypes.getRenderLayers(state);
    }

    /**
     * Gets an ordered list of {@link RenderType render types} to use when drawing this item.
     * All render types using the {@link com.mojang.blaze3d.vertex.DefaultVertexFormat#NEW_ENTITY} format are supported.
     * <p>
     * This method will only be called on the models returned by {@link #getRenderPasses(ItemStack, boolean)}.
     * <p>
     * By default, defers query to {@link ItemBlockRenderTypes}.
     *
     * @see #getRenderPasses(ItemStack, boolean)
     */
    default List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        return List.of(RenderTypeHelper.getFallbackItemRenderType(itemStack, self(), fabulous));
    }

    /**
     * Gets an ordered list of baked models used to render this model as an item.
     * Each of those models' render types will be queried via {@link #getRenderTypes(ItemStack, boolean)}.
     * <p>
     * By default, returns the model itself.
     *
     * @see #getRenderTypes(ItemStack, boolean)
     */
    default List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
        return List.of(self());
    }
}
