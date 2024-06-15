/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.concurrent.atomic.AtomicInteger;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(side = Dist.CLIENT, groups = { "client.render", "render" })
public class RenderTests {
    private static final AtomicInteger STATE = new AtomicInteger();
    @SuppressWarnings("unused") // Referenced by enumextender.json
    public static final EnumProxy<VertexFormatElement.Usage> USAGE_ENUM_PARAMS = new EnumProxy<>(
            VertexFormatElement.Usage.class,
            "neotests:test",
            (VertexFormatElement.Usage.SetupState) (size, type, stride, offset, index) -> STATE.incrementAndGet());

    @TestHolder(description = { "Tests if custom VertexFormatElement.Usage setup and clear callbacks were called" }, enabledByDefault = true)
    static void setupClearVertexFormatElement(final DynamicTest test) {
        test.framework().modEventBus().addListener(RenderLevelStageEvent.RegisterStageEvent.class, event -> {
            var state = new AtomicInteger();
            var usage = USAGE_ENUM_PARAMS.getValue();
            var element = new VertexFormatElement(VertexFormatElement.findNextId(), 0, VertexFormatElement.Type.BYTE, usage, 4);
            var format = VertexFormat.builder().add("TEST", element).build();

            format.setupBufferState();

            if (state.get() != 1) {
                test.fail("VertexFormatElement.Usage setup state callback was not called");
                return;
            }

            format.clearBufferState();

            test.pass();
        });
    }
}
