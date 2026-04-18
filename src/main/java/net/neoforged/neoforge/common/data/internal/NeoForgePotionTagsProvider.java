/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.PotionTagsProvider;
import net.neoforged.neoforge.common.Tags;

public final class NeoForgePotionTagsProvider extends PotionTagsProvider {
    public NeoForgePotionTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    public void addTags(HolderLookup.Provider lookupProvider) {
        tag(Tags.Potions.HIDDEN_FROM_RECIPE_VIEWERS);
    }
}
