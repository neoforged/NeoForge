/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.buffer.param.general.BooleanParam;
import net.neoforged.neoforge.client.buffer.param.general.FloatParam;
import net.neoforged.neoforge.client.buffer.param.general.IntegerParam;
import net.neoforged.neoforge.client.buffer.param.general.Vector2fParam;
import net.neoforged.neoforge.client.buffer.param.general.Vector3fParam;
import net.neoforged.neoforge.client.buffer.param.state.OutputState;
import net.neoforged.neoforge.client.buffer.param.state.TextureState;
import net.neoforged.neoforge.client.buffer.param.state.TransparencyState;
import net.neoforged.neoforge.client.buffer.param.state.WriteMaskState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Stores {@link IBufferDefinitionParamType} provided by NeoForge and
 */
public class BufferDefinitionParamTypeManager {
    private static final Map<ResourceLocation, IBufferDefinitionParamType<?, ?>> BUFFER_DEFINITION_PARAM_TYPES = new HashMap<>();

    public static final IBufferDefinitionParamType<List<TextureState>, TextureParam> TEXTURE = register("texture", TextureParam.Vanilla.EMPTY);
    public static final IBufferDefinitionParamType<Optional<Supplier<ShaderInstance>>, ShaderParam> SHADER = register("shader", ShaderParam.Vanilla.NO_SHADER);
    public static final IBufferDefinitionParamType<Optional<TransparencyState>, TransparencyParam> TRANSPARENCY = register("transparency", TransparencyParam.Vanilla.NO_TRANSPARENCY);
    public static final IBufferDefinitionParamType<Integer, IntegerParam> DEPTH = register("depth", DepthTestParam.Vanilla.LEQUAL_DEPTH_TEST);
    public static final IBufferDefinitionParamType<Boolean, BooleanParam> CULL = register("cull", CullParam.Vanilla.CULL);
    public static final IBufferDefinitionParamType<Boolean, BooleanParam> LIGHTMAP = register("lightmap", LightmapParam.Vanilla.NO_LIGHTMAP);
    public static final IBufferDefinitionParamType<Boolean, BooleanParam> OVERLAY = register("overlay", OverlayParam.Vanilla.NO_OVERLAY);
    public static final IBufferDefinitionParamType<Vector2f, Vector2fParam> POLYGON_OFFSET = register("polygon_offset", PolygonOffsetParam.Vanilla.POLYGON_OFFSET_LAYERING);
    public static final IBufferDefinitionParamType<Vector3f, Vector3fParam> VIEW_OFFSET = register("view_offset", ViewOffsetParam.Vanilla.VIEW_OFFSET_Z_LAYERING);
    public static final IBufferDefinitionParamType<Optional<OutputState>, OutputParam> OUTPUT = register("output", OutputParam.Vanilla.MAIN_TARGET);
    public static final IBufferDefinitionParamType<Float, FloatParam> GLINT_SCALE = register("glint_scale", GlintScaleParam.Vanilla.GLINT_TEXTURING);
    public static final IBufferDefinitionParamType<Vector2f, Vector2fParam> TEXTURE_OFFSET = register("texture_offset", new Vector2fParam(new Vector2f(0.0f, 0.0f)));
    public static final IBufferDefinitionParamType<WriteMaskState, WriteMaskParam> WRITE_MASK = register("write_mask", WriteMaskParam.Vanilla.COLOR_DEPTH_WRITE);
    public static final IBufferDefinitionParamType<OptionalDouble, LineParam> LINE = register("line", LineParam.Vanilla.DEFAULT_LINE);
    public static final IBufferDefinitionParamType<Optional<GlStateManager.LogicOp>, ColorLogicParam> COLOR_LOGIC = register("color_logic", ColorLogicParam.Vanilla.NO_COLOR_LOGIC);
    public static final IBufferDefinitionParamType<OptionalDouble, FragmentDiscardParam> FRAGMENT_DISCARD = register("fragment_discard", FragmentDiscardParam.Vanilla.ZERO);
    public static final IBufferDefinitionParamType<RenderType.OutlineProperty, OutlineParam> OUTLINE = register("outline", OutlineParam.Vanilla.AFFECTS_OUTLINE);
    public static final IBufferDefinitionParamType<String, NameParam> NAME = register("name", new NameParam("baked_buffer_definition"));
    public static final IBufferDefinitionParamType<VertexFormat, FormatParam> FORMAT = register("format", FormatParam.Vanilla.BLOCK);
    public static final IBufferDefinitionParamType<VertexFormat.Mode, ModeParam> MODE = register("mode", ModeParam.Vanilla.QUADS);
    public static final IBufferDefinitionParamType<Integer, IntegerParam> SIZE = register("size", new IntegerParam(0));
    public static final IBufferDefinitionParamType<Boolean, BooleanParam> CRUMBLING = register("crumbling", CrumblingParam.Vanilla.NOT_AFFECTS_CRUMBLING);
    public static final IBufferDefinitionParamType<Boolean, BooleanParam> SORT = register("sort", SortParam.Vanilla.DONT_SORT_ON_UPLOAD);

    @ApiStatus.Internal
    private static <T, P extends IBufferDefinitionParam<T>> IBufferDefinitionParamType<T, P> register(String name, P defaultValue) {
        return register(ResourceLocation.fromNamespaceAndPath("neoforge", name), defaultValue);
    }

    /**
     * Build a {@link IBufferDefinitionParamType#simple(ResourceLocation, IBufferDefinitionParam) simple param type} from a default value and register {@link ResourceLocation alias} for it
     * 
     * @param resourceLocation the alias of the param type
     * @param defaultValue     the default value of the param type
     * @return the registered param type
     * @param <T> The value hold by the param
     * @param <P> The param class
     */
    @SuppressWarnings("unchecked")
    public static <T, P extends IBufferDefinitionParam<T>> IBufferDefinitionParamType<T, P> register(ResourceLocation resourceLocation, P defaultValue) {
        return (IBufferDefinitionParamType<T, P>) BUFFER_DEFINITION_PARAM_TYPES.computeIfAbsent(resourceLocation, resourceLocation1 -> IBufferDefinitionParamType.simple(resourceLocation1, defaultValue));
    }

    /**
     * Register an {@link ResourceLocation alias} for a given {@link IBufferDefinitionParamType}
     * 
     * @param resourceLocation the alias of the param type
     * @param paramType        the param type to be registered
     */
    public static void register(ResourceLocation resourceLocation, IBufferDefinitionParamType<?, ?> paramType) {
        BUFFER_DEFINITION_PARAM_TYPES.putIfAbsent(resourceLocation, paramType);
    }

    /**
     * Find {@link IBufferDefinitionParamType} by {@link ResourceLocation alias}
     * 
     * @param resourceLocation the alias of param type
     * @return The found corresponding param type, or null if not found
     */
    @Nullable
    public static IBufferDefinitionParamType<?, ?> getParamType(ResourceLocation resourceLocation) {
        return BUFFER_DEFINITION_PARAM_TYPES.get(resourceLocation);
    }
}
