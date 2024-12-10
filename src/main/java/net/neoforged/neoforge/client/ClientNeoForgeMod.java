/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import java.util.Optional;
import net.minecraft.DetectedVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.client.color.item.FluidContentsTint;
import net.neoforged.neoforge.client.entity.animation.json.AnimationLoader;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterNamedRenderTypesEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.model.EmptyModel;
import net.neoforged.neoforge.client.model.UnbakedCompositeModel;
import net.neoforged.neoforge.client.model.item.DynamicFluidContainerModel;
import net.neoforged.neoforge.client.model.obj.ObjLoader;
import net.neoforged.neoforge.client.textures.NamespacedDirectoryLister;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.data.internal.NeoForgeAdvancementProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeBiomeTagsProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeBlockTagsProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeDamageTypeTagsProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeDataMapsProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeEnchantmentTagsProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeEntityTypeTagsProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeFluidTagsProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeItemTagsProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeLanguageProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeLootTableProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeRecipeProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeRegistryOrderReportProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeSpriteSourceProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeStructureTagsProvider;
import net.neoforged.neoforge.common.data.internal.VanillaSoundDefinitionsProvider;
import net.neoforged.neoforge.common.util.SelfTest;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Mod(value = "neoforge", dist = Dist.CLIENT)
public class ClientNeoForgeMod {
    private static ResourceLocation neoForgeId(String path) {
        return ResourceLocation.fromNamespaceAndPath("neoforge", path);
    }

    public ClientNeoForgeMod(IEventBus modEventBus, ModContainer container) {
        SelfTest.initClient();

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
    static void onGatherData(GatherDataEvent.Client event) {
        // We perform client and server datagen in a single clientData run to avoid
        // having to juggle two generated resources folders and two runs for no additional benefit.

        event.createProvider(output -> new PackMetadataGenerator(output)
                .add(PackMetadataSection.TYPE, new PackMetadataSection(
                        Component.translatable("pack.neoforge.description"),
                        DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA),
                        Optional.of(new InclusiveRange<>(0, Integer.MAX_VALUE)))));

        event.createProvider(NeoForgeAdvancementProvider::new);
        event.createBlockAndItemTags(NeoForgeBlockTagsProvider::new, NeoForgeItemTagsProvider::new);
        event.createProvider(NeoForgeEntityTypeTagsProvider::new);
        event.createProvider(NeoForgeFluidTagsProvider::new);
        event.createProvider(NeoForgeEnchantmentTagsProvider::new);
        event.createProvider(NeoForgeRecipeProvider.Runner::new);
        event.createProvider(NeoForgeLootTableProvider::new);
        event.createProvider(NeoForgeBiomeTagsProvider::new);
        event.createProvider(NeoForgeStructureTagsProvider::new);
        event.createProvider(NeoForgeDamageTypeTagsProvider::new);
        event.createProvider(NeoForgeRegistryOrderReportProvider::new);
        event.createProvider(NeoForgeDataMapsProvider::new);

        event.createProvider(NeoForgeSpriteSourceProvider::new);
        event.createProvider(VanillaSoundDefinitionsProvider::new);
        event.createProvider(NeoForgeLanguageProvider::new);
    }

    @SubscribeEvent
    static void onRegisterModelLoaders(ModelEvent.RegisterLoaders event) {
        event.register(neoForgeId("empty"), EmptyModel.LOADER);
        event.register(neoForgeId("obj"), ObjLoader.INSTANCE);
        event.register(neoForgeId("composite"), UnbakedCompositeModel.Loader.INSTANCE);
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

    @SubscribeEvent
    static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(neoForgeId("fluid_contents_tint"), FluidContentsTint.MAP_CODEC);
    }

    @SubscribeEvent
    static void registerItemModels(RegisterItemModelsEvent event) {
        event.register(neoForgeId("fluid_container"), DynamicFluidContainerModel.Unbaked.MAP_CODEC);
    }

    // TODO 1.21.4
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    static void registerSpawnEggColors(RegisterColorHandlersEvent.Item event) {
//        SpawnEggItem.eggs().forEach(egg -> {
//            if (event.getItemColors().get(egg) == null) {
//                event.register((stack, layer) -> ARGB.opaque(egg.getColor(layer)), egg);
//            }
//        });
//    }
}
