/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.rendering;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.DistExecutor;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.slf4j.Logger;

@Mod(ShaderResourcesTest.MODID)
public class ShaderResourcesTest {
    private static Logger LOGGER;

    public static final String MODID = "shader_resources_test";
    private static final boolean ENABLE = false;

    public ShaderResourcesTest(IEventBus modEventBus) {
        if (ENABLE) {
            LOGGER = LogUtils.getLogger();

            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClientInit.init(modEventBus);
            }
        }
    }

    private static class ClientInit {
        public static void init(IEventBus modEventBus) {
            modEventBus.addListener(ClientInit::registerShaders);
        }

        public static void registerShaders(final RegisterShadersEvent event) {
            if (!ENABLE)
                return;

            try {
                event.registerShader(
                        new ShaderInstance(
                                event.getResourceProvider(),
                                new ResourceLocation(MODID, "vertex_cubemap"),
                                DefaultVertexFormat.POSITION),
                        shader -> {
                            LOGGER.info("Completely loaded shader {} with no issues", shader.getName());
                        });

                LOGGER.info("Loaded registered shaders with no exceptions");
            } catch (IOException e) {
                LOGGER.error("Failed to load shaders with exception", e);
            }
        }
    }
}
