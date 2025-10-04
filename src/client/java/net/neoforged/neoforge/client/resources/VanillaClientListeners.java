/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.resources;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.PeriodicNotificationManager;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DryFoliageColorReloadListener;
import net.minecraft.client.resources.FoliageColorReloadListener;
import net.minecraft.client.resources.GrassColorReloadListener;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.client.resources.WaypointStyleManager;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.common.util.VanillaClassToKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Keys for vanilla {@link PreparableReloadListener reload listeners}, used to specify dependency ordering in the {@link AddClientReloadListenersEvent}.
 * <p>
 * Due to the volume of vanilla listeners, these keys are automatically generated based on the class name.
 * 
 * @see {@link VanillaServerListeners} for vanilla server listener names.
 * @see {@link NeoForgeReloadListeners} for Neo-added listener names.
 */
public class VanillaClientListeners {
    private static final Map<Class<?>, ResourceLocation> KNOWN_CLASSES = new LinkedHashMap<>();

    public static final ResourceLocation LANGUAGE = key(LanguageManager.class);

    public static final ResourceLocation TEXTURES = key(TextureManager.class);

    public static final ResourceLocation SHADERS = key(ShaderManager.class);

    public static final ResourceLocation SOUNDS = key(SoundManager.class);

    public static final ResourceLocation SPLASHES = key(SplashManager.class);

    public static final ResourceLocation ATLASES = key(AtlasManager.class);

    public static final ResourceLocation FONTS = key(FontManager.class);

    public static final ResourceLocation GRASS_COLOR = key(GrassColorReloadListener.class);

    public static final ResourceLocation FOLIAGE_COLOR = key(FoliageColorReloadListener.class);

    public static final ResourceLocation DRY_FOLIAGE_COLOR = key(DryFoliageColorReloadListener.class);

    public static final ResourceLocation MODELS = key(ModelManager.class);

    public static final ResourceLocation EQUIPMENT_ASSETS = key(EquipmentAssetManager.class);

    public static final ResourceLocation BLOCK_RENDERER = key(BlockRenderDispatcher.class);

    public static final ResourceLocation ENTITY_RENDERER = key(EntityRenderDispatcher.class);

    public static final ResourceLocation BLOCK_ENTITY_RENDERER = key(BlockEntityRenderDispatcher.class);

    public static final ResourceLocation PARTICLE_RESOURCES = key(ParticleResources.class);

    public static final ResourceLocation WAYPOINT_STYLES = key(WaypointStyleManager.class);

    public static final ResourceLocation LEVEL_RENDERER = key(LevelRenderer.class);

    public static final ResourceLocation CLOUD_RENDERER = key(CloudRenderer.class);

    public static final ResourceLocation GPU_WARNLIST = key(GpuWarnlistManager.class);

    public static final ResourceLocation REGIONAL_COMPLIANCES = key(PeriodicNotificationManager.class);

    /**
     * Sentinel field that will always reference the first reload listener in the vanilla order.
     */
    public static final ResourceLocation FIRST = LANGUAGE;

    /**
     * Sentinel field that will always reference the last reload listener in the vanilla order.
     */
    public static final ResourceLocation LAST = REGIONAL_COMPLIANCES;

    private static ResourceLocation key(Class<? extends PreparableReloadListener> cls) {
        if (KNOWN_CLASSES.containsKey(cls)) {
            // Prevent duplicate registration, in case we accidentally use the same class in two different fields.
            throw new UnsupportedOperationException("Attempted to create two keys for the same class");
        }

        ResourceLocation key = VanillaClassToKey.convert(cls);
        KNOWN_CLASSES.put(cls, key);
        return key;
    }

    @Nullable
    @ApiStatus.Internal
    public static ResourceLocation getNameForClass(Class<? extends PreparableReloadListener> cls) {
        return KNOWN_CLASSES.get(cls);
    }
}
