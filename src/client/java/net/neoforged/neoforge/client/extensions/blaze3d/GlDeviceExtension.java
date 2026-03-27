/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions.blaze3d;

import com.mojang.blaze3d.textures.GpuTexture;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public interface GlDeviceExtension {
    GpuTexture createExternalTexture(@Nullable String label, int usage, int nativeId);
}
