/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param.general;

import net.minecraft.resources.ResourceLocation;

public record ResourceLocationParam(ResourceLocation resourceLocation) implements IGeneralUsageParam<ResourceLocation> {
    @Override
    public ResourceLocation getValue() {
        return resourceLocation;
    }
}
