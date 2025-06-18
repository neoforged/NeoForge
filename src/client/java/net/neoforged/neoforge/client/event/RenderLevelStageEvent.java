/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.IRenderableSection;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * Fires at various times during LevelRenderer.renderLevel.
 * Check {@link #getStage} to render during the appropriate time for your use case.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}. </p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}. </p>
 */
public class RenderLevelStageEvent extends Event {
    private final Stage stage;
    private final Level level;
    private final LevelRenderer levelRenderer;
    private final PoseStack poseStack;
    private final Matrix4f modelViewMatrix;
    private final int renderTick;
    private final DeltaTracker partialTick;
    private final Camera camera;
    private final Frustum frustum;
    private final Iterable<? extends IRenderableSection> renderableSections;

    public RenderLevelStageEvent(Stage stage, Level level, LevelRenderer levelRenderer, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, int renderTick, DeltaTracker partialTick, Camera camera, Frustum frustum, Iterable<? extends IRenderableSection> renderableSections) {
        this.stage = stage;
        this.level = level;
        this.levelRenderer = levelRenderer;
        this.poseStack = poseStack != null ? poseStack : new PoseStack();
        this.modelViewMatrix = modelViewMatrix;
        this.renderTick = renderTick;
        this.partialTick = partialTick;
        this.camera = camera;
        this.frustum = frustum;
        this.renderableSections = renderableSections;
    }

    /**
     * {@return the current {@linkplain Stage stage} that is being rendered. Check this before doing rendering to ensure
     * that rendering happens at the appropriate time.}
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * {@return the current {@linkplain Level level} that is being rendered.}
     */
    public Level getLevel() {
        return level;
    }

    /**
     * {@return the level renderer}
     */
    public LevelRenderer getLevelRenderer() {
        return levelRenderer;
    }

    /**
     * {@return the pose stack used for rendering}
     */
    public PoseStack getPoseStack() {
        return poseStack;
    }

    /**
     * {@return the model view matrix used for rendering}
     */
    public Matrix4f getModelViewMatrix() {
        return modelViewMatrix;
    }

    /**
     * {@return the current "ticks" value in the {@linkplain LevelRenderer level renderer}}
     */
    public int getRenderTick() {
        return renderTick;
    }

    /**
     * {@return the current partialTick value used for rendering}
     */
    public DeltaTracker getPartialTick() {
        return partialTick;
    }

    /**
     * {@return the camera}
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * {@return the frustum}
     */
    public Frustum getFrustum() {
        return frustum;
    }

    /**
     * Returns an iterable of all visible sections.
     *
     * Calling {@link Iterable#forEach(Consumer)} on the returned iterable allows the underlying renderer
     * to optimize how it fetches the visible sections, and is recommended.
     */
    public Iterable<? extends IRenderableSection> getRenderableSections() {
        return renderableSections;
    }

    /**
     * Use to create a custom {@linkplain RenderLevelStageEvent.Stage stages}.
     * Fired after the LevelRenderer has been created.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}. </p>
     *
     * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}. </p>
     */
    public static class RegisterStageEvent extends Event implements IModBusEvent {
        /**
         * @param name The name of your Stage.
         */
        public Stage register(ResourceLocation name) throws IllegalArgumentException {
            return Stage.register(name, null);
        }
    }

    /**
     * A time during level rendering for you to render custom things into the world.
     * 
     * @see RegisterStageEvent
     */
    public static class Stage {
        private static final Map<ChunkSectionLayerGroup, Stage> CHUNK_LAYER_STAGES = new EnumMap<>(ChunkSectionLayerGroup.class);

        /**
         * Use this to render custom objects into the skybox.
         * Called regardless of if they sky actually renders or not.
         */
        public static final Stage AFTER_SKY = register("after_sky", null);
        /**
         * Use this to render custom block-like geometry into the world.
         */
        public static final Stage AFTER_OPAQUE_BLOCKS = register("after_solid_blocks", ChunkSectionLayerGroup.OPAQUE);
        /**
         * Use this to render custom block-like geometry into the world.
         */
        public static final Stage AFTER_ENTITIES = register("after_entities", null);
        /**
         * Use this to render custom block-like geometry into the world.
         */
        public static final Stage AFTER_BLOCK_ENTITIES = register("after_block_entities", null);
        /**
         * Use this to render custom block-like geometry into the world.
         * Due to how transparency sorting works, this stage may not work properly with translucency. If you intend to render translucency,
         * try using {@link #AFTER_TRIPWIRE_BLOCKS} or {@link #AFTER_PARTICLES}.
         * Although this is called within a fabulous graphics target, it does not function properly in many cases.
         */
        public static final Stage AFTER_TRANSLUCENT_BLOCKS = register("after_translucent_blocks", ChunkSectionLayerGroup.TRANSLUCENT);
        /**
         * Use this to render custom block-like geometry into the world.
         */
        public static final Stage AFTER_TRIPWIRE_BLOCKS = register("after_tripwire_blocks", ChunkSectionLayerGroup.TRIPWIRE);
        /**
         * Use this to render custom effects into the world, such as custom entity-like objects or special rendering effects.
         * Called within a fabulous graphics target.
         * Happens after entities render.
         *
         * @see NeoForgeRenderTypes#TRANSLUCENT_ON_PARTICLES_TARGET
         */
        public static final Stage AFTER_PARTICLES = register("after_particles", null);
        /**
         * Use this to render custom weather effects into the world.
         * Called within a fabulous graphics target.
         */
        public static final Stage AFTER_WEATHER = register("after_weather", null);
        /**
         * Use this to render after everything in the level has been rendered.
         * Called after {@link LevelRenderer#renderLevel(float, long, boolean, Camera, GameRenderer, LightTexture, Matrix4f, Matrix4f)} finishes.
         */
        public static final Stage AFTER_LEVEL = register("after_level", null);

        private final String name;

        private Stage(String name) {
            this.name = name;
        }

        private static Stage register(ResourceLocation name, @Nullable ChunkSectionLayerGroup layerGroup) throws IllegalArgumentException {
            Stage stage = new Stage(name.toString());
            if (layerGroup != null && CHUNK_LAYER_STAGES.putIfAbsent(layerGroup, stage) != null)
                throw new IllegalArgumentException("Attempted to replace an existing RenderLevelStageEvent.Stage for a ChunkSectionLayerGroup: Stage = " + stage + ", ChunkSectionLayerGroup = " + layerGroup);
            return stage;
        }

        private static Stage register(String name, @Nullable ChunkSectionLayerGroup layerGroup) throws IllegalArgumentException {
            return register(ResourceLocation.parse(name), layerGroup);
        }

        @Override
        public String toString() {
            return this.name;
        }

        /**
         * {@return the {@linkplain Stage stage} bound to the {@linkplain RenderType render type}, or null if no value is present}
         */
        public static Stage fromChunkLayerGroup(ChunkSectionLayerGroup layerGroup) {
            return CHUNK_LAYER_STAGES.get(layerGroup);
        }
    }
}
