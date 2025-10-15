/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicInfo;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.locale.Language;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.earlydisplay.DisplayWindow;
import net.neoforged.fml.loading.EarlyLoadingScreenController;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuDevice;
import net.neoforged.neoforge.client.config.NeoForgeClientConfig;
import net.neoforged.neoforge.client.entity.animation.json.AnimationTypeManager;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.CalculatePlayerTurnEvent;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPauseChangeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerChangeGameTypeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientResourceLoadFinishedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.ConfigureMainRenderTargetEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.FrameGraphSetupEvent;
import net.neoforged.neoforge.client.event.GatherEffectScreenTooltipsEvent;
import net.neoforged.neoforge.client.event.InitializeClientRegistriesEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.PlayerHeartTypeEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterTextureAtlasesEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
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
import net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration;
import net.neoforged.neoforge.client.gui.map.MapDecorationRendererManager;
import net.neoforged.neoforge.client.internal.ForgeSnapshotsModClient;
import net.neoforged.neoforge.client.loading.NeoForgeLoadingOverlay;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.block.BlockStateModelHooks;
import net.neoforged.neoforge.client.pipeline.PipelineModifiers;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.internal.BrandingControl;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.apache.commons.lang3.mutable.MutableObject;
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
        minecraft.getNarrator().saySystemNow(screen.getNarrationMessage());
    }

    public static void popGuiLayer(Minecraft minecraft) {
        if (guiLayers.isEmpty()) {
            minecraft.setScreen(null);
            return;
        }

        popGuiLayerInternal(minecraft);
        if (minecraft.screen != null)
            minecraft.getNarrator().saySystemNow(minecraft.screen.getNarrationMessage());
    }

    /**
     * Called by {@link Gui.HeartType#forPlayer} to allow for modification of the displayed heart type in the
     * health bar.
     *
     * @param player    The local {@link Player}
     * @param heartType The {@link Gui.HeartType} which would be displayed by vanilla
     * @return The heart type which should be displayed
     */
    public static Gui.HeartType firePlayerHeartTypeEvent(Player player, Gui.HeartType heartType) {
        return NeoForge.EVENT_BUS.post(new PlayerHeartTypeEvent(player, heartType)).getType();
    }

    public static ResourceLocation getArmorTexture(ItemStack armor, EquipmentClientInfo.LayerType type, EquipmentClientInfo.Layer layer, ResourceLocation _default) {
        ResourceLocation result = IClientItemExtensions.of(armor).getArmorTexture(armor, type, layer, _default);
        return result != null ? result : _default;
    }

    public static boolean onClientPauseChangePre(boolean pause) {
        var event = NeoForge.EVENT_BUS.post(new ClientPauseChangeEvent.Pre(pause));
        return event.isCanceled();
    }

    public static void onClientPauseChangePost(boolean pause) {
        NeoForge.EVENT_BUS.post(new ClientPauseChangeEvent.Post(pause));
    }

    public static boolean renderSpecificFirstPersonHand(InteractionHand hand, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, float partialTick, float interpPitch, float swingProgress, float equipProgress, ItemStack stack) {
        return NeoForge.EVENT_BUS.post(new RenderHandEvent(hand, poseStack, submitNodeCollector, packedLight, partialTick, interpPitch, swingProgress, equipProgress, stack)).isCanceled();
    }

    public static boolean renderSpecificFirstPersonArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, AbstractClientPlayer player, HumanoidArm arm) {
        return NeoForge.EVENT_BUS.post(new RenderArmEvent(poseStack, submitNodeCollector, packedLight, player, arm)).isCanceled();
    }

    public static void onTextureAtlasStitched(TextureAtlas atlas) {
        ModLoader.postEvent(new TextureAtlasStitchedEvent(atlas));
    }

    public static void onBlockColorsInit(BlockColors blockColors) {
        ModLoader.postEvent(new RegisterColorHandlersEvent.Block(blockColors));
    }

    /** Copies humanoid model properties from the original model to another, used for armor models */
    public static void copyModelProperties(HumanoidModel<?> original, HumanoidModel<?> replacement) {
        copyModelPartProperties(original.head, replacement.head);
        copyModelPartProperties(original.hat, replacement.hat);
        copyModelPartProperties(original.body, replacement.body);
        copyModelPartProperties(original.rightArm, replacement.rightArm);
        copyModelPartProperties(original.leftArm, replacement.leftArm);
        copyModelPartProperties(original.rightLeg, replacement.rightLeg);
        copyModelPartProperties(original.leftLeg, replacement.leftLeg);
    }

    private static void copyModelPartProperties(ModelPart original, ModelPart replacement) {
        replacement.visible = original.visible;
        replacement.x = original.x;
        replacement.y = original.y;
        replacement.z = original.z;
        replacement.xRot = original.xRot;
        replacement.yRot = original.yRot;
        replacement.zRot = original.zRot;
        replacement.xScale = original.xScale;
        replacement.yScale = original.yScale;
        replacement.zScale = original.zScale;
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

    public static float getDetachedCameraDistance(Camera camera, boolean flipped, float entityScale, float entityDistance, float vehicleEntityScale, float vehicleDistance) {
        var event = new CalculateDetachedCameraDistanceEvent(camera, flipped, entityScale, entityDistance, vehicleEntityScale, vehicleDistance);
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

        BrandingControl.setForgeStatusLine(switch (NeoForgeVersion.getStatus()) {
            // case FAILED -> " Version check failed";
            // case UP_TO_DATE -> "Forge up to date";
            // case AHEAD -> "Using non-recommended Forge build, issues may arise.";
            case OUTDATED, BETA_OUTDATED -> I18n.get("neoforge.update.newversion", NeoForgeVersion.getTarget());
            default -> null;
        });
    }

    @Nullable
    public static SoundInstance playSound(SoundEngine manager, SoundInstance sound) {
        PlaySoundEvent e = new PlaySoundEvent(manager, sound);
        NeoForge.EVENT_BUS.post(e);
        return e.getSound();
    }

    @Nullable
    public static MusicInfo selectMusic(MusicInfo situational, @Nullable SoundInstance playing) {
        SelectMusicEvent e = new SelectMusicEvent(situational, playing);
        NeoForge.EVENT_BUS.post(e);
        return e.getMusic();
    }

    public static void drawScreen(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiLayers.forEach(layer -> {
            // Prevent the background layers from thinking the mouse is over their controls and showing them as highlighted.
            drawScreenInternal(layer, guiGraphics, Integer.MAX_VALUE, Integer.MAX_VALUE, partialTick);
            guiGraphics.nextStratum();
        });
        drawScreenInternal(screen, guiGraphics, mouseX, mouseY, partialTick);
    }

    private static void drawScreenInternal(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!NeoForge.EVENT_BUS.post(new ScreenEvent.Render.Pre(screen, guiGraphics, mouseX, mouseY, partialTick)).isCanceled())
            screen.renderWithTooltipAndSubtitles(guiGraphics, mouseX, mouseY, partialTick);
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

    public static void onSetupFog(@Nullable FogEnvironment environment, FogType type, Camera camera, float partialTick, float renderDistance, FogData fogData) {
        // Modify fog rendering depending on the fluid
        FluidState state = camera.getEntity().level().getFluidState(camera.getBlockPosition());
        if (camera.getPosition().y < (double) ((float) camera.getBlockPosition().getY() + state.getHeight(camera.getEntity().level(), camera.getBlockPosition())))
            IClientFluidTypeExtensions.of(state).modifyFogRender(camera, environment, renderDistance, partialTick, fogData);

        NeoForge.EVENT_BUS.post(new ViewportEvent.RenderFog(environment, type, camera, partialTick, fogData));
    }

    public static void onModifyBakingResult(ModelBakery.BakingResult bakingResult, SpriteLoader.Preparations spriteLoaderPreparations, ModelBakery modelBakery) {
        Function<ResourceLocation, TextureAtlasSprite> textureGetter = location -> {
            TextureAtlasSprite sprite = spriteLoaderPreparations.getSprite(location);
            if (sprite != null) {
                return sprite;
            }
            LOGGER.warn("Failed to retrieve texture '{}' from the block atlas", location, new Throwable());
            return spriteLoaderPreparations.missing();
        };
        ModLoader.postEvent(new ModelEvent.ModifyBakingResult(bakingResult, textureGetter, modelBakery));
    }

    public static void onModelBake(ModelManager modelManager, ModelBakery.BakingResult bakingResult, ModelBakery modelBakery) {
        ModLoader.postEvent(new ModelEvent.BakingCompleted(modelManager, bakingResult, modelBakery));
    }

    @SuppressWarnings("deprecation")
    public static Material getBlockMaterial(ResourceLocation loc) {
        return new Material(TextureAtlas.LOCATION_BLOCKS, loc);
    }

    /**
     * Computes the packed normal of a quad based on the stored vertex positions.
     */
    public static int computeQuadNormal(int[] vertices) {
        float x0 = Float.intBitsToFloat(vertices[IQuadTransformer.POSITION]);
        float y0 = Float.intBitsToFloat(vertices[IQuadTransformer.POSITION + 1]);
        float z0 = Float.intBitsToFloat(vertices[IQuadTransformer.POSITION + 2]);
        float x1 = Float.intBitsToFloat(vertices[IQuadTransformer.STRIDE + IQuadTransformer.POSITION]);
        float y1 = Float.intBitsToFloat(vertices[IQuadTransformer.STRIDE + IQuadTransformer.POSITION + 1]);
        float z1 = Float.intBitsToFloat(vertices[IQuadTransformer.STRIDE + IQuadTransformer.POSITION + 2]);
        float x2 = Float.intBitsToFloat(vertices[2 * IQuadTransformer.STRIDE + IQuadTransformer.POSITION]);
        float y2 = Float.intBitsToFloat(vertices[2 * IQuadTransformer.STRIDE + IQuadTransformer.POSITION + 1]);
        float z2 = Float.intBitsToFloat(vertices[2 * IQuadTransformer.STRIDE + IQuadTransformer.POSITION + 2]);
        float x3 = Float.intBitsToFloat(vertices[3 * IQuadTransformer.STRIDE + IQuadTransformer.POSITION]);
        float y3 = Float.intBitsToFloat(vertices[3 * IQuadTransformer.STRIDE + IQuadTransformer.POSITION + 1]);
        float z3 = Float.intBitsToFloat(vertices[3 * IQuadTransformer.STRIDE + IQuadTransformer.POSITION + 2]);

        float dx0 = x3 - x1;
        float dy0 = y3 - y1;
        float dz0 = z3 - z1;
        float dx1 = x2 - x0;
        float dy1 = y2 - y0;
        float dz1 = z2 - z0;

        float nx = dy1 * dz0 - dz1 * dy0;
        float ny = dz1 * dx0 - dx1 * dz0;
        float nz = dx1 * dy0 - dy1 * dx0;

        float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        if (length > 0) {
            nx /= length;
            ny /= length;
            nz /= length;
        }

        int packedx = ((byte) Math.round(nx * 127)) & 0xFF;
        int packedy = ((byte) Math.round(ny * 127)) & 0xFF;
        int packedz = ((byte) Math.round(nz * 127)) & 0xFF;

        return packedx | (packedy << 8) | (packedz << 16);
    }

    /**
     * Modifies the passed {@code faceData} to fill in the vertex normals.
     * The normals are computed from the vertex positions, see {@link #computeQuadNormal}.
     */
    public static void fillNormal(int[] faceData) {
        int normal = computeQuadNormal(faceData);

        for (int i = 0; i < 4; i++) {
            faceData[i * 8 + 7] = normal;
        }
    }

    public static boolean loadEntityShader(@Nullable Entity entity, GameRenderer gameRenderer) {
        if (entity != null) {
            ResourceLocation shader = EntitySpectatorShaderManager.get(entity.getType());
            if (shader != null) {
                gameRenderer.setPostEffect(shader);
                return true;
            }
        }
        return false;
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

    public static boolean onScreenMouseClickedPre(Screen guiScreen, MouseButtonEvent mouseEvent, boolean doubleClick) {
        var event = new ScreenEvent.MouseButtonPressed.Pre(guiScreen, mouseEvent, doubleClick);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static boolean onScreenMouseClickedPost(Screen guiScreen, MouseButtonEvent mouseEvent, boolean doubleClick, boolean handled) {
        var event = new ScreenEvent.MouseButtonPressed.Post(guiScreen, mouseEvent, doubleClick, handled);
        NeoForge.EVENT_BUS.post(event);
        return event.getClickResult();
    }

    public static boolean onScreenMouseReleasedPre(Screen guiScreen, MouseButtonEvent mouseEvent) {
        var event = new ScreenEvent.MouseButtonReleased.Pre(guiScreen, mouseEvent);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static boolean onScreenMouseReleasedPost(Screen guiScreen, MouseButtonEvent mouseEvent, boolean handled) {
        var event = new ScreenEvent.MouseButtonReleased.Post(guiScreen, mouseEvent, handled);
        NeoForge.EVENT_BUS.post(event);
        return event.getReleaseResult();
    }

    public static boolean onScreenMouseDragPre(Screen guiScreen, MouseButtonEvent mouseEvent, double dragX, double dragY) {
        var event = new ScreenEvent.MouseDragged.Pre(guiScreen, mouseEvent, dragX, dragY);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static void onScreenMouseDragPost(Screen guiScreen, MouseButtonEvent mouseEvent, double dragX, double dragY) {
        Event event = new ScreenEvent.MouseDragged.Post(guiScreen, mouseEvent, dragX, dragY);
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

    public static boolean onScreenKeyPressedPre(Screen guiScreen, KeyEvent keyEvent) {
        var event = new ScreenEvent.KeyPressed.Pre(guiScreen, keyEvent);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static boolean onScreenKeyPressedPost(Screen guiScreen, KeyEvent keyEvent) {
        var event = new ScreenEvent.KeyPressed.Post(guiScreen, keyEvent);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static boolean onScreenKeyReleasedPre(Screen guiScreen, KeyEvent keyEvent) {
        var event = new ScreenEvent.KeyReleased.Pre(guiScreen, keyEvent);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static boolean onScreenKeyReleasedPost(Screen guiScreen, KeyEvent keyEvent) {
        var event = new ScreenEvent.KeyReleased.Post(guiScreen, keyEvent);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static boolean onScreenCharTypedPre(Screen guiScreen, CharacterEvent charEvent) {
        var event = new ScreenEvent.CharacterTyped.Pre(guiScreen, charEvent);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static void onScreenCharTypedPost(Screen guiScreen, CharacterEvent charEvent) {
        Event event = new ScreenEvent.CharacterTyped.Post(guiScreen, charEvent);
        NeoForge.EVENT_BUS.post(event);
    }

    public static boolean onMouseButtonPre(MouseButtonInfo mouseButtonInfo, int action) {
        return NeoForge.EVENT_BUS.post(new InputEvent.MouseButton.Pre(mouseButtonInfo, action)).isCanceled();
    }

    public static void onMouseButtonPost(MouseButtonInfo mouseButtonInfo, int action) {
        NeoForge.EVENT_BUS.post(new InputEvent.MouseButton.Post(mouseButtonInfo, action));
    }

    public static boolean onMouseScroll(MouseHandler mouseHelper, double scrollDeltaX, double scrollDeltaY) {
        var event = new InputEvent.MouseScrollingEvent(scrollDeltaX, scrollDeltaY, mouseHelper.isLeftPressed(), mouseHelper.isMiddlePressed(), mouseHelper.isRightPressed(), mouseHelper.xpos(), mouseHelper.ypos());
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static void onKeyInput(KeyEvent keyEvent, int action) {
        NeoForge.EVENT_BUS.post(new InputEvent.Key(keyEvent, action));
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

    private static final Map<SkullBlock.Type, Function<EntityModelSet, SkullModelBase>> skullModelsByType = new HashMap<>();

    @Nullable
    public static SkullModelBase getModdedSkullModel(EntityModelSet modelSet, SkullBlock.Type type) {
        return skullModelsByType.getOrDefault(type, set -> null).apply(modelSet);
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

    public static void onRegisterParticleProviders(ParticleResources particleResources) {
        ModLoader.postEvent(new RegisterParticleProvidersEvent(particleResources));
    }

    @ApiStatus.Internal
    public static void onRegisterKeyMappings(Options options, List<KeyMapping.Category> categories) {
        RegisterKeyMappingsEvent event = new RegisterKeyMappingsEvent(options);
        ModLoader.postEvent(event);
        event.sortAndStoreCategories(categories);
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

    @ApiStatus.Internal
    public static void handleUpdateRecipes(ClientPacketListener packetListener, Consumer<FuelValues> fuelValuesSetter) {
        // Neo: abuse recipe sync to overwrite fuel values with datamap values after their sync (tag update doesn't fire on initial sync and the constructor is too early)
        if (packetListener.getConnectionType().isNeoForge()) {
            fuelValuesSetter.accept(net.neoforged.neoforge.common.DataMapHooks.populateFuelValues(packetListener.registryAccess(), packetListener.enabledFeatures()));
        } else {
            // Notify client mods that they're connected to a Vanilla server, which will never give them recipe data
            var event = new net.neoforged.neoforge.client.event.RecipesReceivedEvent(java.util.Set.of(), net.minecraft.world.item.crafting.RecipeMap.create(java.util.List.of()));
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event);
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

    public static ScreenEvent.RenderInventoryMobEffects onScreenPotionSize(Screen screen, int availableSpace, boolean compact, int horizontalOffset) {
        final ScreenEvent.RenderInventoryMobEffects event = new ScreenEvent.RenderInventoryMobEffects(screen, availableSpace, compact, horizontalOffset);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static boolean onToastAdd(Toast toast) {
        return NeoForge.EVENT_BUS.post(new ToastAddEvent(toast)).isCanceled();
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
        elements.replaceAll(element -> {
            // Edge elements are guaranteed to have exactly one face, anything else is either invalid or the front/back
            if (element.faces().size() != 1) return element;

            var faceEntry = element.faces().entrySet().iterator().next();
            if (faceEntry.getKey().getAxis() == Direction.Axis.Z) return element;

            // Move edge quads to account for sprite expansion of the front and back quads
            Vector3f from = new Vector3f(
                    Mth.clamp(Mth.lerp(expand, element.from().x(), 8F), 0F, 16F),
                    Mth.clamp(Mth.lerp(expand, element.from().y(), 8F), 0F, 16F),
                    element.from().z());
            Vector3f to = new Vector3f(
                    Mth.clamp(Mth.lerp(expand, element.to().x(), 8F), 0F, 16F),
                    Mth.clamp(Mth.lerp(expand, element.to().y(), 8F), 0F, 16F),
                    element.to().z());

            BlockElementFace face = faceEntry.getValue();
            BlockElementFace.UVs uvs = face.uvs();
            if (uvs == null) {
                uvs = FaceBakery.defaultFaceUV(element.from(), element.to(), faceEntry.getKey());
            }
            float minU = uvs.minU();
            float minV = uvs.minV();
            float maxU = uvs.maxU();
            float maxV = uvs.maxV();
            // Counteract sprite expansion on edge quads to ensure alignment with pixels on the front and back quads
            if (faceEntry.getKey().getAxis() == Direction.Axis.Y) {
                float centerU = (minU + minU + maxU + maxU) / 4.0F;
                minU = Mth.clamp(Mth.lerp(expand, minU, centerU), 0F, 16F);
                maxU = Mth.clamp(Mth.lerp(expand, maxU, centerU), 0F, 16F);
            } else {
                float centerV = (minV + minV + maxV + maxV) / 4.0F;
                minV = Mth.clamp(Mth.lerp(expand, minV, centerV), 0F, 16F);
                maxV = Mth.clamp(Mth.lerp(expand, maxV, centerV), 0F, 16F);
            }
            uvs = new BlockElementFace.UVs(minU, minV, maxU, maxV);
            face = new BlockElementFace(face.cullForDirection(), face.tintIndex(), face.texture(), uvs, face.rotation(), face.faceData(), new MutableObject<>());

            return new BlockElement(from, to, Map.of(faceEntry.getKey(), face), element.rotation(), element.shade(), element.lightEmission(), element.faceData());
        });
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
            Function<ChunkSectionLayer, VertexConsumer> getOrCreateBuilder,
            RenderSectionRegion region,
            PoseStack transformation) {
        if (additionalRenderers.isEmpty()) {
            return;
        }
        final var context = new AddSectionGeometryEvent.SectionRenderingContext(getOrCreateBuilder, region, transformation);
        for (final var renderer : additionalRenderers) {
            renderer.render(context);
        }
    }

    // Make sure the below methods are only ever called once (by forge).
    private static boolean initializedClientHooks = false;
    private static boolean initializedClientRegistries = false;

    // Runs during Minecraft construction, before initial resource loading.
    @ApiStatus.Internal
    public static void initClientHooks(Minecraft mc, ReloadableResourceManager resourceManager) {
        if (initializedClientHooks) {
            throw new IllegalStateException("Client hooks initialized more than once");
        }
        initializedClientHooks = true;

        ClientExtensionsManager.init();
        MenuScreens.init();
        initClientRegistries();

        var rlEvent = new AddClientReloadListenersEvent(resourceManager);
        ModLoader.postEvent(rlEvent);
        resourceManager.updateListenersFrom(rlEvent);

        ModLoader.postEvent(new EntityRenderersEvent.RegisterLayerDefinitions());
        ModLoader.postEvent(new EntityRenderersEvent.CreateSkullModels(skullModelsByType, SkullBlockRenderer.SKIN_BY_TYPE));
        ModLoader.postEvent(new EntityRenderersEvent.RegisterRenderers());
        ModLoader.postEvent(new RegisterRenderStateModifiersEvent());
        ClientTooltipComponentManager.init();
        EntitySpectatorShaderManager.init();
        RecipeBookManager.init();
        mc.gui.initModdedOverlays();
        DimensionSpecialEffectsManager.init();
        NamedRenderTypeManager.init();
        ColorResolverManager.init();
        ItemDecoratorHandler.init();
        PresetEditorManager.init();
        MapDecorationRendererManager.init();
        DimensionTransitionScreenManager.init();
        RenderPipelines.registerCustomPipelines();
        PipelineModifiers.init();
    }

    // Runs during Minecraft construction, before initial resource loading and during datagen startup
    public static void initClientRegistries() {
        if (initializedClientRegistries) {
            throw new IllegalStateException("Client registries initialized more than once");
        }
        initializedClientRegistries = true;

        AnimationTypeManager.init();
        BlockStateModelHooks.init();

        ModLoader.postEvent(new InitializeClientRegistriesEvent());
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
        RenderSystem.ensurePipelineModifiersEmpty();
    }

    /**
     * Fires {@link ClientResourceLoadFinishedEvent}.
     */
    public static void fireResourceLoadFinishedEvent(boolean initial) {
        NeoForge.EVENT_BUS.post(new ClientResourceLoadFinishedEvent(initial));
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

    private static final RandomSource OUTLINE_PASS_RANDOM = RandomSource.create();
    private static final List<BlockModelPart> OUTLINE_PART_SCRATCH_LIST = new ObjectArrayList<>();

    public static boolean isInTranslucentBlockOutlinePass(Level level, BlockPos pos, BlockState state) {
        OUTLINE_PASS_RANDOM.setSeed(42);
        OUTLINE_PART_SCRATCH_LIST.clear();

        BlockStateModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        model.collectParts(level, pos, state, OUTLINE_PASS_RANDOM, OUTLINE_PART_SCRATCH_LIST);
        for (BlockModelPart part : OUTLINE_PART_SCRATCH_LIST) {
            ChunkSectionLayer renderType = part.getRenderType(state);
            if (renderType == ChunkSectionLayer.TRANSLUCENT || renderType == ChunkSectionLayer.TRIPWIRE) {
                return true;
            }
        }
        return false;
    }

    public static void reloadRenderer() {
        Minecraft.getInstance().levelRenderer.allChanged();
    }

    public static List<AtlasManager.AtlasConfig> gatherTextureAtlases(List<AtlasManager.AtlasConfig> vanillaAtlases) {
        SequencedMap<ResourceLocation, AtlasManager.AtlasConfig> atlasMap = new LinkedHashMap<>(vanillaAtlases.size());
        vanillaAtlases.forEach(atlas -> atlasMap.put(atlas.definitionLocation(), atlas));
        ModLoader.postEvent(new RegisterTextureAtlasesEvent(atlasMap));
        return List.copyOf(atlasMap.values());
    }

    @ApiStatus.Internal
    public static FrameGraphSetupEvent fireFrameGraphSetup(FrameGraphBuilder builder, LevelTargetBundle targets, RenderTargetDescriptor renderTargetDescriptor, Frustum frustum, Camera camera, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, DeltaTracker deltaTracker, ProfilerFiller profiler) {
        return NeoForge.EVENT_BUS.post(new FrameGraphSetupEvent(builder, targets, renderTargetDescriptor, frustum, camera, modelViewMatrix, projectionMatrix, deltaTracker, profiler));
    }

    @ApiStatus.Internal
    public static MainTarget instantiateMainTarget(int width, int height) {
        var e = ModLoader.postEventWithReturn(new ConfigureMainRenderTargetEvent());
        return new MainTarget(width, height, e.isStencilEnabled());
    }

    @ApiStatus.Internal
    public static TextureFormat getStencilFormat() {
        var reducedPrecision = NeoForgeClientConfig.INSTANCE.reducedDepthStencilFormat.getAsBoolean();
        return reducedPrecision ? TextureFormat.DEPTH24_STENCIL8 : TextureFormat.DEPTH32_STENCIL8;
    }

    @ApiStatus.Internal
    public static Overlay createLoadingOverlay(Minecraft minecraft, ReloadInstance reloadInstance, Consumer<Optional<Throwable>> errorHandler, boolean fadeIn) {
        // If our own early loading screen is in use, we use a loading overlay that draws on top of that cooperatively
        if (EarlyLoadingScreenController.current() instanceof DisplayWindow displayWindow) {
            return new NeoForgeLoadingOverlay(minecraft, reloadInstance, errorHandler, displayWindow);
        } else {
            return new LoadingOverlay(minecraft, reloadInstance, errorHandler, fadeIn);
        }
    }

    private static final HashSet<MetadataSectionType<?>> DEFAULT_METADATA_SECTION_TYPES = new HashSet<>();

    @ApiStatus.Internal
    public static Set<MetadataSectionType<?>> getSpriteDefaultMetadataSectionTypes(Set<MetadataSectionType<?>> vanillaTypes) {
        DEFAULT_METADATA_SECTION_TYPES.addAll(vanillaTypes);
        return Collections.unmodifiableSet(DEFAULT_METADATA_SECTION_TYPES);
    }

    public static List<PictureInPictureRendererRegistration<?>> gatherPictureInPictureRenderers(
            List<PictureInPictureRendererRegistration<?>> vanillaRenderers) {
        vanillaRenderers = new ArrayList<>(vanillaRenderers);
        ModLoader.postEvent(new RegisterPictureInPictureRenderersEvent(vanillaRenderers));
        return List.copyOf(vanillaRenderers);
    }

    public static GpuDevice createGpuDevice(long window, int debugLevel, boolean syncDebug, BiFunction<ResourceLocation, ShaderType, String> defaultShaderSource, boolean enableDebugLabels) {
        final var glDevice = new GlDevice(window, debugLevel, syncDebug, defaultShaderSource, enableDebugLabels);
        boolean enableValidation;
        try {
            enableValidation = NeoForgeClientConfig.INSTANCE.enableB3DValidationLayer.getAsBoolean();
        } catch (NullPointerException | IllegalStateException e) {
            // We're in an early error state, config is not available. Assume environment default.
            enableValidation = NeoForgeClientConfig.INSTANCE.enableB3DValidationLayer.getDefault();
        }
        if (enableValidation) {
            return new ValidationGpuDevice(glDevice, true);
        }
        return glDevice;
    }
}
