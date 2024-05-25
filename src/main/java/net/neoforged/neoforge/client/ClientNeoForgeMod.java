/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterNamedRenderTypesEvent;
import net.neoforged.neoforge.client.model.CompositeModel;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;
import net.neoforged.neoforge.client.model.ElementsModel;
import net.neoforged.neoforge.client.model.EmptyModel;
import net.neoforged.neoforge.client.model.ItemLayerModel;
import net.neoforged.neoforge.client.model.SeparateTransformsModel;
import net.neoforged.neoforge.client.model.obj.ObjLoader;

@Mod(value = "neoforge", dist = Dist.CLIENT)
public class ClientNeoForgeMod {
    public ClientNeoForgeMod(IEventBus modEventBus) {
        ClientCommandHandler.init();
        TagConventionLogWarningClient.init();

        modEventBus.register(ClientNeoForgeMod.class);
    }

    @SubscribeEvent
    static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ResourceLocation.fromNamespaceAndPath("neoforge", "empty"), EmptyModel.LOADER);
        event.register(ResourceLocation.fromNamespaceAndPath("neoforge", "elements"), ElementsModel.Loader.INSTANCE);
        event.register(ResourceLocation.fromNamespaceAndPath("neoforge", "obj"), ObjLoader.INSTANCE);
        event.register(ResourceLocation.fromNamespaceAndPath("neoforge", "fluid_container"), DynamicFluidContainerModel.Loader.INSTANCE);
        event.register(ResourceLocation.fromNamespaceAndPath("neoforge", "composite"), CompositeModel.Loader.INSTANCE);
        event.register(ResourceLocation.fromNamespaceAndPath("neoforge", "item_layers"), ItemLayerModel.Loader.INSTANCE);
        event.register(ResourceLocation.fromNamespaceAndPath("neoforge", "separate_transforms"), SeparateTransformsModel.Loader.INSTANCE);
    }

    @SubscribeEvent
    static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(ObjLoader.INSTANCE);
    }

    @SubscribeEvent
    static void onRegisterNamedRenderTypes(RegisterNamedRenderTypesEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath("neoforge", "item_unlit"), RenderType.translucent(), NeoForgeRenderTypes.ITEM_UNSORTED_UNLIT_TRANSLUCENT.get());
    }
}
