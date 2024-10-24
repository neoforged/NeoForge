/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Options;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.Registry;
import net.minecraft.locale.Language;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.sounds.Music;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentModel;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.neoforge.client.buffer.VanillaBufferDefinitions;
import net.neoforged.neoforge.client.buffer.chunk.ChunkBufferDefinitionManager;
import net.neoforged.neoforge.client.buffer.chunk.IChunkBufferCallback;
import net.neoforged.neoforge.client.buffer.chunk.ISectionLayerRenderer;
import net.neoforged.neoforge.client.entity.animation.json.AnimationTypeManager;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.CalculatePlayerTurnEvent;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPauseChangeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerChangeGameTypeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.GatherEffectScreenTooltipsEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ScreenshotEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.client.event.ToastAddEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.client.extensions.common.ClientExtensionsManager;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.gui.ClientTooltipComponentManager;
import net.neoforged.neoforge.client.gui.GuiLayerManager;
import net.neoforged.neoforge.client.gui.map.MapDecorationRendererManager;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.forge.snapshots.ForgeSnapshotsModClient;
import net.neoforged.neoforge.gametest.GameTestHooks;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Class for various client-side-only hooks.
 */
public class ClientHooks {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker CLIENTHOOKS = MarkerManager.getMarker("CLIENTHOOKS");

    //private static final ResourceLocation ITEM_GLINT = ResourceLocation.withDefaultNamespace("textures/misc/enchanted_item_glint.png");

    /**
     * Contains the *extra* GUI layers.
     * The current top layer stays in Minecraft#currentScreen, and the rest serve as a background for it.
     */
    private static final Stack<Screen> guiLayers = new Stack<>();

    public static void resizeGuiLayers(Minecraft minecraft, int width, int height) {
        guiLayers.forEach(screen -> screen.resize(minecraft, width, height));
    }

    public static void clearGuiLayers(Minecraft minecraft) {
        while (!guiLayers.isEmpty())
            popGuiLayerInternal(minecraft);
    }

    private static void popGuiLayerInternal(Minecraft minecraft) {
        if (minecraft.screen != null)
            minecraft.screen.removed();
        minecraft.screen = guiLayers.pop();
    }

    public static void pushGuiLayer(Minecraft minecraft, Screen screen) {
        if (minecraft.screen != null)
            guiLayers.push(minecraft.screen);
        minecraft.screen = Objects.requireNonNull(screen);
        screen.init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
        minecraft.getNarrator().sayNow(screen.getNarrationMessage());
    }

    public static void popGuiLayer(Minecraft minecraft) {
        if (guiLayers.isEmpty()) {
            minecraft.setScreen(null);
            return;
        }

        popGuiLayerInternal(minecraft);
        if (minecraft.screen != null)
            minecraft.getNarrator().sayNow(minecraft.screen.getNarrationMessage());
    }

    public static float getGuiFarPlane() {
        // 11000 units for the overlay background and 10000 units for each layered Screen or 200 units for each HUD layer, whichever ends up higher

        float depth = 10_000F * (1 + guiLayers.size());
        if (Minecraft.getInstance().level != null) {
            depth = Math.max(depth, GuiLayerManager.Z_SEPARATION * Minecraft.getInstance().gui.getLayerCount());
        }
        return 11_000F + depth;
    }

    public static ResourceLocation getArmorTexture(ItemStack armor, EquipmentModel.LayerType type, EquipmentModel.Layer layer, ResourceLocation _default) {
        ResourceLocation result = armor.getItem().getArmorTexture(armor, type, layer, _default);
        return result != null ? result : _default;
    }

    public static boolean onClientPauseChangePre(boolean pause) {
        var event = NeoForge.EVENT_BUS.post(new ClientPauseChangeEvent.Pre(pause));
        return event.isCanceled();
    }

    public static void onClientPauseChangePost(boolean pause) {
        NeoForge.EVENT_BUS.post(new ClientPauseChangeEvent.Post(pause));
    }

    public static boolean onDrawHighlight(LevelRenderer context, Camera camera, BlockHitResult target, DeltaTracker deltaTracker, PoseStack poseStack, MultiBufferSource bufferSource, boolean forTranslucentBlocks) {
        return NeoForge.EVENT_BUS.post(new RenderHighlightEvent.Block(context, camera, target, deltaTracker, poseStack, bufferSource, forTranslucentBlocks)).isCanceled();
    }

    public static void dispatchRenderStage(RenderLevelStageEvent.Stage stage, LevelRenderer levelRenderer, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, int renderTick, Camera camera, Frustum frustum) {
        var mc = Minecraft.getInstance();
        var profiler = Profiler.get();

        profiler.push(stage.toString());
        NeoForge.EVENT_BUS.post(new RenderLevelStageEvent(stage, levelRenderer, poseStack, modelViewMatrix, projectionMatrix, renderTick, mc.getDeltaTracker(), camera, frustum));
        dispatchCustomChunkBuffers(stage, levelRenderer, modelViewMatrix, projectionMatrix, camera.getPosition().toVector3f());
        profiler.pop();
    }

    public static void dispatchCustomChunkBuffers(RenderLevelStageEvent.Stage stage, LevelRenderer levelRenderer, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, Vector3f position) {
        ChunkBufferDefinitionManager.getChunkBufferDefinitions(stage).forEach(entry -> {
            RenderType renderType = VanillaBufferDefinitions.bakeVanillaRenderType(entry.bufferDefinition());
            IChunkBufferCallback callback = entry.callback();
            ISectionLayerRenderer sectionLayerRenderer = ISectionLayerRenderer.vanilla(renderType, levelRenderer);

            callback.onRenderChunkBuffer(
                    sectionLayerRenderer,
                    levelRenderer,
                    entry.bufferDefinition(),
                    position.x,
                    position.y,
                    position.z,
                    modelViewMatrix,
                    projectionMatrix);
            levelRenderer.renderBuffers.bufferSource().endBatch(renderType);
        });
    }

