/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.resource.model.extensions;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.resources.model.UnbakedModel;

public interface IUnbakedModelExtension {
    MapCodec<? extends UnbakedModel> codec();

    default void setName(String name) {}
}
