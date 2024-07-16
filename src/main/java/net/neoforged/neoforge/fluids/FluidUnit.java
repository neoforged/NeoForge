/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class FluidUnit {
    private static final Map<ResourceLocation, FluidUnit> units = new ConcurrentHashMap<>();

    public static final Codec<FluidUnit> CODEC = ResourceLocation.CODEC.xmap(FluidUnit::get, FluidUnit::name);
    public static final StreamCodec<ByteBuf, FluidUnit> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(FluidUnit::get, FluidUnit::name);

    private final ResourceLocation name;

    /**
     * Gets or creates a FluidUnit for the given name
     */
    public static FluidUnit get(ResourceLocation name) {
        return units.computeIfAbsent(name, FluidUnit::new);
    }

    /**
     * Returns the name of this Fluid Unit.
     */
    public ResourceLocation name() {
        return name;
    }

    /**
     * Returns a translation key suitable for making a component describing this unit
     *
     * @param plural Whether the plural variant should be returned
     */
    public String getDescriptionId(boolean plural) {
        return name.toLanguageKey("fluid_unit") + (plural ? ".plural" : "");
    }

    private FluidUnit(ResourceLocation name) {
        this.name = name;
    }
}
