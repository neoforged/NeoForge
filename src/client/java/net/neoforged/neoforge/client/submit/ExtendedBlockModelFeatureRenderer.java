/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.submit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ExtendedBlockModelFeatureRenderer extends RenderTypeFeatureRenderer<ExtendedBlockModelFeatureRenderer.Submit> {
    public static final FeatureRendererType<ExtendedBlockModelFeatureRenderer.Submit> TYPE = FeatureRendererType.create("Multilayer Block Model");
    private static final Direction[] DIRECTIONS = Direction.values();

    private final QuadInstance quadInstance = new QuadInstance();

    @Override
    protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
        for (Submit submit : submits) {
            this.quadInstance.setLightCoords(submit.lightCoords());
            this.quadInstance.setOverlayCoords(submit.overlayCoords());

            for (BlockStateModelPart part : submit.modelParts()) {
                putMultiLayerPartQuads(submit.pose(), part, this.quadInstance, submit.tintColor(), submit.tintLayers());
            }
        }
    }

    private void putMultiLayerPartQuads(PoseStack.Pose pose, BlockStateModelPart part, QuadInstance quadInstance, int baseTintColor, int[] tintLayers) {
        for (Direction direction : DIRECTIONS) {
            for (BakedQuad quad : part.getQuads(direction)) {
                putQuad(pose, quad, quadInstance, baseTintColor, tintLayers, getQuadBuffer(quad));
            }
        }

        for (BakedQuad quad : part.getQuads(null)) {
            putQuad(pose, quad, quadInstance, baseTintColor, tintLayers, getQuadBuffer(quad));
        }
    }

    private static void putQuad(PoseStack.Pose pose, BakedQuad quad, QuadInstance instance, int baseTintColor, int[] tintLayers, VertexConsumer buffer) {
        int tintIndex = quad.materialInfo().tintIndex();
        boolean useTintLayer = tintIndex != -1 && tintIndex < tintLayers.length;
        instance.setColor(useTintLayer ? ARGB.multiply(baseTintColor, tintLayers[tintIndex]) : baseTintColor);
        buffer.putBakedQuad(pose, quad, instance);
    }

    private VertexConsumer getQuadBuffer(BakedQuad quad) {
        return getVertexBuilder(switch (quad.materialInfo().layer()) {
            case SOLID -> NeoForgeRenderTypes.SOLID_BLOCK_SHEET;
            case CUTOUT -> Sheets.cutoutBlockItemSheet();
            case TRANSLUCENT -> Sheets.translucentBlockItemSheet();
        });
    }

    public record Submit(
            PoseStack.Pose pose,
            List<BlockStateModelPart> modelParts,
            boolean translucent,
            int[] tintLayers,
            int lightCoords,
            int overlayCoords,
            int tintColor) implements TranslucentSubmit {
        @Override
        public float distanceToCameraSq() {
            return TranslucentSubmit.computeDistanceToCameraSq(this.pose.pose(), 0.5F, 0.5F, 0.5F);
        }

        @Override
        public FeatureRendererType<Submit> featureType() {
            return TYPE;
        }
    }
}