    public static void dispatchRenderStage(RenderType renderType, LevelRenderer levelRenderer, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, int renderTick, Camera camera, Frustum frustum) {
        RenderLevelStageEvent.Stage stage = RenderLevelStageEvent.Stage.fromRenderType(renderType);
        if (stage != null)
            dispatchRenderStage(stage, levelRenderer, null, modelViewMatrix, projectionMatrix, renderTick, camera, frustum);
    }

    public static boolean renderSpecificFirstPersonHand(InteractionHand hand, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick, float interpPitch, float swingProgress, float equipProgress, ItemStack stack) {
        return NeoForge.EVENT_BUS.post(new RenderHandEvent(hand, poseStack, bufferSource, packedLight, partialTick, interpPitch, swingProgress, equipProgress, stack)).isCanceled();
    }

    public static boolean renderSpecificFirstPersonArm(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, AbstractClientPlayer player, HumanoidArm arm) {
        return NeoForge.EVENT_BUS.post(new RenderArmEvent(poseStack, multiBufferSource, packedLight, player, arm)).isCanceled();
    }

    public static void onTextureAtlasStitched(TextureAtlas atlas) {
        ModLoader.postEvent(new TextureAtlasStitchedEvent(atlas));
    }

    public static void onBlockColorsInit(BlockColors blockColors) {
        ModLoader.postEvent(new RegisterColorHandlersEvent.Block(blockColors));
    }

    public static void onItemColorsInit(ItemColors itemColors, BlockColors blockColors) {
        ModLoader.postEvent(new RegisterColorHandlersEvent.Item(itemColors, blockColors));
    }

    public static Model getArmorModel(ItemStack itemStack, EquipmentModel.LayerType layerType, Model _default) {
        return IClientItemExtensions.of(itemStack).getGenericArmorModel(itemStack, layerType, _default);
    }

    /** Copies humanoid model properties from the original model to another, used for armor models */
    @SuppressWarnings("unchecked")
    public static <T extends HumanoidRenderState> void copyModelProperties(HumanoidModel<T> original, HumanoidModel<?> replacement) {
        // this function does not make use of the <T> generic, so the unchecked cast should be safe
        original.copyPropertiesTo((HumanoidModel<T>) replacement);
        replacement.head.visible = original.head.visible;
        replacement.hat.visible = original.hat.visible;
        replacement.body.visible = original.body.visible;
        replacement.rightArm.visible = original.rightArm.visible;
        replacement.leftArm.visible = original.leftArm.visible;
        replacement.rightLeg.visible = original.rightLeg.visible;
        replacement.leftLeg.visible = original.leftLeg.visible;
    }

    //This properly moves the domain, if provided, to the front of the string before concatenating
    public static String fixDomain(String base, String complex) {
        int idx = complex.indexOf(':');
        if (idx == -1) {
            return base + complex;
        }

        String name = complex.substring(idx + 1, complex.length());
        if (idx > 1) {
            String domain = complex.substring(0, idx);
            return domain + ':' + base + name;
        } else {
            return base + name;
        }
    }

    public static float getFieldOfViewModifier(Player entity, float fovModifier, float fovScale) {
        ComputeFovModifierEvent fovModifierEvent = new ComputeFovModifierEvent(entity, fovModifier, fovScale);
        NeoForge.EVENT_BUS.post(fovModifierEvent);
        return fovModifierEvent.getNewFovModifier();
    }

    public static float getFieldOfView(GameRenderer renderer, Camera camera, float partialTick, float fov, boolean usedConfiguredFov) {
        ViewportEvent.ComputeFov event = new ViewportEvent.ComputeFov(renderer, camera, partialTick, fov, usedConfiguredFov);
        NeoForge.EVENT_BUS.post(event);
        return event.getFOV();
    }

    public static CalculatePlayerTurnEvent getTurnPlayerValues(double mouseSensitivity, boolean cinematicCameraEnabled) {
        var event = new CalculatePlayerTurnEvent(mouseSensitivity, cinematicCameraEnabled);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static float getDetachedCameraDistance(Camera camera, boolean flipped, float entityScale, float distance) {
        var event = new CalculateDetachedCameraDistanceEvent(camera, flipped, entityScale, distance);
        NeoForge.EVENT_BUS.post(event);
        return event.getDistance();
    }

    /**
     * Initialization of Forge Renderers.
     */
    static {
        //FluidRegistry.renderIdFluid = RenderingRegistry.getNextAvailableRenderId();
        //RenderingRegistry.registerBlockHandler(RenderBlockFluid.instance);
    }

    public static void renderMainMenu(TitleScreen gui, GuiGraphics guiGraphics, Font font, int width, int height, int alpha) {
        ForgeSnapshotsModClient.renderMainMenuWarning(NeoForgeVersion.getVersion(), guiGraphics, font, width, height, alpha);

        forgeStatusLine = switch (NeoForgeVersion.getStatus()) {
            // case FAILED -> " Version check failed";
            // case UP_TO_DATE -> "Forge up to date";
            // case AHEAD -> "Using non-recommended Forge build, issues may arise.";
            case OUTDATED, BETA_OUTDATED -> I18n.get("neoforge.update.newversion", NeoForgeVersion.getTarget());
            default -> null;
        };
    }

    public static String forgeStatusLine;

    @Nullable
    public static SoundInstance playSound(SoundEngine manager, SoundInstance sound) {
        PlaySoundEvent e = new PlaySoundEvent(manager, sound);
        NeoForge.EVENT_BUS.post(e);
        return e.getSound();
    }

    @Nullable
    public static Music selectMusic(Music situational, @Nullable SoundInstance playing) {
        SelectMusicEvent e = new SelectMusicEvent(situational, playing);
        NeoForge.EVENT_BUS.post(e);
        return e.getMusic();
    }

    public static void drawScreen(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.pose().pushPose();
        guiLayers.forEach(layer -> {
            // Prevent the background layers from thinking the mouse is over their controls and showing them as highlighted.
            drawScreenInternal(layer, guiGraphics, Integer.MAX_VALUE, Integer.MAX_VALUE, partialTick);
            guiGraphics.pose().translate(0, 0, 10000);
        });
        drawScreenInternal(screen, guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().popPose();
    }

    private static void drawScreenInternal(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!NeoForge.EVENT_BUS.post(new ScreenEvent.Render.Pre(screen, guiGraphics, mouseX, mouseY, partialTick)).isCanceled())
            screen.renderWithTooltip(guiGraphics, mouseX, mouseY, partialTick);
        NeoForge.EVENT_BUS.post(new ScreenEvent.Render.Post(screen, guiGraphics, mouseX, mouseY, partialTick));
    }

    public static Vector4f getFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, float fogRed, float fogGreen, float fogBlue) {
        // Modify fog color depending on the fluid
        FluidState state = level.getFluidState(camera.getBlockPosition());
        Vector4f fluidFogColor = new Vector4f(fogRed, fogGreen, fogBlue, 1F);
        if (camera.getPosition().y < (double) ((float) camera.getBlockPosition().getY() + state.getHeight(level, camera.getBlockPosition())))
            fluidFogColor = IClientFluidTypeExtensions.of(state).modifyFogColor(camera, partialTick, level, renderDistance, darkenWorldAmount, fluidFogColor);

        ViewportEvent.ComputeFogColor event = new ViewportEvent.ComputeFogColor(camera, partialTick, fluidFogColor.x(), fluidFogColor.y(), fluidFogColor.z());
        NeoForge.EVENT_BUS.post(event);

        fluidFogColor.set(event.getRed(), event.getGreen(), event.getBlue());
        return fluidFogColor;
    }

