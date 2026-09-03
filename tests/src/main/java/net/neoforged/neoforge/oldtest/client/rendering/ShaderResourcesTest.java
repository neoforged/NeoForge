/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.rendering;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
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
                .withLocation(Identifier.fromNamespaceAndPath(MODID, "pipeline/vertex_cubemap"))
                .withVertexShader(Identifier.fromNamespaceAndPath(MODID, "core/vertex_cubemap"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(MODID, "core/vertex_cubemap"))
                .withVertexBinding(0, DefaultVertexFormat.POSITION)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .build();

        public static void init(IEventBus modEventBus) {
            modEventBus.addListener(ClientInit::registerShaders);
        }

        public static void registerShaders(final RegisterRenderPipelinesEvent event) {
            event.registerPipeline(CUBEMAP_PIPELINE);
        }
    }
}
