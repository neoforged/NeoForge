/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.neoforge.client.buffer.param.BufferDefinitionParamTypes;
import net.neoforged.neoforge.client.buffer.param.state.OutputState;
import net.neoforged.neoforge.client.buffer.param.state.TextureState;
import net.neoforged.neoforge.client.buffer.param.state.TransparencyState;
import net.neoforged.neoforge.client.buffer.param.state.WriteMaskState;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4fStack;

/**
 * Convert middleware {@link IBufferDefinition} into vanilla compatible {@link RenderType}.
 */
@ApiStatus.Internal
public class VanillaBufferDefinitions {
    private static final Map<IBufferDefinition, RenderType> BAKED_VANILLA_RENDER_TYPES = new HashMap<>();

    public static RenderType bakeVanillaRenderType(IBufferDefinition bufferDefinition) {
        return BAKED_VANILLA_RENDER_TYPES.computeIfAbsent(bufferDefinition, bufferDefinition1 -> {
            if (bufferDefinition1 instanceof LegacyRenderTypeBufferDefinition legacy) {
                return legacy.getRenderType();
            }

            List<TextureState> textures = bufferDefinition1.getParamOrDefault(BufferDefinitionParamTypes.TEXTURE);
            Optional<Supplier<ShaderInstance>> shaderSupplier = bufferDefinition1.getParamOrDefault(BufferDefinitionParamTypes.SHADER);
            Optional<TransparencyState> transparencyState = bufferDefinition1.getParamOrDefault(BufferDefinitionParamTypes.TRANSPARENCY);
            int depthFunction = bufferDefinition1.getParamOrDefault(BufferDefinitionParamTypes.DEPTH);
            boolean cull = bufferDefinition1.getParamOrDefault(BufferDefinitionParamTypes.CULL);
            boolean lightmap = bufferDefinition1.getParamOrDefault(BufferDefinitionParamTypes.LIGHTMAP);
            boolean overlay = bufferDefinition1.getParamOrDefault(BufferDefinitionParamTypes.OVERLAY);
            Optional<OutputState> outputState = bufferDefinition1.getParamOrDefault(BufferDefinitionParamTypes.OUTPUT);
            WriteMaskState writeMaskState = bufferDefinition1.getParamOrDefault(BufferDefinitionParamTypes.WRITE_MASK);
            OptionalDouble lineWidth = bufferDefinition1.getParamOrDefault(BufferDefinitionParamTypes.LINE);
            Optional<GlStateManager.LogicOp> logicOp = bufferDefinition1.getParamOrDefault(BufferDefinitionParamTypes.COLOR_LOGIC);
            RenderType.OutlineProperty outlineProperty = bufferDefinition1.getParamOrDefault(BufferDefinitionParamTypes.OUTLINE);

            Optional<RenderStateShard.LayeringStateShard> viewOffsetLayeringRenderStateShard = bufferDefinition1.getParam(BufferDefinitionParamTypes.VIEW_OFFSET).map(offset -> new RenderStateShard.LayeringStateShard("view_offset_layering", () -> {
                Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
                matrix4fstack.pushMatrix();
                matrix4fstack.scale(offset.x, offset.y, offset.z);
                RenderSystem.applyModelViewMatrix();
            }, () -> {
                Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
                matrix4fstack.popMatrix();
                RenderSystem.applyModelViewMatrix();
            }));

            RenderStateShard.OutputStateShard outputStateShard = outputState.map(outputState1 -> new RenderStateShard.OutputStateShard("render_target", () -> {
                if (outputState1.ignoreTransparency() || Minecraft.useShaderTransparency()) {
                    outputState1.renderTargetSupplier().get().bindWrite(false);
                }
            }, () -> {
                if (outputState1.ignoreTransparency() || Minecraft.useShaderTransparency()) {
                    outputState1.renderTargetSupplier().get().bindWrite(false);
                }
            })).orElse(RenderStateShard.MAIN_TARGET);

            Optional<RenderStateShard.LayeringStateShard> polygonOffsetLayeringRenderStateShard = bufferDefinition1.getParam(BufferDefinitionParamTypes.POLYGON_OFFSET).map(offset -> new RenderStateShard.LayeringStateShard("polygon_offset_layering", () -> {
                RenderSystem.polygonOffset(offset.x, offset.y);
                RenderSystem.enablePolygonOffset();
            }, () -> {
                RenderSystem.polygonOffset(0.0F, 0.0F);
                RenderSystem.disablePolygonOffset();
            }));

            RenderStateShard.TransparencyStateShard transparencyStateShard = transparencyState.map(transparencyState1 -> new RenderStateShard.TransparencyStateShard("transparency", () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(transparencyState1.sourceRgbFactor(), transparencyState1.destRgbFactor(), transparencyState1.sourceAlphaFactor(), transparencyState1.destAlphaFactor());
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            })).orElse(RenderStateShard.NO_TRANSPARENCY);

            RenderStateShard.EmptyTextureStateShard textureStateShard = switch (textures.size()) {
                case 0 -> new RenderStateShard.EmptyTextureStateShard();
                case 1 -> new RenderStateShard.TextureStateShard(textures.getFirst().texture(), textures.getFirst().blur(), textures.getFirst().mipmap());
                default -> textures.stream().reduce(new RenderStateShard.MultiTextureStateShard.Builder(), (builder1, textureState) -> builder1.add(textureState.texture(), textureState.blur(), textureState.mipmap()), (builder1, builder2) -> builder2).build();
            };

            RenderStateShard.ColorLogicStateShard colorLogicStateShard = logicOp.map(logicOp1 -> new RenderStateShard.ColorLogicStateShard("or_reverse", () -> {
                RenderSystem.enableColorLogicOp();
                RenderSystem.logicOp(logicOp1);
            }, RenderSystem::disableColorLogicOp)).orElse(RenderStateShard.NO_COLOR_LOGIC);

            RenderStateShard.ShaderStateShard shaderStateShard = shaderSupplier.map(RenderStateShard.ShaderStateShard::new).orElse(RenderStateShard.NO_SHADER);
            RenderStateShard.DepthTestStateShard depthTestStateShard = new RenderStateShard.DepthTestStateShard("depth", depthFunction);
            RenderStateShard.CullStateShard cullStateShard = new RenderStateShard.CullStateShard(cull);
            RenderStateShard.LightmapStateShard lightmapStateShard = new RenderStateShard.LightmapStateShard(lightmap);
            RenderStateShard.OverlayStateShard overlayStateShard = new RenderStateShard.OverlayStateShard(overlay);
            RenderStateShard.WriteMaskStateShard writeMaskStateShard = new RenderStateShard.WriteMaskStateShard(writeMaskState.writeColor(), writeMaskState.writeDepth());
            RenderStateShard.LineStateShard lineStateShard = new RenderStateShard.LineStateShard(lineWidth);
            Optional<RenderStateShard.TexturingStateShard> glintTexturingRenderStateShard = bufferDefinition1.getParam(BufferDefinitionParamTypes.GLINT_SCALE).map(scale -> new RenderStateShard.TexturingStateShard("glint_texturing", () -> RenderStateShard.setupGlintTexturing(8.0F), RenderSystem::resetTextureMatrix));
            Optional<RenderStateShard.TexturingStateShard> offsetTexturingRenderStateShard = bufferDefinition1.getParam(BufferDefinitionParamTypes.TEXTURE_OFFSET).map(offset -> new RenderStateShard.OffsetTexturingStateShard(offset.x, offset.y));

            RenderType.CompositeState compositeState = new RenderType.CompositeState(
                    textureStateShard,
                    shaderStateShard,
                    transparencyStateShard,
                    depthTestStateShard,
                    cullStateShard,
                    lightmapStateShard,
                    overlayStateShard,
                    Stream.of(polygonOffsetLayeringRenderStateShard, viewOffsetLayeringRenderStateShard).filter(Optional::isPresent).map(Optional::get).findAny().orElse(RenderStateShard.NO_LAYERING),
                    outputStateShard,
                    Stream.of(glintTexturingRenderStateShard, offsetTexturingRenderStateShard).filter(Optional::isPresent).map(Optional::get).findAny().orElse(RenderStateShard.DEFAULT_TEXTURING),
                    writeMaskStateShard,
                    lineStateShard,
                    colorLogicStateShard,
                    outlineProperty);

            VertexFormat vertexFormat = bufferDefinition1.getParam(BufferDefinitionParamTypes.FORMAT).orElseThrow(() -> new IllegalStateException("Vanilla baked buffer definition must have param \"FORMAT\""));
            VertexFormat.Mode mode = bufferDefinition1.getParam(BufferDefinitionParamTypes.MODE).orElseThrow(() -> new IllegalStateException("Vanilla baked buffer definition must have param \"MODE\""));
            int size = bufferDefinition1.getParam(BufferDefinitionParamTypes.SIZE).orElseThrow(() -> new IllegalStateException("Vanilla baked buffer definition must have param \"SIZE\""));

            String name = bufferDefinition1.getParamOrDefault(BufferDefinitionParamTypes.NAME);
            boolean affectsCrumbling = bufferDefinition1.getParamOrDefault(BufferDefinitionParamTypes.CRUMBLING);
            boolean sortOnUpload = bufferDefinition1.getParamOrDefault(BufferDefinitionParamTypes.SORT);

            return RenderType.create(name, vertexFormat, mode, size, affectsCrumbling, sortOnUpload, compositeState);
        });
    }
}