    public static FogParameters onFogRender(FogRenderer.FogMode mode, FogType type, Camera camera, float partialTick, float renderDistance, FogParameters fogParameters) {
        // Modify fog rendering depending on the fluid
        FluidState state = camera.getEntity().level().getFluidState(camera.getBlockPosition());
        if (camera.getPosition().y < (double) ((float) camera.getBlockPosition().getY() + state.getHeight(camera.getEntity().level(), camera.getBlockPosition())))
            fogParameters = IClientFluidTypeExtensions.of(state).modifyFogRender(camera, mode, renderDistance, partialTick, fogParameters);

        ViewportEvent.RenderFog event = new ViewportEvent.RenderFog(mode, type, camera, partialTick, fogParameters);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            return new FogParameters(
                    event.getNearPlaneDistance(),
                    event.getFarPlaneDistance(),
                    event.getFogShape(),
                    fogParameters.red(),
                    fogParameters.green(),
                    fogParameters.blue(),
                    fogParameters.alpha());
        }
        return fogParameters;
    }

    public static void onModifyBakingResult(Map<ModelResourceLocation, BakedModel> models, Map<ResourceLocation, AtlasSet.StitchResult> stitchResults, ModelBakery modelBakery) {
        Function<Material, TextureAtlasSprite> textureGetter = material -> {
            AtlasSet.StitchResult stitchResult = stitchResults.get(material.atlasLocation());
            TextureAtlasSprite sprite = stitchResult.getSprite(material.texture());
            if (sprite != null) {
                return sprite;
            }
            LOGGER.warn("Failed to retrieve texture '{}' from atlas '{}'", material.texture(), material.atlasLocation(), new Throwable());
            return stitchResult.missing();
        };
        ModLoader.postEvent(new ModelEvent.ModifyBakingResult(models, textureGetter, modelBakery));
    }

    public static void onModelBake(ModelManager modelManager, Map<ModelResourceLocation, BakedModel> models, ModelBakery modelBakery) {
        ModLoader.postEvent(new ModelEvent.BakingCompleted(modelManager, Collections.unmodifiableMap(models), modelBakery));
    }

    public static BakedModel handleCameraTransforms(PoseStack poseStack, BakedModel model, ItemDisplayContext cameraTransformType, boolean applyLeftHandTransform) {
        model = model.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform);
        return model;
    }

    @SuppressWarnings("deprecation")
    public static Material getBlockMaterial(ResourceLocation loc) {
        return new Material(TextureAtlas.LOCATION_BLOCKS, loc);
    }

    /**
     * internal, relies on fixed format of FaceBakery
     */
    // TODO Do we need this?
    public static void fillNormal(int[] faceData, Direction facing) {
        Vector3f v1 = getVertexPos(faceData, 3);
        Vector3f t1 = getVertexPos(faceData, 1);
        Vector3f v2 = getVertexPos(faceData, 2);
        Vector3f t2 = getVertexPos(faceData, 0);
        v1.sub(t1);
        v2.sub(t2);
        v2.cross(v1);
        v2.normalize();

        int x = ((byte) Math.round(v2.x() * 127)) & 0xFF;
        int y = ((byte) Math.round(v2.y() * 127)) & 0xFF;
        int z = ((byte) Math.round(v2.z() * 127)) & 0xFF;

        int normal = x | (y << 0x08) | (z << 0x10);

        for (int i = 0; i < 4; i++) {
            faceData[i * 8 + 7] = normal;
        }
    }

    private static Vector3f getVertexPos(int[] data, int vertex) {
        int idx = vertex * 8;

        float x = Float.intBitsToFloat(data[idx]);
        float y = Float.intBitsToFloat(data[idx + 1]);
        float z = Float.intBitsToFloat(data[idx + 2]);

        return new Vector3f(x, y, z);
    }

    public static boolean calculateFaceWithoutAO(BlockAndTintGetter getter, BlockState state, BlockPos pos, BakedQuad quad, boolean isFaceCubic, float[] brightness, int[] lightmap) {
        if (quad.hasAmbientOcclusion())
            return false;

        BlockPos lightmapPos = isFaceCubic ? pos.relative(quad.getDirection()) : pos;

        brightness[0] = brightness[1] = brightness[2] = brightness[3] = getter.getShade(quad.getDirection(), quad.isShade());
        lightmap[0] = lightmap[1] = lightmap[2] = lightmap[3] = LevelRenderer.getLightColor(getter, state, lightmapPos);
        return true;
    }

    public static void loadEntityShader(@Nullable Entity entity, GameRenderer gameRenderer) {
        if (entity != null) {
            ResourceLocation shader = EntitySpectatorShaderManager.get(entity.getType());
            if (shader != null) {
                gameRenderer.setPostEffect(shader);
            }
        }
    }

    private static int slotMainHand = 0;

    public static boolean shouldCauseReequipAnimation(ItemStack from, ItemStack to, int slot) {
        boolean fromInvalid = from.isEmpty();
        boolean toInvalid = to.isEmpty();

        if (fromInvalid && toInvalid) return false;
        if (fromInvalid || toInvalid) return true;

        boolean changed = false;
        if (slot != -1) {
            changed = slot != slotMainHand;
            slotMainHand = slot;
        }
        return from.getItem().shouldCauseReequipAnimation(from, to, changed);
    }

    public static CustomizeGuiOverlayEvent.BossEventProgress onCustomizeBossEventProgress(GuiGraphics guiGraphics, Window window, LerpingBossEvent bossInfo, int x, int y, int increment) {
        CustomizeGuiOverlayEvent.BossEventProgress evt = new CustomizeGuiOverlayEvent.BossEventProgress(window, guiGraphics,
                Minecraft.getInstance().getDeltaTracker(), bossInfo, x, y, increment);
        NeoForge.EVENT_BUS.post(evt);
        return evt;
    }

    public static ScreenshotEvent onScreenshot(NativeImage image, File screenshotFile) {
        ScreenshotEvent event = new ScreenshotEvent(image, screenshotFile);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static void onClientChangeGameType(PlayerInfo info, GameType currentGameMode, GameType newGameMode) {
        if (currentGameMode != newGameMode) {
            ClientPlayerChangeGameTypeEvent evt = new ClientPlayerChangeGameTypeEvent(info, currentGameMode, newGameMode);
            NeoForge.EVENT_BUS.post(evt);
        }
    }

    public static void onMovementInputUpdate(Player player, ClientInput movementInput) {
        NeoForge.EVENT_BUS.post(new MovementInputUpdateEvent(player, movementInput));
    }

    public static boolean onScreenMouseClickedPre(Screen guiScreen, double mouseX, double mouseY, int button) {
        var event = new ScreenEvent.MouseButtonPressed.Pre(guiScreen, mouseX, mouseY, button);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static boolean onScreenMouseClickedPost(Screen guiScreen, double mouseX, double mouseY, int button, boolean handled) {
        var event = new ScreenEvent.MouseButtonPressed.Post(guiScreen, mouseX, mouseY, button, handled);
        NeoForge.EVENT_BUS.post(event);
        return event.getClickResult();
    }

    public static boolean onScreenMouseReleasedPre(Screen guiScreen, double mouseX, double mouseY, int button) {
        var event = new ScreenEvent.MouseButtonReleased.Pre(guiScreen, mouseX, mouseY, button);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static boolean onScreenMouseReleasedPost(Screen guiScreen, double mouseX, double mouseY, int button, boolean handled) {
        var event = new ScreenEvent.MouseButtonReleased.Post(guiScreen, mouseX, mouseY, button, handled);
        NeoForge.EVENT_BUS.post(event);
        return event.getReleaseResult();
    }

    public static boolean onScreenMouseDragPre(Screen guiScreen, double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        var event = new ScreenEvent.MouseDragged.Pre(guiScreen, mouseX, mouseY, mouseButton, dragX, dragY);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static void onScreenMouseDragPost(Screen guiScreen, double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        Event event = new ScreenEvent.MouseDragged.Post(guiScreen, mouseX, mouseY, mouseButton, dragX, dragY);
        NeoForge.EVENT_BUS.post(event);
    }

    public static boolean onScreenMouseScrollPre(MouseHandler mouseHelper, Screen guiScreen, double scrollDeltaX, double scrollDeltaY) {
        Window mainWindow = guiScreen.getMinecraft().getWindow();
        double mouseX = mouseHelper.xpos() * (double) mainWindow.getGuiScaledWidth() / (double) mainWindow.getScreenWidth();
        double mouseY = mouseHelper.ypos() * (double) mainWindow.getGuiScaledHeight() / (double) mainWindow.getScreenHeight();
        var event = new ScreenEvent.MouseScrolled.Pre(guiScreen, mouseX, mouseY, scrollDeltaX, scrollDeltaY);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static void onScreenMouseScrollPost(MouseHandler mouseHelper, Screen guiScreen, double scrollDeltaX, double scrollDeltaY) {
        Window mainWindow = guiScreen.getMinecraft().getWindow();
        double mouseX = mouseHelper.xpos() * (double) mainWindow.getGuiScaledWidth() / (double) mainWindow.getScreenWidth();
        double mouseY = mouseHelper.ypos() * (double) mainWindow.getGuiScaledHeight() / (double) mainWindow.getScreenHeight();
        Event event = new ScreenEvent.MouseScrolled.Post(guiScreen, mouseX, mouseY, scrollDeltaX, scrollDeltaY);
        NeoForge.EVENT_BUS.post(event);
    }

    public static boolean onScreenKeyPressedPre(Screen guiScreen, int keyCode, int scanCode, int modifiers) {
        var event = new ScreenEvent.KeyPressed.Pre(guiScreen, keyCode, scanCode, modifiers);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static boolean onScreenKeyPressedPost(Screen guiScreen, int keyCode, int scanCode, int modifiers) {
        var event = new ScreenEvent.KeyPressed.Post(guiScreen, keyCode, scanCode, modifiers);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static boolean onScreenKeyReleasedPre(Screen guiScreen, int keyCode, int scanCode, int modifiers) {
        var event = new ScreenEvent.KeyReleased.Pre(guiScreen, keyCode, scanCode, modifiers);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static boolean onScreenKeyReleasedPost(Screen guiScreen, int keyCode, int scanCode, int modifiers) {
        var event = new ScreenEvent.KeyReleased.Post(guiScreen, keyCode, scanCode, modifiers);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static boolean onScreenCharTypedPre(Screen guiScreen, char codePoint, int modifiers) {
        var event = new ScreenEvent.CharacterTyped.Pre(guiScreen, codePoint, modifiers);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static void onScreenCharTypedPost(Screen guiScreen, char codePoint, int modifiers) {
        Event event = new ScreenEvent.CharacterTyped.Post(guiScreen, codePoint, modifiers);
        NeoForge.EVENT_BUS.post(event);
    }

    public static boolean onMouseButtonPre(int button, int action, int mods) {
        return NeoForge.EVENT_BUS.post(new InputEvent.MouseButton.Pre(button, action, mods)).isCanceled();
    }

    public static void onMouseButtonPost(int button, int action, int mods) {
        NeoForge.EVENT_BUS.post(new InputEvent.MouseButton.Post(button, action, mods));
    }

    public static boolean onMouseScroll(MouseHandler mouseHelper, double scrollDeltaX, double scrollDeltaY) {
        var event = new InputEvent.MouseScrollingEvent(scrollDeltaX, scrollDeltaY, mouseHelper.isLeftPressed(), mouseHelper.isMiddlePressed(), mouseHelper.isRightPressed(), mouseHelper.xpos(), mouseHelper.ypos());
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static void onKeyInput(int key, int scanCode, int action, int modifiers) {
        NeoForge.EVENT_BUS.post(new InputEvent.Key(key, scanCode, action, modifiers));
    }

    public static InputEvent.InteractionKeyMappingTriggered onClickInput(int button, KeyMapping keyBinding, InteractionHand hand) {
        InputEvent.InteractionKeyMappingTriggered event = new InputEvent.InteractionKeyMappingTriggered(button, keyBinding, hand);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static boolean isNameplateInRenderDistance(LivingEntity entity, double squareDistance) {
        double value = entity.getAttributeValue(NeoForgeMod.NAMETAG_DISTANCE);
        return !(squareDistance > value * value);
    }

    public static void renderPistonMovedBlocks(BlockPos pos, BlockState state, PoseStack stack, MultiBufferSource bufferSource, Level level, boolean checkSides, int packedOverlay, BlockRenderDispatcher blockRenderer) {
        var model = blockRenderer.getBlockModel(state);
        for (var renderType : model.getRenderTypes(state, RandomSource.create(state.getSeed(pos)), ModelData.EMPTY)) {
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderTypeHelper.getMovingBlockRenderType(renderType));
            blockRenderer.getModelRenderer().tesselateBlock(level, model, state, pos, stack, vertexConsumer, checkSides, RandomSource.create(), state.getSeed(pos), packedOverlay, ModelData.EMPTY, renderType);
        }
    }

    public static boolean shouldRenderEffect(MobEffectInstance effectInstance) {
        return IClientMobEffectExtensions.of(effectInstance).isVisibleInInventory(effectInstance);
    }

    private static final Map<ModelLayerLocation, Supplier<LayerDefinition>> layerDefinitions = new HashMap<>();

    public static void registerLayerDefinition(ModelLayerLocation layerLocation, Supplier<LayerDefinition> supplier) {
        layerDefinitions.put(layerLocation, supplier);
    }

    public static void loadLayerDefinitions(ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> builder) {
        layerDefinitions.forEach((k, v) -> builder.put(k, v.get()));
    }

    private static final ResourceLocation ICON_SHEET = ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "textures/gui/icons.png");

    public static void firePlayerLogin(MultiPlayerGameMode pc, LocalPlayer player, Connection networkManager) {
        NeoForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.LoggingIn(pc, player, networkManager));
    }

    public static void firePlayerLogout(@Nullable MultiPlayerGameMode pc, @Nullable LocalPlayer player) {
        NeoForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.LoggingOut(pc, player, player != null ? player.connection != null ? player.connection.getConnection() : null : null));
    }

    public static void firePlayerRespawn(MultiPlayerGameMode pc, LocalPlayer oldPlayer, LocalPlayer newPlayer, Connection networkManager) {
        NeoForge.EVENT_BUS.post(new ClientPlayerNetworkEvent.Clone(pc, oldPlayer, newPlayer, networkManager));
    }

    public static void onRegisterParticleProviders(ParticleEngine particleEngine) {
        ModLoader.postEvent(new RegisterParticleProvidersEvent(particleEngine));
    }

    public static void onRegisterKeyMappings(Options options) {
        ModLoader.postEvent(new RegisterKeyMappingsEvent(options));
    }

    public static void onRegisterAdditionalModels(Set<ModelResourceLocation> additionalModels) {
        ModLoader.postEvent(new ModelEvent.RegisterAdditional(additionalModels));
    }

    @Nullable
    public static Component onClientChat(ChatType.Bound boundChatType, Component message, UUID sender) {
        ClientChatReceivedEvent event = new ClientChatReceivedEvent(boundChatType, message, sender);
        return NeoForge.EVENT_BUS.post(event).isCanceled() ? null : event.getMessage();
    }

    @Nullable
    public static Component onClientPlayerChat(ChatType.Bound boundChatType, Component message, PlayerChatMessage playerChatMessage, UUID sender) {
        ClientChatReceivedEvent.Player event = new ClientChatReceivedEvent.Player(boundChatType, message, playerChatMessage, sender);
        return NeoForge.EVENT_BUS.post(event).isCanceled() ? null : event.getMessage();
    }

    @Nullable
    public static Component onClientSystemChat(Component message, boolean overlay) {
        ClientChatReceivedEvent.System event = new ClientChatReceivedEvent.System(message, overlay);
        return NeoForge.EVENT_BUS.post(event).isCanceled() ? null : event.getMessage();
    }

    public static String onClientSendMessage(String message) {
        ClientChatEvent event = new ClientChatEvent(message);
        return NeoForge.EVENT_BUS.post(event).isCanceled() ? "" : event.getMessage();
    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = "neoforge", bus = EventBusSubscriber.Bus.MOD)
    public static class ClientEvents {
        public static final ShaderProgram RENDERTYPE_ENTITY_TRANSLUCENT_UNLIT_SHADER = new ShaderProgram(
                ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "core/rendertype_entity_unlit_translucent"),
                DefaultVertexFormat.NEW_ENTITY,
                ShaderDefines.EMPTY);

        @SubscribeEvent
        public static void registerShaders(RegisterShadersEvent event) {
            event.registerShader(RENDERTYPE_ENTITY_TRANSLUCENT_UNLIT_SHADER);
        }
    }

    public static Font getTooltipFont(ItemStack stack, Font fallbackFont) {
        Font stackFont = IClientItemExtensions.of(stack).getFont(stack, IClientItemExtensions.FontContext.TOOLTIP);
        return stackFont == null ? fallbackFont : stackFont;
    }

    public static RenderTooltipEvent.Pre onRenderTooltipPre(ItemStack stack, GuiGraphics graphics, int x, int y, int screenWidth, int screenHeight, List<ClientTooltipComponent> components, Font fallbackFont, ClientTooltipPositioner positioner) {
        var preEvent = new RenderTooltipEvent.Pre(stack, graphics, x, y, screenWidth, screenHeight, getTooltipFont(stack, fallbackFont), components, positioner);
        NeoForge.EVENT_BUS.post(preEvent);
        return preEvent;
    }

    public static RenderTooltipEvent.Texture onRenderTooltipTexture(ItemStack stack, GuiGraphics graphics, int x, int y, Font font, List<ClientTooltipComponent> components, @Nullable ResourceLocation texture) {
        return NeoForge.EVENT_BUS.post(new RenderTooltipEvent.Texture(stack, graphics, x, y, font, components, texture));
    }

    public static List<ClientTooltipComponent> gatherTooltipComponents(ItemStack stack, List<? extends FormattedText> textElements, int mouseX, int screenWidth, int screenHeight, Font fallbackFont) {
        return gatherTooltipComponents(stack, textElements, Optional.empty(), mouseX, screenWidth, screenHeight, fallbackFont);
    }

    public static List<ClientTooltipComponent> gatherTooltipComponents(ItemStack stack, List<? extends FormattedText> textElements, Optional<TooltipComponent> itemComponent, int mouseX, int screenWidth, int screenHeight, Font fallbackFont) {
        List<Either<FormattedText, TooltipComponent>> elements = textElements.stream()
                .map((Function<FormattedText, Either<FormattedText, TooltipComponent>>) Either::left)
                .collect(Collectors.toCollection(ArrayList::new));
        itemComponent.ifPresent(c -> elements.add(1, Either.right(c)));
        return gatherTooltipComponentsFromElements(stack, elements, mouseX, screenWidth, screenHeight, fallbackFont);
    }

    public static List<ClientTooltipComponent> gatherTooltipComponentsFromElements(ItemStack stack, List<Either<FormattedText, TooltipComponent>> elements, int mouseX, int screenWidth, int screenHeight, Font fallbackFont) {
        Font font = getTooltipFont(stack, fallbackFont);

        var event = new RenderTooltipEvent.GatherComponents(stack, screenWidth, screenHeight, elements, -1);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return List.of();

        // text wrapping
        int tooltipTextWidth = event.getTooltipElements().stream()
                .mapToInt(either -> either.map(font::width, component -> 0))
                .max()
                .orElse(0);

        boolean needsWrap = false;

        int tooltipX = mouseX + 12;
        if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
            tooltipX = mouseX - 16 - tooltipTextWidth;
            if (tooltipX < 4) // if the tooltip doesn't fit on the screen
            {
                if (mouseX > screenWidth / 2)
                    tooltipTextWidth = mouseX - 12 - 8;
                else
                    tooltipTextWidth = screenWidth - 16 - mouseX;
                needsWrap = true;
            }
        }

        if (event.getMaxWidth() > 0 && tooltipTextWidth > event.getMaxWidth()) {
            tooltipTextWidth = event.getMaxWidth();
            needsWrap = true;
        }

        int tooltipTextWidthF = tooltipTextWidth;
        if (needsWrap) {
            return event.getTooltipElements().stream()
                    .flatMap(either -> either.map(
                            text -> splitLine(text, font, tooltipTextWidthF),
                            component -> Stream.of(ClientTooltipComponent.create(component))))
                    .toList();
        }
        return event.getTooltipElements().stream()
                .map(either -> either.map(
                        text -> ClientTooltipComponent.create(text instanceof Component ? ((Component) text).getVisualOrderText() : Language.getInstance().getVisualOrder(text)),
                        ClientTooltipComponent::create))
                .toList();
    }

    private static Stream<ClientTooltipComponent> splitLine(FormattedText text, Font font, int maxWidth) {
        if (text instanceof Component component && component.getString().isEmpty()) {
            return Stream.of(component.getVisualOrderText()).map(ClientTooltipComponent::create);
        }
        return font.split(text, maxWidth).stream().map(ClientTooltipComponent::create);
    }

    public static Comparator<ParticleRenderType> makeParticleRenderTypeComparator(List<ParticleRenderType> renderOrder) {
        Comparator<ParticleRenderType> vanillaComparator = Comparator.comparingInt(renderOrder::indexOf);
        return (typeOne, typeTwo) -> {
            boolean vanillaOne = renderOrder.contains(typeOne);
            boolean vanillaTwo = renderOrder.contains(typeTwo);

            if (vanillaOne && vanillaTwo) {
                return vanillaComparator.compare(typeOne, typeTwo);
            }
            if (!vanillaOne && !vanillaTwo) {
                return Integer.compare(System.identityHashCode(typeOne), System.identityHashCode(typeTwo));
            }
            return vanillaOne ? -1 : 1;
        };
    }

    public static ScreenEvent.RenderInventoryMobEffects onScreenPotionSize(Screen screen, int availableSpace, boolean compact, int horizontalOffset) {
        final ScreenEvent.RenderInventoryMobEffects event = new ScreenEvent.RenderInventoryMobEffects(screen, availableSpace, compact, horizontalOffset);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static boolean onToastAdd(Toast toast) {
        return NeoForge.EVENT_BUS.post(new ToastAddEvent(toast)).isCanceled();
    }

    public static boolean isBlockInSolidLayer(BlockState state) {
        var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        return model.getRenderTypes(state, RandomSource.create(), ModelData.EMPTY).contains(RenderType.solid());
    }

    public static boolean renderFireOverlay(Player player, PoseStack mat) {
        return renderBlockOverlay(player, mat, RenderBlockScreenEffectEvent.OverlayType.FIRE, Blocks.FIRE.defaultBlockState(), player.blockPosition());
    }

    public static boolean renderWaterOverlay(Player player, PoseStack mat) {
        return renderBlockOverlay(player, mat, RenderBlockScreenEffectEvent.OverlayType.WATER, Blocks.WATER.defaultBlockState(), player.blockPosition());
    }

    public static boolean renderBlockOverlay(Player player, PoseStack mat, RenderBlockScreenEffectEvent.OverlayType type, BlockState block, BlockPos pos) {
        return NeoForge.EVENT_BUS.post(new RenderBlockScreenEffectEvent(player, mat, type, block, pos)).isCanceled();
    }

    public static int getMaxMipmapLevel(int width, int height) {
        return Math.min(
                Mth.log2(Math.max(1, width)),
                Mth.log2(Math.max(1, height)));
    }

    private static final BiMap<ResourceLocation, SpriteSourceType> SPRITE_SOURCE_TYPES_MAP = HashBiMap.create();

    public static BiMap<ResourceLocation, SpriteSourceType> makeSpriteSourceTypesMap() {
        return SPRITE_SOURCE_TYPES_MAP;
    }

    @ApiStatus.Internal
    public static void registerSpriteSourceTypes() {
        ModLoader.postEvent(new RegisterSpriteSourceTypesEvent(SPRITE_SOURCE_TYPES_MAP));
    }

    @ApiStatus.Internal
    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> boolean isBlockEntityRendererVisible(BlockEntityRenderDispatcher dispatcher, BlockEntity blockEntity, Frustum frustum) {
        BlockEntityRenderer<T> renderer = (BlockEntityRenderer<T>) dispatcher.getRenderer(blockEntity);
        return renderer != null && frustum.isVisible(renderer.getRenderBoundingBox((T) blockEntity));
    }

    /**
     * Modify the position and UVs of the edge quads of generated item models to account for sprite expansion of the
     * front and back quad. Fixes <a href="https://bugs.mojang.com/browse/MC-73186">MC-73186</a> on generated item models.
     *
     * @param elements The generated elements, may include the front and back face
     * @param sprite   The texture from which the elements were generated
     * @return the original elements list
     */
    public static List<BlockElement> fixItemModelSeams(List<BlockElement> elements, TextureAtlasSprite sprite) {
        float expand = -sprite.uvShrinkRatio();
        for (BlockElement element : elements) {
            // Edge elements are guaranteed to have exactly one face, anything else is either invalid or the front/back
            if (element.faces.size() != 1) continue;

            var faceEntry = element.faces.entrySet().iterator().next();
            if (faceEntry.getKey().getAxis() == Direction.Axis.Z) continue;

            // Move edge quads to account for sprite expansion of the front and back quads
            element.from.x = Mth.clamp(Mth.lerp(expand, element.from.x, 8F), 0F, 16F);
            element.from.y = Mth.clamp(Mth.lerp(expand, element.from.y, 8F), 0F, 16F);
            element.to.x = Mth.clamp(Mth.lerp(expand, element.to.x, 8F), 0F, 16F);
            element.to.y = Mth.clamp(Mth.lerp(expand, element.to.y, 8F), 0F, 16F);

            float[] uv = faceEntry.getValue().uv().uvs;
            // Counteract sprite expansion on edge quads to ensure alignment with pixels on the front and back quads
            if (faceEntry.getKey().getAxis() == Direction.Axis.Y) {
                float centerU = (uv[0] + uv[0] + uv[2] + uv[2]) / 4.0F;
                uv[0] = Mth.clamp(Mth.lerp(expand, uv[0], centerU), 0F, 16F);
                uv[2] = Mth.clamp(Mth.lerp(expand, uv[2], centerU), 0F, 16F);
            } else {
                float centerV = (uv[1] + uv[1] + uv[3] + uv[3]) / 4.0F;
                uv[1] = Mth.clamp(Mth.lerp(expand, uv[1], centerV), 0F, 16F);
                uv[3] = Mth.clamp(Mth.lerp(expand, uv[3], centerV), 0F, 16F);
            }
        }
        return elements;
    }

    public static List<AddSectionGeometryEvent.AdditionalSectionRenderer> gatherAdditionalRenderers(
            BlockPos sectionOrigin, Level level) {
        final var event = new AddSectionGeometryEvent(sectionOrigin, level);
        NeoForge.EVENT_BUS.post(event);
        return event.getAdditionalRenderers();
    }

    public static void addAdditionalGeometry(
            List<AddSectionGeometryEvent.AdditionalSectionRenderer> additionalRenderers,
            Function<RenderType, VertexConsumer> getOrCreateBuilder,
            RenderChunkRegion region,
            PoseStack transformation) {
        if (additionalRenderers.isEmpty()) {
            return;
        }
        final var context = new AddSectionGeometryEvent.SectionRenderingContext(getOrCreateBuilder, region, transformation);
        for (final var renderer : additionalRenderers) {
            renderer.render(context);
        }
    }

    // Make sure the below method is only ever called once (by forge).
    private static boolean initializedClientHooks = false;

    // Runs during Minecraft construction, before initial resource loading.
    @ApiStatus.Internal
    public static void initClientHooks(Minecraft mc, ReloadableResourceManager resourceManager) {
        if (initializedClientHooks) {
            throw new IllegalStateException("Client hooks initialized more than once");
        }
        initializedClientHooks = true;

        ClientExtensionsManager.init();
        GameTestHooks.registerGametests();
        registerSpriteSourceTypes();
        MenuScreens.init();
        ModLoader.postEvent(new RegisterClientReloadListenersEvent(resourceManager));
        ModLoader.postEvent(new EntityRenderersEvent.RegisterLayerDefinitions());
        ModLoader.postEvent(new EntityRenderersEvent.RegisterRenderers());
        ClientTooltipComponentManager.init();
        EntitySpectatorShaderManager.init();
        ClientHooks.onRegisterKeyMappings(mc.options);
        RecipeBookManager.init();
        mc.gui.initModdedOverlays();
        DimensionSpecialEffectsManager.init();
        NamedRenderTypeManager.init();
        ColorResolverManager.init();
        ItemDecoratorHandler.init();
        PresetEditorManager.init();
        MapDecorationRendererManager.init();
        DimensionTransitionScreenManager.init();
        AnimationTypeManager.init();
        CoreShaderManager.init();
    }

    /**
     * Fires {@link RenderFrameEvent.Pre}. Called just before {@link GameRenderer#render(DeltaTracker, boolean)} in {@link Minecraft#runTick(boolean)}.
     * <p>
     * Fired before the profiler section for "gameRenderer" is started.
     *
     * @param partialTick The current partial tick
     */
    public static void fireRenderFramePre(DeltaTracker partialTick) {
        NeoForge.EVENT_BUS.post(new RenderFrameEvent.Pre(partialTick));
    }

    /**
     * Fires {@link RenderFrameEvent.Post}. Called just after {@link GameRenderer#render(DeltaTracker, boolean)} in {@link Minecraft#runTick(boolean)}.
     * <p>
     * Fired after the profiler section for "gameRenderer" is ended.
     *
     * @param partialTick The current partial tick
     */
    public static void fireRenderFramePost(DeltaTracker partialTick) {
        NeoForge.EVENT_BUS.post(new RenderFrameEvent.Post(partialTick));
    }

    /**
     * Fires {@link ClientTickEvent.Pre}. Called from the head of {@link Minecraft#tick()}.
     */
    public static void fireClientTickPre() {
        NeoForge.EVENT_BUS.post(new ClientTickEvent.Pre());
    }

    /**
     * Fires {@link ClientTickEvent.Post}. Called from the tail of {@link Minecraft#tick()}.
     */
    public static void fireClientTickPost() {
        NeoForge.EVENT_BUS.post(new ClientTickEvent.Post());
    }

    @Nullable
    @SuppressWarnings("resource")
    public static <T> RegistryLookup<T> resolveLookup(ResourceKey<? extends Registry<T>> key) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            return level.registryAccess().lookup(key).orElse(null);
        }
        return null;
    }

    /**
     * Fires the {@link GatherEffectScreenTooltipsEvent} and returns the resulting tooltip lines.
     * <p>
     * Called from {@link EffectsInInventory#renderEffects} just before {@link GuiGraphics#renderTooltip(Font, List, Optional, int, int)} is called.
     *
     * @param screen     The screen rendering the tooltip.
     * @param effectInst The effect instance whose tooltip is being rendered.
     * @param tooltip    An immutable list containing the existing tooltip lines, which consist of the name and the duration.
     * @return The new tooltip lines, modified by the event.
     */
    public static List<Component> getEffectTooltip(AbstractContainerScreen<?> screen, MobEffectInstance effectInst, List<Component> tooltip) {
        var event = new GatherEffectScreenTooltipsEvent(screen, effectInst, tooltip);
        NeoForge.EVENT_BUS.post(event);
        return event.getTooltip();
    }

    private static final ExtensionInfo RECIPE_BOOK_TYPE_EXTENSION_INFO = RecipeBookType.getExtensionInfo();
    private static final RecipeBookType[] RECIPE_BOOK_TYPES = RecipeBookType.values();
    private static RecipeBookType @Nullable [] cachedFilteredTypes = null;

    public static RecipeBookType[] getFilteredRecipeBookTypeValues() {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        if (listener != null && !listener.getConnection().isMemoryConnection() && listener.getConnectionType().isOther()) {
            if (cachedFilteredTypes == null) {
                if (RECIPE_BOOK_TYPE_EXTENSION_INFO.extended()) {
                    cachedFilteredTypes = Arrays.copyOfRange(RECIPE_BOOK_TYPES, 0, RECIPE_BOOK_TYPE_EXTENSION_INFO.vanillaCount());
                } else {
                    cachedFilteredTypes = RECIPE_BOOK_TYPES;
                }
            }
            return cachedFilteredTypes;
        }
        return RECIPE_BOOK_TYPES;
    }

    private static final RandomSource OUTLINE_PASS_RANDOM = RandomSource.create();

    public static boolean isInTranslucentBlockOutlinePass(Level level, BlockPos pos, BlockState state) {
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        OUTLINE_PASS_RANDOM.setSeed(42);
        ChunkRenderTypeSet renderTypes = model.getRenderTypes(state, OUTLINE_PASS_RANDOM, level.getModelData(pos));
        return renderTypes.contains(RenderType.TRANSLUCENT) || renderTypes.contains(RenderType.TRIPWIRE);
    }
}
