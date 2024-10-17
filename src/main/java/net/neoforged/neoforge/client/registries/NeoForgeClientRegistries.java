/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.registries;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.resource.model.TopLevelUnbakedModel;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.registries.RegistryBuilder;

public final class NeoForgeClientRegistries {
    private NeoForgeClientRegistries() {}

    public static final Registry<MapCodec<? extends TopLevelUnbakedModel>> UNBAKED_MODEL_SERIALIZERS = new RegistryBuilder<>(Keys.UNBAKED_MODEL_SERIALIZERS).sync(false).create();

    public static final class Keys {
        private Keys() {}

        public static final ResourceKey<Registry<MapCodec<? extends TopLevelUnbakedModel>>> UNBAKED_MODEL_SERIALIZERS = key("unbaked_model_serializers");

        private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, name));
        }
    }
}
