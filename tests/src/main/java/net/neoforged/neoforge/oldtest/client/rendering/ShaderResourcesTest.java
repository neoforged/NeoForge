/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
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

            if (FMLEnvironment.dist.isClient()) {
                ClientInit.init(modEventBus);
            }
        }
    }

    private static class ClientInit {
        private static final ShaderProgram CUBEMAP_SHADER = new ShaderProgram(
                ResourceLocation.fromNamespaceAndPath(MODID, "core/vertex_cubemap"),
                DefaultVertexFormat.POSITION,
                ShaderDefines.EMPTY);
        private static boolean checked = false;

        public static void init(IEventBus modEventBus) {
            modEventBus.addListener(ClientInit::registerShaders);
            NeoForge.EVENT_BUS.addListener(ClientInit::onRenderLevelStage);
        }

        public static void registerShaders(final RegisterShadersEvent event) {
            event.registerShader(CUBEMAP_SHADER);
        }

        private static void onRenderLevelStage(final RenderLevelStageEvent event) {
            if (checked) return;

            if (RenderSystem.setShader(CUBEMAP_SHADER) != null) {
                LOGGER.info("Shader loaded and available");
            } else {
                LOGGER.info("Shader failed to load or compile");
            }
            checked = true;
        }
    }
}
