/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

/**
 * We use this to transfer the actual recipe content from server to client.
 */
@ApiStatus.Internal
public record RecipeContentPayload(
        Set<RecipeType<?>> recipeTypes,
        List<RecipeHolder<?>> recipes) implements CustomPacketPayload {
    public static final Type<RecipeContentPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "recipe_content"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeContentPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.RECIPE_TYPE).apply(ByteBufCodecs.collection(HashSet::new)), RecipeContentPayload::recipeTypes,
            RecipeHolder.STREAM_CODEC.apply(ByteBufCodecs.list()), RecipeContentPayload::recipes,
            RecipeContentPayload::new);

    public static RecipeContentPayload create(Collection<RecipeType<?>> recipeTypes, RecipeMap recipes) {
        var recipeTypeSet = Set.copyOf(recipeTypes);
        // Fast-path for empty recipe type set (if no mod wants to sync anything)
        if (recipeTypeSet.isEmpty()) {
            return new RecipeContentPayload(recipeTypeSet, List.of());
        } else {
            var recipeSubset = recipes.values().stream().filter(h -> recipeTypeSet.contains(h.value().getType())).toList();
            return new RecipeContentPayload(recipeTypeSet, recipeSubset);
        }
    }

    @Override
    public Type<RecipeContentPayload> type() {
        return TYPE;
    }
}
