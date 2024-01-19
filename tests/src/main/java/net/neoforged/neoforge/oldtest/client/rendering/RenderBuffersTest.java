/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.rendering;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.logging.LogUtils;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.slf4j.Logger;

@Mod(RenderBuffersTest.MODID)
public class RenderBuffersTest {
    public static final String MODID = "render_buffers_test";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean ENABLED = true;

    public RenderBuffersTest(IEventBus modEventBus) {
        if (ENABLED && FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(this::registerRenderBuffers);
            modEventBus.addListener(this::testBuffer);
        }
    }

    private void registerRenderBuffers(RegisterRenderBuffersEvent event) {
        event.registerRenderBuffer(RenderType.lightning());
    }

    private void testBuffer(RenderLevelStageEvent.RegisterStageEvent event) {
        Map<RenderType, BufferBuilder> fixedBuffers = ObfuscationReflectionHelper.getPrivateValue(MultiBufferSource.BufferSource.class, Minecraft.getInstance().renderBuffers().bufferSource(), "fixedBuffers");

        if (fixedBuffers != null && fixedBuffers.containsKey(RenderType.lightning())) {
            LOGGER.info("RenderBuffersTest passed");
        } else {
            throw new RuntimeException("RenderBuffersTest failed");
        }
    }
}
