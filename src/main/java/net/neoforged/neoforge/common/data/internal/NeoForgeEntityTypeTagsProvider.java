/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class NeoForgeEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public NeoForgeEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, "neoforge", existingFileHelper);
    }

    @Override
    public void addTags(HolderLookup.Provider lookupProvider) {
        tag(Tags.EntityTypes.BOSSES).add(EntityType.ENDER_DRAGON, EntityType.WITHER);
        tag(Tags.EntityTypes.MINECARTS).add(EntityType.MINECART, EntityType.CHEST_MINECART, EntityType.FURNACE_MINECART, EntityType.HOPPER_MINECART, EntityType.SPAWNER_MINECART, EntityType.TNT_MINECART, EntityType.COMMAND_BLOCK_MINECART);
        tag(Tags.EntityTypes.BOATS).addTag(EntityTypeTags.BOAT)
                .add(
                        EntityType.OAK_CHEST_BOAT,
                        EntityType.SPRUCE_CHEST_BOAT,
                        EntityType.BIRCH_CHEST_BOAT,
                        EntityType.JUNGLE_CHEST_BOAT,
                        EntityType.ACACIA_CHEST_BOAT,
                        EntityType.CHERRY_CHEST_BOAT,
                        EntityType.DARK_OAK_CHEST_BOAT,
                        EntityType.MANGROVE_CHEST_BOAT,
                        EntityType.BAMBOO_CHEST_RAFT);
        tag(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED);
        tag(Tags.EntityTypes.TELEPORTING_NOT_SUPPORTED);

        // Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
        // TODO: Remove backwards compat tag entries in 1.22
        tag(Tags.EntityTypes.BOSSES).addOptionalTag(ResourceLocation.fromNamespaceAndPath("forge", "bosses"));
        tag(Tags.EntityTypes.MINECARTS).addOptionalTag(ResourceLocation.fromNamespaceAndPath("forge", "minecarts"));
        tag(Tags.EntityTypes.BOATS).addOptionalTag(ResourceLocation.fromNamespaceAndPath("forge", "boats"));
    }
}
