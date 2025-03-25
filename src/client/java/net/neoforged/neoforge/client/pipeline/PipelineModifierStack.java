/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.pipeline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class PipelineModifierStack {
    private final Deque<ResourceKey<PipelineModifier>> modifiers = new ArrayDeque<>();
    private final Map<ResourceKey<PipelineModifier>, Map<RenderPipeline, RenderPipeline>> modifierTransformCache = new Reference2ReferenceOpenHashMap<>();

    public void renderWithModifier(ResourceKey<PipelineModifier> modifier, Runnable renderTask) {
        this.push(modifier);
        renderTask.run();
        this.pop();
    }

    public void push(ResourceKey<PipelineModifier> modifier) {
        RenderSystem.assertOnRenderThread();
        this.modifiers.push(modifier);
    }

    public void pop() {
        RenderSystem.assertOnRenderThread();
        this.modifiers.pop();
    }

    public void ensureEmpty() {
        RenderSystem.assertOnRenderThread();
        if (!this.modifiers.isEmpty()) {
            throw new IllegalStateException("Modifier stack is not empty: " + this.modifiers);
        }
    }

    public RenderPipeline apply(RenderPipeline pipeline) {
        RenderSystem.assertOnRenderThread();
        if (this.modifiers.isEmpty()) {
            return pipeline;
        }

        for (ResourceKey<PipelineModifier> modifier : this.modifiers) {
            Map<RenderPipeline, RenderPipeline> xformCache = this.modifierTransformCache.computeIfAbsent(modifier, $ -> new Reference2ReferenceOpenHashMap<>());
            RenderPipeline newPipeline = xformCache.get(pipeline);
            if (newPipeline == null) {
                ResourceLocation name = pipeline.getLocation().withSuffix("/transform/" + modifier.location().toString().replace(":", "/"));
                newPipeline = PipelineModifiers.MODIFIERS.get(modifier).apply(pipeline, name);
                if (newPipeline != pipeline && newPipeline.getLocation().equals(pipeline.getLocation())) {
                    throw new IllegalStateException(String.format(
                            Locale.ROOT,
                            "Modified pipeline %s must use a unique location instead of the incoming pipeline's location %s, ideally the provided location %s",
                            newPipeline,
                            pipeline.getLocation(),
                            name));
                }
                xformCache.put(pipeline, newPipeline);
            }
            pipeline = newPipeline;
        }
        return pipeline;
    }
}
