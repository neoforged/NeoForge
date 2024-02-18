/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.model.lighting.LightPipelineAwareModelBlockRenderer;
import net.neoforged.neoforge.client.model.lighting.QuadLighter;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This event can be used to add static geometry to chunk sections. The event is fired on the main client thread
 * whenever a section is queued for (re)building. A rebuild can be triggered manually using e.g.
 * {@link net.minecraft.client.renderer.LevelRenderer#setSectionDirty(int, int, int)}.<br>
 *
 * While the event itself is fired on the main client thread, the renderers registered using
 * {@link net.neoforged.neoforge.client.event.AddSectionGeometryEvent#addRenderer(net.neoforged.neoforge.client.event.AddSectionGeometryEvent.AdditionalSectionRenderer)}
 * while be executed on the thread performing the rebuild, which will typically <b>not</b> be the main thread.
 * Therefore, any data from non-thread-safe data structures need to be retrieved during the event handler itself rather
 * than the renderer. A typical usage would look like
 * <pre>
 * {@code
 * @SubscribeEvent
 * public static void addChunkGeometry(AddSectionGeometryEvent ev) {
 *     if (shouldAddGeometryTo(ev.getLevel(), ev.getSectionOrigin())) {
 *         final var renderingData = getDataOnMainThread(ev.getLevel(), ev.getSectionOrigin());
 *         ev.addRenderer(context -> renderThreadsafe(renderingData, context));
 *     }
 * }
 * }
 * </pre>
 * Note that the renderer is only added if something will actually be rendered in this example. This structure should be
 * replicated whenever the event is used, to allow for optimizations related to entirely empty sections.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus}, only on the
 * {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class AddSectionGeometryEvent extends Event {
    private final List<AdditionalSectionRenderer> additionalRenderers = new ArrayList<>();
    private final BlockPos sectionOrigin;
    private final Level level;

    public AddSectionGeometryEvent(BlockPos sectionOrigin, Level level) {
        this.sectionOrigin = sectionOrigin;
        this.level = level;
    }

    /**
     * Adds a renderer which will add geometry to the chunk section.
     * @param renderer the renderer to add
     */
    public void addRenderer(AdditionalSectionRenderer renderer) {
        additionalRenderers.add(renderer);
    }

    /**
     * @return the list of all added renderers. Do not modify the result.
     */
    public List<AdditionalSectionRenderer> getAdditionalRenderers() {
        return additionalRenderers;
    }

    /**
     * @return the origin of the section to add renderers to, i.e. the block with the smallest coordinates contained in
     * the section.
     */
    public BlockPos getSectionOrigin() {
        return sectionOrigin;
    }

    /**
     * @return the level to render in. This can differ from the current client level in case of e.g. guidebooks.
     */
    public Level getLevel() {
        return level;
    }

    /**
     * A rendering callback that will be invoked during chunk meshing.
     */
    @FunctionalInterface
    public interface AdditionalSectionRenderer {
        void render(SectionRenderingContext context);
    }

    public static final class SectionRenderingContext {
        private final Set<RenderType> layers;
        private final SectionBufferBuilderPack buffers;
        private final BlockAndTintGetter region;
        private final PoseStack poseStack;

        /**
         * @param layers the set of currently present render types in this chunk. This will be modified whenever new
         *                     types are added!
         * @param buffers the collection of buffers rendering to the current section
         * @param region a view of the section and some surrounding blocks
         * @param poseStack the transformations to use, currently set to the chunk origin at unit scaling and no
         *                        rotation.
         */
        public SectionRenderingContext(
            Set<RenderType> layers, SectionBufferBuilderPack buffers, BlockAndTintGetter region, PoseStack poseStack
        ) {
            this.layers = layers;
            this.buffers = buffers;
            this.region = region;
            this.poseStack = poseStack;
        }

        /**
         * Returns the builder for the given render type/layer in the chunk section. If the render type is not already
         * present in the section, marks the type as present in the section.
         * @param type the render type to retrieve a builder for
         * @return a vertex consumer adding geometry of the specified layer
         */
        public VertexConsumer getOrCreateBuffer(RenderType type) {
            BufferBuilder builder = buffers.builder(type);
            if(layers.add(type))
                builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            return builder;
        }

        /**
         * @param smooth whether a lighter for "smooth"/"ambient occlusion" lighting should be returned rather than a
         *                     "flat" one
         * @return a quad lighter usable on the current thread
         */
        public QuadLighter getQuadLighter(boolean smooth) {
            final var renderer = (LightPipelineAwareModelBlockRenderer) Minecraft.getInstance().getBlockRenderer().getModelRenderer();
            return renderer.getQuadLighter(smooth);
        }

        public PoseStack getPoseStack() {
            return poseStack;
        }

        /**
         * @return the "view" on the client world used in the current chunk meshing thread. This will generally only
         * contain blocks in a small radius around the section being rendered.
         */
        public BlockAndTintGetter getRegion() { return region; }
    }
}
