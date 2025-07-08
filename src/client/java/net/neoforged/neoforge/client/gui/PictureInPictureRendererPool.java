/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Pools {@link PictureInPictureRenderer} for a single type of {@link PictureInPictureRenderState} and tries
 * to reuse renderers on subsequent frames.
 * <p>Vanilla only ever uses one PIP renderer per PIP state type. This can lead to crashes or
 * visual artifacts, since the backing render target textures are stored within the renderer,
 * and if two or more of the same type of state are submitted in one frame, the states will
 * begin interfering with each other.
 * <p>We solve this by using one renderer per distinct {@link PictureInPictureRenderState} state per frame,
 * and use this class to pool them for reuse in subsequent frames.
 */
@ApiStatus.Internal
public class PictureInPictureRendererPool<T extends PictureInPictureRenderState> implements AutoCloseable {
    private final PictureInPictureRendererRegistration<T> factory;
    private final MultiBufferSource.BufferSource buffers;
    // The renderers from last frame, which we will try to reuse this frame
    private Object2ObjectMap<T, PictureInPictureRenderer<T>> renderersLastFrame = new Object2ObjectOpenHashMap<>();
    // The renderers we already used in this frame, which we will try to reuse next frame
    private Object2ObjectMap<T, PictureInPictureRenderer<T>> renderersThisFrame = new Object2ObjectOpenHashMap<>();

    public PictureInPictureRendererPool(PictureInPictureRendererRegistration<T> factory,
            MultiBufferSource.BufferSource buffers) {
        this.factory = factory;
        this.buffers = buffers;
    }

    @Nullable
    public PictureInPictureRenderer<T> get(T state, int guiScale, boolean firstPass) {
        var width = (state.x1() - state.x0()) * guiScale;
        var height = (state.y1() - state.y0()) * guiScale;

        // On the first pass just try to reuse existing renderers by state equality
        if (firstPass) {
            var renderer = renderersLastFrame.get(state);
            if (renderer != null && renderer.canBeReusedFor(state, width, height)) {
                renderersLastFrame.remove(state);
                renderersThisFrame.put(state, renderer);
                return renderer;
            }
            return null;
        }

        // On the second pass, we try to find a renderer of matching texture size
        var it = renderersLastFrame.values().iterator();
        while (it.hasNext()) {
            var renderer = it.next();
            if (renderer.canBeReusedFor(state, width, height)) {
                it.remove();
                renderersThisFrame.put(state, renderer);
                return renderer;
            }
        }

        // Nothing else helped, create a new one
        var renderer = factory.factory().apply(buffers);
        renderersThisFrame.put(state, renderer);
        return renderer;
    }

    public void clearUnusedRenderers() {
        renderersLastFrame.values().forEach(PictureInPictureRenderer::close);
        renderersLastFrame.clear();

        // Swap back/front buffer of maps, if you will
        var tmp = renderersLastFrame;
        renderersLastFrame = renderersThisFrame;
        renderersThisFrame = tmp;
    }

    @Override
    public void close() {
        renderersThisFrame.values().forEach(PictureInPictureRenderer::close);
        renderersLastFrame.values().forEach(PictureInPictureRenderer::close);
    }

    public static Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererPool<?>> createPools(MultiBufferSource.BufferSource bufferSource, List<PictureInPictureRendererRegistration<?>> pipRendererFactories) {
        ImmutableMap.Builder<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererPool<?>> builder = ImmutableMap.builder();

        for (var factory : pipRendererFactories) {
            builder.put(factory.stateClass(), new PictureInPictureRendererPool<>(factory, bufferSource));
        }

        return builder.buildOrThrow();
    }
}
