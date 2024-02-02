/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import org.slf4j.Logger;

@ForEachTest(side = Dist.CLIENT, groups = { "client.render", "render" })
public class RenderTests {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final VertexFormatElement.Usage TEST_USAGE = VertexFormatElement.Usage.create("TEST", "Test",
            (p_167053_, p_167054_, p_167055_, p_167056_, p_167057_, p_167058_) -> LOGGER.info("Setup Test Vertex Attribute"),
            (p_167050_, p_167051_) -> LOGGER.info("Clear Test Vertex Attribute"));
    private static final VertexFormatElement TEST_ELEMENT = new VertexFormatElement(0, VertexFormatElement.Type.BYTE, TEST_USAGE, 4);
    private static final VertexFormat TEST_FORMAT = new VertexFormat(
            ImmutableMap.<String, VertexFormatElement>builder()
                    .put("TEST", TEST_ELEMENT)
                    .build());

    @TestHolder(description = { "Tests if custom VertexFormatElement.Usage was created and attached to VertexFormatElement", "Prints in console on setup and clear state of custom VertexFormatElement.Usage" }, enabledByDefault = true)
    static void setupClearVertexFormatElement(final DynamicTest test) {
        TEST_FORMAT.setupBufferState();
        TEST_FORMAT.clearBufferState();
    }
}
