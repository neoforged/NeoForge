/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class PictureInPictureRendererPool<T extends PictureInPictureRenderState> implements AutoCloseable {
    private final PictureInPictureRendererRegistration<T> factory;
    private final MultiBufferSource.BufferSource buffers;
    private Map<T, PictureInPictureRenderer<T>> renderersLastFrame = new HashMap<>();
    private Map<T, PictureInPictureRenderer<T>> renderersThisFrame = new HashMap<>();

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
            var renderer = renderersLastFrame.remove(state);
            if (renderer != null && renderer.canBeReusedFor(state, width, height)) {
                renderersThisFrame.put(state, renderer);
            }
            return renderer;
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
