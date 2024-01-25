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
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import net.minecraft.FileUtil;
import net.minecraft.client.Camera;
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
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.locale.Language;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.CalculatePlayerTurnEvent;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPauseUpdatedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerChangeGameTypeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ScreenshotEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.client.event.ToastAddEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.gui.ClientTooltipComponentManager;
import net.neoforged.neoforge.client.gui.overlay.GuiOverlayManager;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Class for various client-side-only hooks.
 */
public class ClientHooks {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker CLIENTHOOKS = MarkerManager.getMarker("CLIENTHOOKS");

    //private static final ResourceLocation ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

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
        // 11000 units for the overlay background,
        // and 10000 units for each layered Screen,

        return 11000.0F + 10000.0F * (1 + guiLayers.size());
    }

    public static String getArmorTexture(Entity entity, ItemStack armor, String _default, EquipmentSlot slot, String type) {
        String result = armor.getItem().getArmorTexture(armor, entity, slot, type);
        return result != null ? result : _default;
    }

    public static void onClientPauseUpdate(boolean paused) {
        NeoForge.EVENT_BUS.post(new ClientPauseUpdatedEvent(paused));
    }

    public static boolean onDrawHighlight(LevelRenderer context, Camera camera, HitResult target, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
        switch (target.getType()) {
            case BLOCK:
                if (!(target instanceof BlockHitResult blockTarget)) return false;
                return NeoForge.EVENT_BUS.post(new RenderHighlightEvent.Block(context, camera, blockTarget, partialTick, poseStack, bufferSource)).isCanceled();
            case ENTITY:
                if (!(target instanceof EntityHitResult entityTarget)) return false;
                NeoForge.EVENT_BUS.post(new RenderHighlightEvent.Entity(context, camera, entityTarget, partialTick, poseStack, bufferSource));
                return false;
            default:
                return false; // NO-OP - This doesn't even get called for anything other than blocks and entities
        }
    }

    public static void dispatchRenderStage(RenderLevelStageEvent.Stage stage, LevelRenderer levelRenderer, PoseStack poseStack, Matrix4f projectionMatrix, int renderTick, Camera camera, Frustum frustum) {
        var mc = Minecraft.getInstance();
        var profiler = mc.getProfiler();
        profiler.push(stage.toString());
        NeoForge.EVENT_BUS.post(new RenderLevelStageEvent(stage, levelRenderer, poseStack, projectionMatrix, renderTick, mc.getPartialTick(), camera, frustum));
        profiler.pop();
    }

    public static void dispatchRenderStage(RenderType renderType, LevelRenderer levelRenderer, PoseStack poseStack, Matrix4f projectionMatrix, int renderTick, Camera camera, Frustum frustum) {
        RenderLevelStageEvent.Stage stage = RenderLevelStageEvent.Stage.fromRenderType(renderType);
        if (stage != null)
            dispatchRenderStage(stage, levelRenderer, poseStack, projectionMatrix, renderTick, camera, frustum);
    }

    public static boolean renderSpecificFirstPersonHand(InteractionHand hand, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick, float interpPitch, float swingProgress, float equipProgress, ItemStack stack) {
        return NeoForge.EVENT_BUS.post(new RenderHandEvent(hand, poseStack, bufferSource, packedLight, partialTick, interpPitch, swingProgress, equipProgress, stack)).isCanceled();
    }

    public static boolean renderSpecificFirstPersonArm(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, AbstractClientPlayer player, HumanoidArm arm) {
        return NeoForge.EVENT_BUS.post(new RenderArmEvent(poseStack, multiBufferSource, packedLight, player, arm)).isCanceled();
    }

    public static void onTextureAtlasStitched(TextureAtlas atlas) {
        ModLoader.get().postEvent(new TextureAtlasStitchedEvent(atlas));
    }

    public static void onBlockColorsInit(BlockColors blockColors) {
        ModLoader.get().postEvent(new RegisterColorHandlersEvent.Block(blockColors));
    }

    public static void onItemColorsInit(ItemColors itemColors, BlockColors blockColors) {
        ModLoader.get().postEvent(new RegisterColorHandlersEvent.Item(itemColors, blockColors));
    }

    public static Model getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot slot, HumanoidModel<?> _default) {
        return IClientItemExtensions.of(itemStack).getGenericArmorModel(entityLiving, itemStack, slot, _default);
    }

    /** Copies humanoid model properties from the original model to another, used for armor models */
    @SuppressWarnings("unchecked")
    public static <T extends LivingEntity> void copyModelProperties(HumanoidModel<T> original, HumanoidModel<?> replacement) {
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

    public static float getFieldOfViewModifier(Player entity, float fovModifier) {
        ComputeFovModifierEvent fovModifierEvent = new ComputeFovModifierEvent(entity, fovModifier);
        NeoForge.EVENT_BUS.post(fovModifierEvent);
        return fovModifierEvent.getNewFovModifier();
    }

    public static double getFieldOfView(GameRenderer renderer, Camera camera, double partialTick, double fov, boolean usedConfiguredFov) {
        ViewportEvent.ComputeFov event = new ViewportEvent.ComputeFov(renderer, camera, partialTick, fov, usedConfiguredFov);
        NeoForge.EVENT_BUS.post(event);
        return event.getFOV();
    }

    public static CalculatePlayerTurnEvent getTurnPlayerValues(double mouseSensitivity, boolean cinematicCameraEnabled) {
        var event = new CalculatePlayerTurnEvent(mouseSensitivity, cinematicCameraEnabled);
        NeoForge.EVENT_BUS.post(event);
        return event;
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

    public static Vector3f getFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, float fogRed, float fogGreen, float fogBlue) {
        // Modify fog color depending on the fluid
        FluidState state = level.getFluidState(camera.getBlockPosition());
        Vector3f fluidFogColor = new Vector3f(fogRed, fogGreen, fogBlue);
        if (camera.getPosition().y < (double) ((float) camera.getBlockPosition().getY() + state.getHeight(level, camera.getBlockPosition())))
            fluidFogColor = IClientFluidTypeExtensions.of(state).modifyFogColor(camera, partialTick, level, renderDistance, darkenWorldAmount, fluidFogColor);

        ViewportEvent.ComputeFogColor event = new ViewportEvent.ComputeFogColor(camera, partialTick, fluidFogColor.x(), fluidFogColor.y(), fluidFogColor.z());
        NeoForge.EVENT_BUS.post(event);

        fluidFogColor.set(event.getRed(), event.getGreen(), event.getBlue());
        return fluidFogColor;
    }

    public static void onFogRender(FogRenderer.FogMode mode, FogType type, Camera camera, float partialTick, float renderDistance, float nearDistance, float farDistance, FogShape shape) {
        // Modify fog rendering depending on the fluid
        FluidState state = camera.getEntity().level().getFluidState(camera.getBlockPosition());
        if (camera.getPosition().y < (double) ((float) camera.getBlockPosition().getY() + state.getHeight(camera.getEntity().level(), camera.getBlockPosition())))
            IClientFluidTypeExtensions.of(state).modifyFogRender(camera, mode, renderDistance, partialTick, nearDistance, farDistance, shape);

        ViewportEvent.RenderFog event = new ViewportEvent.RenderFog(mode, type, camera, partialTick, nearDistance, farDistance, shape);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            RenderSystem.setShaderFogStart(event.getNearPlaneDistance());
            RenderSystem.setShaderFogEnd(event.getFarPlaneDistance());
            RenderSystem.setShaderFogShape(event.getFogShape());
        }
    }

    public static ViewportEvent.ComputeCameraAngles onCameraSetup(GameRenderer renderer, Camera camera, float partial) {
        ViewportEvent.ComputeCameraAngles event = new ViewportEvent.ComputeCameraAngles(renderer, camera, partial, camera.getYRot(), camera.getXRot(), 0);
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    public static void onModifyBakingResult(Map<ResourceLocation, BakedModel> models, Map<ResourceLocation, AtlasSet.StitchResult> stitchResults, ModelBakery modelBakery) {
        Function<Material, TextureAtlasSprite> textureGetter = material -> {
            AtlasSet.StitchResult stitchResult = stitchResults.get(material.atlasLocation());
            TextureAtlasSprite sprite = stitchResult.getSprite(material.texture());
            if (sprite != null) {
                return sprite;
            }
            LOGGER.warn("Failed to retrieve texture '{}' from atlas '{}'", material.texture(), material.atlasLocation(), new Throwable());
            return stitchResult.missing();
        };
        ModLoader.get().postEvent(new ModelEvent.ModifyBakingResult(models, textureGetter, modelBakery));
    }

    public static void onModelBake(ModelManager modelManager, Map<ResourceLocation, BakedModel> models, ModelBakery modelBakery) {
        ModLoader.get().postEvent(new ModelEvent.BakingCompleted(modelManager, Collections.unmodifiableMap(models), modelBakery));
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

    public static void loadEntityShader(Entity entity, GameRenderer entityRenderer) {
        if (entity != null) {
            ResourceLocation shader = EntitySpectatorShaderManager.get(entity.getType());
            if (shader != null) {
                entityRenderer.loadEffect(shader);
            }
        }
    }

    private static int slotMainHand = 0;

    public static boolean shouldCauseReequipAnimation(@NotNull ItemStack from, @NotNull ItemStack to, int slot) {
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
                Minecraft.getInstance().getPartialTick(), bossInfo, x, y, increment);
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

    public static void onMovementInputUpdate(Player player, Input movementInput) {
        NeoForge.EVENT_BUS.post(new MovementInputUpdateEvent(player, movementInput));
    }

    public static boolean onScreenMouseClickedPre(Screen guiScreen, double mouseX, double mouseY, int button) {
        var event = new ScreenEvent.MouseButtonPressed.Pre(guiScreen, mouseX, mouseY, button);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static boolean onScreenMouseClickedPost(Screen guiScreen, double mouseX, double mouseY, int button, boolean handled) {
        Event event = new ScreenEvent.MouseButtonPressed.Post(guiScreen, mouseX, mouseY, button, handled);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult() == Event.Result.DEFAULT ? handled : event.getResult() == Event.Result.ALLOW;
    }

    public static boolean onScreenMouseReleasedPre(Screen guiScreen, double mouseX, double mouseY, int button) {
        var event = new ScreenEvent.MouseButtonReleased.Pre(guiScreen, mouseX, mouseY, button);
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    public static boolean onScreenMouseReleasedPost(Screen guiScreen, double mouseX, double mouseY, int button, boolean handled) {
        Event event = new ScreenEvent.MouseButtonReleased.Post(guiScreen, mouseX, mouseY, button, handled);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult() == Event.Result.DEFAULT ? handled : event.getResult() == Event.Result.ALLOW;
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

    public static void onRecipesUpdated(RecipeManager mgr) {
        Event event = new RecipesUpdatedEvent(mgr);
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

    public static boolean isNameplateInRenderDistance(Entity entity, double squareDistance) {
        if (entity instanceof LivingEntity) {
            final AttributeInstance attribute = ((LivingEntity) entity).getAttribute(NeoForgeMod.NAMETAG_DISTANCE.value());
            if (attribute != null) {
                return !(squareDistance > (attribute.getValue() * attribute.getValue()));
            }
        }
        return !(squareDistance > 4096.0f);
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

    private static final ResourceLocation ICON_SHEET = new ResourceLocation(NeoForgeVersion.MOD_ID, "textures/gui/icons.png");

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
        ModLoader.get().postEvent(new RegisterParticleProvidersEvent(particleEngine));
    }

    public static void onRegisterKeyMappings(Options options) {
        ModLoader.get().postEvent(new RegisterKeyMappingsEvent(options));
    }

    public static void onRegisterAdditionalModels(Set<ResourceLocation> additionalModels) {
        ModLoader.get().postEvent(new ModelEvent.RegisterAdditional(additionalModels));
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

    private static final ChatTypeDecoration SYSTEM_CHAT_TYPE_DECORATION = new ChatTypeDecoration("neoforge.chatType.system", List.of(ChatTypeDecoration.Parameter.CONTENT), Style.EMPTY);
    private static final ChatType SYSTEM_CHAT_TYPE = new ChatType(SYSTEM_CHAT_TYPE_DECORATION, SYSTEM_CHAT_TYPE_DECORATION);
    private static final ChatType.Bound SYSTEM_CHAT_TYPE_BOUND = SYSTEM_CHAT_TYPE.bind(Component.literal("System"));

    @Nullable
    public static Component onClientSystemChat(Component message, boolean overlay) {
        ClientChatReceivedEvent.System event = new ClientChatReceivedEvent.System(SYSTEM_CHAT_TYPE_BOUND, message, overlay);
        return NeoForge.EVENT_BUS.post(event).isCanceled() ? null : event.getMessage();
    }

    @NotNull
    public static String onClientSendMessage(String message) {
        ClientChatEvent event = new ClientChatEvent(message);
        return NeoForge.EVENT_BUS.post(event).isCanceled() ? "" : event.getMessage();
    }

    /**
     * Mimics the behavior of {@link net.minecraft.client.renderer.ItemBlockRenderTypes#getRenderType(BlockState, boolean)}
     * for the input {@link RenderType}.
     */
    @NotNull
    public static RenderType getEntityRenderType(RenderType chunkRenderType, boolean cull) {
        return RenderTypeHelper.getEntityRenderType(chunkRenderType, cull);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "neoforge", bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientEvents {
        @Nullable
        private static ShaderInstance rendertypeEntityTranslucentUnlitShader;

        public static ShaderInstance getEntityTranslucentUnlitShader() {
            return Objects.requireNonNull(rendertypeEntityTranslucentUnlitShader, "Attempted to call getEntityTranslucentUnlitShader before shaders have finished loading.");
        }

        @SubscribeEvent
        public static void registerShaders(RegisterShadersEvent event) throws IOException {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("neoforge", "rendertype_entity_unlit_translucent"), DefaultVertexFormat.NEW_ENTITY), (p_172645_) -> {
                rendertypeEntityTranslucentUnlitShader = p_172645_;
            });
        }
    }

    public static Font getTooltipFont(@NotNull ItemStack stack, Font fallbackFont) {
        Font stackFont = IClientItemExtensions.of(stack).getFont(stack, IClientItemExtensions.FontContext.TOOLTIP);
        return stackFont == null ? fallbackFont : stackFont;
    }

    public static RenderTooltipEvent.Pre onRenderTooltipPre(@NotNull ItemStack stack, GuiGraphics graphics, int x, int y, int screenWidth, int screenHeight, @NotNull List<ClientTooltipComponent> components, @NotNull Font fallbackFont, @NotNull ClientTooltipPositioner positioner) {
        var preEvent = new RenderTooltipEvent.Pre(stack, graphics, x, y, screenWidth, screenHeight, getTooltipFont(stack, fallbackFont), components, positioner);
        NeoForge.EVENT_BUS.post(preEvent);
        return preEvent;
    }

    public static RenderTooltipEvent.Color onRenderTooltipColor(@NotNull ItemStack stack, GuiGraphics graphics, int x, int y, @NotNull Font font, @NotNull List<ClientTooltipComponent> components) {
        var colorEvent = new RenderTooltipEvent.Color(stack, graphics, x, y, font, 0xf0100010, 0x505000FF, 0x5028007f, components);
        NeoForge.EVENT_BUS.post(colorEvent);
        return colorEvent;
    }

    public static List<ClientTooltipComponent> gatherTooltipComponents(ItemStack stack, List<? extends FormattedText> textElements, int mouseX, int screenWidth, int screenHeight, Font fallbackFont) {
        return gatherTooltipComponents(stack, textElements, Optional.empty(), mouseX, screenWidth, screenHeight, fallbackFont);
    }

    public static List<ClientTooltipComponent> gatherTooltipComponents(ItemStack stack, List<? extends FormattedText> textElements, Optional<TooltipComponent> itemComponent, int mouseX, int screenWidth, int screenHeight, Font fallbackFont) {
        Font font = getTooltipFont(stack, fallbackFont);
        List<Either<FormattedText, TooltipComponent>> elements = textElements.stream()
                .map((Function<FormattedText, Either<FormattedText, TooltipComponent>>) Either::left)
                .collect(Collectors.toCollection(ArrayList::new));
        itemComponent.ifPresent(c -> elements.add(1, Either.right(c)));

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

    public static void createWorldConfirmationScreen(Runnable doConfirmedWorldLoad) {
        Component title = Component.translatable("selectWorld.backupQuestion.experimental");
        Component msg = Component.translatable("selectWorld.backupWarning.experimental")
                .append("\n\n")
                .append(Component.translatable("neoforge.selectWorld.backupWarning.experimental.additional"));

        Screen screen = new ConfirmScreen(confirmed -> {
            if (confirmed) {
                doConfirmedWorldLoad.run();
            } else {
                Minecraft.getInstance().setScreen(null);
            }
        }, title, msg, CommonComponents.GUI_PROCEED, CommonComponents.GUI_CANCEL);

        Minecraft.getInstance().setScreen(screen);
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

    public static ResourceLocation getShaderImportLocation(String basePath, boolean isRelative, String importPath) {
        final var loc = new ResourceLocation(importPath);
        final var normalised = FileUtil.normalizeResourcePath(
                (isRelative ? basePath : "shaders/include/") + loc.getPath());
        return new ResourceLocation(loc.getNamespace(), normalised);
    }

    private static final BiMap<ResourceLocation, SpriteSourceType> SPRITE_SOURCE_TYPES_MAP = HashBiMap.create();

    public static BiMap<ResourceLocation, SpriteSourceType> makeSpriteSourceTypesMap() {
        return SPRITE_SOURCE_TYPES_MAP;
    }

    @ApiStatus.Internal
    public static void registerSpriteSourceTypes() {
        ModLoader.get().postEvent(new RegisterSpriteSourceTypesEvent(SPRITE_SOURCE_TYPES_MAP));
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

            float[] uv = faceEntry.getValue().uv.uvs;
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

    // Make sure the below method is only ever called once (by forge).
    private static boolean initializedClientHooks = false;

    // Runs during Minecraft construction, before initial resource loading.
    @ApiStatus.Internal
    public static void initClientHooks(Minecraft mc, ReloadableResourceManager resourceManager) {
        if (initializedClientHooks) {
            throw new IllegalStateException("Client hooks initialized more than once");
        }
        initializedClientHooks = true;

        GameTestHooks.registerGametests();
        registerSpriteSourceTypes();
        MenuScreens.init();
        ModLoader.get().postEvent(new RegisterClientReloadListenersEvent(resourceManager));
        ModLoader.get().postEvent(new EntityRenderersEvent.RegisterLayerDefinitions());
        ModLoader.get().postEvent(new EntityRenderersEvent.RegisterRenderers());
        ClientTooltipComponentManager.init();
        EntitySpectatorShaderManager.init();
        ClientHooks.onRegisterKeyMappings(mc.options);
        RecipeBookManager.init();
        GuiOverlayManager.init();
        DimensionSpecialEffectsManager.init();
        NamedRenderTypeManager.init();
        ColorResolverManager.init();
        ItemDecoratorHandler.init();
        PresetEditorManager.init();
    }
}
