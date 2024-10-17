/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.neoforge.client.buffer.IBufferDefinition;
import net.neoforged.neoforge.client.buffer.VanillaBufferDefinitions;

public interface IMultiBufferSourceExtension {
    default MultiBufferSource self() {
        return (MultiBufferSource) this;
    }

    default VertexConsumer getBuffer(IBufferDefinition bufferDefinition) {
        return self().getBuffer(VanillaBufferDefinitions.bakeVanillaRenderType(bufferDefinition));
    }
}
