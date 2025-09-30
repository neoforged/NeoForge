/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.rendering;

import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

// TODO: convert to automated test
@Mod(ShaderResourcesTest.MODID)
public class ShaderResourcesTest {
    private static Logger LOGGER;

    public static final String MODID = "shader_resources_test";
    private static final boolean ENABLE = false;

    public ShaderResourcesTest(IEventBus modEventBus) {
        if (ENABLE) {
            LOGGER = LogUtils.getLogger();

            if (FMLEnvironment.getDist().isClient()) {
                ClientInit.init(modEventBus);
            }
        }
    }

    private static class ClientInit {
        private static final RenderPipeline CUBEMAP_PIPELINE = RenderPipeline.builder()
                .withLocation(ResourceLocation.fromNamespaceAndPath(MODID, "pipeline/vertex_cubemap"))
                .withVertexShader(ResourceLocation.fromNamespaceAndPath(MODID, "core/vertex_cubemap"))
                .withFragmentShader(ResourceLocation.fromNamespaceAndPath(MODID, "core/vertex_cubemap"))
                .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
                .build();
        private static boolean checked = false;

        public static void init(IEventBus modEventBus) {
            modEventBus.addListener(ClientInit::registerShaders);
            NeoForge.EVENT_BUS.addListener(ClientInit::onRenderLevelStage);
        }

        public static void registerShaders(final RegisterRenderPipelinesEvent event) {
            event.registerPipeline(CUBEMAP_PIPELINE);
        }

        private static void onRenderLevelStage(final RenderLevelStageEvent.AfterLevel event) {
            if (checked) return;

            CompiledRenderPipeline compiledPipeline = RenderSystem.getDevice().precompilePipeline(CUBEMAP_PIPELINE);
            if (compiledPipeline.isValid()) {
                LOGGER.info("Shader loaded and available");
            } else {
                LOGGER.info("Shader failed to load or compile");
            }
            checked = true;
        }
    }
}
