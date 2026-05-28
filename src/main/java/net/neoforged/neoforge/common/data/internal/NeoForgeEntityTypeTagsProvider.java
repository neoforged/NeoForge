/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityTypeIds;
import net.neoforged.neoforge.common.Tags;

public class NeoForgeEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public NeoForgeEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, "neoforge");
    }

    @Override
    public void addTags(HolderLookup.Provider lookupProvider) {
        tag(Tags.EntityTypes.BOSSES).add(EntityTypeIds.ENDER_DRAGON, EntityTypeIds.WITHER);
        tag(Tags.EntityTypes.MINECARTS).add(EntityTypeIds.MINECART, EntityTypeIds.CHEST_MINECART, EntityTypeIds.FURNACE_MINECART, EntityTypeIds.HOPPER_MINECART, EntityTypeIds.SPAWNER_MINECART, EntityTypeIds.TNT_MINECART, EntityTypeIds.COMMAND_BLOCK_MINECART);
        tag(Tags.EntityTypes.BOATS).addTag(EntityTypeTags.BOAT)
                .add(
                        EntityTypeIds.OAK_CHEST_BOAT,
                        EntityTypeIds.SPRUCE_CHEST_BOAT,
                        EntityTypeIds.BIRCH_CHEST_BOAT,
                        EntityTypeIds.JUNGLE_CHEST_BOAT,
                        EntityTypeIds.ACACIA_CHEST_BOAT,
                        EntityTypeIds.CHERRY_CHEST_BOAT,
                        EntityTypeIds.PALE_OAK_CHEST_BOAT,
                        EntityTypeIds.DARK_OAK_CHEST_BOAT,
                        EntityTypeIds.MANGROVE_CHEST_BOAT,
                        EntityTypeIds.BAMBOO_CHEST_RAFT);
        tag(Tags.EntityTypes.ITEM_FRAMES).add(EntityTypeIds.ITEM_FRAME, EntityTypeIds.GLOW_ITEM_FRAME);
        tag(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED);
        tag(Tags.EntityTypes.TELEPORTING_NOT_SUPPORTED);
    }
}
