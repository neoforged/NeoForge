/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.renderstate;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.neoforged.neoforge.client.extensions.IRenderStateExtension;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Extension class for RenderState objects (ie {@link EntityRenderState}).
 * Allows modders to add arbitrary data onto render states for use in custom rendering.
 */
public abstract class BaseRenderState implements IRenderStateExtension {
    private static final ContextKey<Object2BooleanMap<String>> MODEL_PART_VISIBILITY = new ContextKey<>(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "model_part_visibility"));

    protected final Map<ContextKey<?>, Object> extensions = new Reference2ObjectOpenHashMap<>();

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getRenderData(ContextKey<T> key) {
        return (T) extensions.get(key);
    }

    @Override
    public <T> void setRenderData(ContextKey<T> key, @Nullable T data) {
        if (data != null) {
            extensions.put(key, data);
        } else {
            extensions.remove(key);
        }
    }

    @ApiStatus.Internal
    public void resetRenderData() {
        extensions.clear();
    }

    /// Changes the visibility of a specific model part after [net.minecraft.client.model.Model#setupAnim(java.lang.Object)] is called, but before rendering is performed.
    /// This allows for hiding parts that are not normally configurable by the render state, for example hiding the player's head.
    ///
    /// @param modelPart Name of the model part. This will be looked up from [net.minecraft.client.model.Model#root()] via [net.minecraft.client.model.geom.ModelPart#getChild]
    /// @param visible `false` to disable rendering of the part.
    ///
    /// @see net.minecraft.client.model.geom.PartNames
    public void overrideModelPartVisibility(String modelPart, boolean visible) {
        Object2BooleanMap<String> visibility = getRenderData(MODEL_PART_VISIBILITY);
        if (visibility == null) {
            visibility = new Object2BooleanOpenHashMap<>();
            setRenderData(MODEL_PART_VISIBILITY, visibility);
        }
        visibility.put(modelPart, visible);
    }

    /// {@return the model part visibility or `null` if all parts have their default visibility}
    @Nullable
    @ApiStatus.Internal
    public Object2BooleanMap<String> getModelPartVisibility() {
        return getRenderData(MODEL_PART_VISIBILITY);
    }
}
