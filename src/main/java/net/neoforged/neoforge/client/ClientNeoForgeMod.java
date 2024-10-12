/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.client.entity.animation.json.AnimationLoader;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterNamedRenderTypesEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.model.CompositeModel;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;
import net.neoforged.neoforge.client.model.EmptyModel;
import net.neoforged.neoforge.client.model.ItemLayerModel;
import net.neoforged.neoforge.client.model.SeparateTransformsModel;
import net.neoforged.neoforge.client.model.obj.ObjLoader;
import net.neoforged.neoforge.client.textures.NamespacedDirectoryLister;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Mod(value = "neoforge", dist = Dist.CLIENT)
public class ClientNeoForgeMod {
    public ClientNeoForgeMod(IEventBus modEventBus, ModContainer container) {
        ClientCommandHandler.init();
        TagConventionLogWarningClient.init();

        modEventBus.register(ClientNeoForgeMod.class);

        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        // Reset WORLD type config caches
        NeoForge.EVENT_BUS.addListener((final ClientPlayerNetworkEvent.LoggingOut event) -> {
            ModConfigs.getFileMap().values().forEach(config -> {
                if (config.getSpec() instanceof ModConfigSpec spec) {
                    spec.resetCaches(ModConfigSpec.RestartType.WORLD);
                }
            });
        });
    }

    @SubscribeEvent
    static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ResourceLocation.fromNamespaceAndPath("neoforge", "empty"), EmptyModel.LOADER);
        event.register(ResourceLocation.fromNamespaceAndPath("neoforge", "obj"), ObjLoader.INSTANCE);
        event.register(ResourceLocation.fromNamespaceAndPath("neoforge", "fluid_container"), DynamicFluidContainerModel.Loader.INSTANCE);
        event.register(ResourceLocation.fromNamespaceAndPath("neoforge", "composite"), CompositeModel.Loader.INSTANCE);
        event.register(ResourceLocation.fromNamespaceAndPath("neoforge", "item_layers"), ItemLayerModel.Loader.INSTANCE);
        event.register(ResourceLocation.fromNamespaceAndPath("neoforge", "separate_transforms"), SeparateTransformsModel.Loader.INSTANCE);
    }

    @SubscribeEvent
    static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(ObjLoader.INSTANCE);
        event.registerReloadListener(AnimationLoader.INSTANCE);
    }

    @SubscribeEvent
    static void onRegisterNamedRenderTypes(RegisterNamedRenderTypesEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath("neoforge", "item_unlit"), RenderType.translucent(), NeoForgeRenderTypes.ITEM_UNSORTED_UNLIT_TRANSLUCENT.get());
    }

    @SubscribeEvent
    static void onRegisterSpriteSourceTypes(RegisterSpriteSourceTypesEvent event) {
        event.register(NamespacedDirectoryLister.ID, NamespacedDirectoryLister.TYPE);
    }

    @SubscribeEvent
    static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            private static final ResourceLocation UNDERWATER_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/underwater.png");
            private static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
            private static final ResourceLocation WATER_FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");
            private static final ResourceLocation WATER_OVERLAY = ResourceLocation.withDefaultNamespace("block/water_overlay");

            @Override
            public ResourceLocation getStillTexture() {
                return WATER_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return WATER_FLOW;
            }

            @Override
            public ResourceLocation getOverlayTexture() {
                return WATER_OVERLAY;
            }

            @Override
            public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
                return UNDERWATER_LOCATION;
            }

            @Override
            public int getTintColor() {
                return 0xFF3F76E4;
            }

            @Override
            public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                return BiomeColors.getAverageWaterColor(getter, pos) | 0xFF000000;
            }
        }, NeoForgeMod.WATER_TYPE.value());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            private static final ResourceLocation LAVA_STILL = ResourceLocation.withDefaultNamespace("block/lava_still");
            private static final ResourceLocation LAVA_FLOW = ResourceLocation.withDefaultNamespace("block/lava_flow");

            @Override
            public ResourceLocation getStillTexture() {
                return LAVA_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return LAVA_FLOW;
            }
        }, NeoForgeMod.LAVA_TYPE.value());

        NeoForgeMod.MILK_TYPE.asOptional().ifPresent(milkType -> event.registerFluidType(new IClientFluidTypeExtensions() {
            private static final ResourceLocation MILK_STILL = ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "block/milk_still");
            private static final ResourceLocation MILK_FLOW = ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "block/milk_flowing");

            @Override
            public ResourceLocation getStillTexture() {
                return MILK_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return MILK_FLOW;
            }
        }, milkType));
    }
}
