/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param.state;

import net.minecraft.resources.ResourceLocation;

// TODO: make filter types
public record TextureState(ResourceLocation texture, boolean blur, boolean mipmap) {

}
