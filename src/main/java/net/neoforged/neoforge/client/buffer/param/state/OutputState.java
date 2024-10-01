/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param.state;

import com.mojang.blaze3d.pipeline.RenderTarget;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;

public record OutputState(Supplier<RenderTarget> renderTargetSupplier, boolean ignoreTransparency) {
    public static final class Vanilla {
        public static final OutputState OUTLINE_TARGET = new OutputState(() -> Minecraft.getInstance().levelRenderer.entityTarget(), true);
        public static final OutputState TRANSLUCENT_TARGET = new OutputState(() -> Minecraft.getInstance().levelRenderer.getTranslucentTarget(), false);
        public static final OutputState PARTICLES_TARGET = new OutputState(() -> Minecraft.getInstance().levelRenderer.getParticlesTarget(), false);
        public static final OutputState WEATHER_TARGET = new OutputState(() -> Minecraft.getInstance().levelRenderer.getWeatherTarget(), false);
        public static final OutputState CLOUDS_TARGET = new OutputState(() -> Minecraft.getInstance().levelRenderer.getCloudsTarget(), false);
        public static final OutputState ITEM_ENTITY_TARGET = new OutputState(() -> Minecraft.getInstance().levelRenderer.getItemEntityTarget(), false);
    }
}
