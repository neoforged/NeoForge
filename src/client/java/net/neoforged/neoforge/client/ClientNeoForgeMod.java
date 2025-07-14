/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import java.util.Optional;
import net.minecraft.DetectedVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
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
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.client.color.item.FluidContentsTint;
import net.neoforged.neoforge.client.command.ClientConfigCommand;
import net.neoforged.neoforge.client.config.NeoForgeClientConfig;
import net.neoforged.neoforge.client.data.internal.NeoForgeSpriteSourceProvider;
import net.neoforged.neoforge.client.entity.animation.json.AnimationLoader;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterNamedRenderTypesEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.fluids.FluidRenderer;
import net.neoforged.neoforge.client.fluids.FluidRendererProvider;
import net.neoforged.neoforge.client.fluids.FluidRenderers;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.internal.SelfTestClient;
import net.neoforged.neoforge.client.loading.ClientModLoader;
import net.neoforged.neoforge.client.model.CompositeUnbakedModel;
import net.neoforged.neoforge.client.model.EmptyModel;
import net.neoforged.neoforge.client.model.block.CompositeBlockModel;
import net.neoforged.neoforge.client.model.item.DynamicFluidContainerModel;
import net.neoforged.neoforge.client.model.obj.ObjLoader;
import net.neoforged.neoforge.client.resources.VanillaClientListeners;
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
import net.neoforged.neoforge.common.data.internal.NeoForgeStructureTagsProvider;
import net.neoforged.neoforge.common.data.internal.VanillaSoundDefinitionsProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.internal.BrandingControl;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.resource.NeoForgeReloadListeners;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Mod(value = "neoforge", dist = Dist.CLIENT)
public class ClientNeoForgeMod {
    private static ResourceLocation neoForgeId(String path) {
        return ResourceLocation.fromNamespaceAndPath("neoforge", path);
    }

    public ClientNeoForgeMod(IEventBus modEventBus, ModContainer container) {
        SelfTestClient.initClient();

        ClientCommandHandler.init();
        TagConventionLogWarningClient.init();

        modEventBus.register(ClientNeoForgeMod.class);

        container.registerConfig(ModConfig.Type.CLIENT, NeoForgeClientConfig.SPEC);
        modEventBus.register(NeoForgeClientConfig.class);

        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        // Reset WORLD type config caches
        NeoForge.EVENT_BUS.addListener((final ClientPlayerNetworkEvent.LoggingOut event) -> {
            ModConfigs.getFileMap().values().forEach(config -> {
                if (config.getSpec() instanceof ModConfigSpec spec) {
                    spec.resetCaches(ModConfigSpec.RestartType.WORLD);
                }
            });
        });

        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, event -> {
            ClientConfigCommand.register(event.getDispatcher());
        });
    }

    @SubscribeEvent
    static void onGatherData(GatherDataEvent.Client event) {
        // We perform client and server datagen in a single clientData run to avoid
        // having to juggle two generated resources folders and two runs for no additional benefit.

        event.createProvider(output -> new PackMetadataGenerator(output)
                .add(PackMetadataSection.TYPE, new PackMetadataSection(
                        Component.translatable("pack.neoforge.description"),
                        DetectedVersion.BUILT_IN.packVersion(PackType.SERVER_DATA),
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
        event.register(neoForgeId("composite"), CompositeUnbakedModel.Loader.INSTANCE);
    }

    @SubscribeEvent
    static void onRegisterReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(NeoForgeReloadListeners.CLIENT_MOD_LOADING, ClientModLoader::onResourceReload);
        event.addListener(NeoForgeReloadListeners.BRANDING, BrandingControl.resourceManagerReloadListener());

        // These run before vanilla reload listeners.
        event.addDependency(NeoForgeReloadListeners.CLIENT_MOD_LOADING, NeoForgeReloadListeners.BRANDING);
        event.addDependency(NeoForgeReloadListeners.BRANDING, VanillaClientListeners.FIRST);

        event.addListener(NeoForgeReloadListeners.OBJ_LOADER, ObjLoader.INSTANCE);
        event.addListener(NeoForgeReloadListeners.ENTITY_ANIMATIONS, AnimationLoader.INSTANCE);
    }

    @SubscribeEvent
    static void onRegisterNamedRenderTypes(RegisterNamedRenderTypesEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath("neoforge", "item_unlit"), ChunkSectionLayer.TRANSLUCENT, NeoForgeRenderTypes.ITEM_UNSORTED_UNLIT_TRANSLUCENT.get());
    }

    @SubscribeEvent
    static void onRegisterSpriteSourceTypes(RegisterSpriteSourcesEvent event) {
        event.register(NamespacedDirectoryLister.ID, NamespacedDirectoryLister.CODEC);
    }

    @SubscribeEvent
    static void onRegisterFluidRenderers(RegisterFluidRenderersEvent event) {
        event.register(NeoForgeMod.MILK.get(), FluidRendererProvider.vanilla(
            ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "block/milk_still"),
            ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "block/milk_flowing"),
            null
        ));
    }

    @SubscribeEvent
    static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(neoForgeId("fluid_contents_tint"), FluidContentsTint.MAP_CODEC);
    }

    @SubscribeEvent
    static void registerItemModels(RegisterItemModelsEvent event) {
        event.register(neoForgeId("fluid_container"), DynamicFluidContainerModel.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    static void registerBlockStateModels(RegisterBlockStateModels event) {
        event.registerModel(neoForgeId("composite"), CompositeBlockModel.Unbaked.MAP_CODEC);
    }
}
