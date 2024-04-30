/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

public final class NeoForgeItemModelProvider extends ItemModelProvider {
    public NeoForgeItemModelProvider(PackOutput pack, ExistingFileHelper fileHelper) {
        super(pack, NeoForgeVersion.MOD_ID, fileHelper);
    }

    @Override
    protected void registerModels() {
        // potion bucket uses dynamic bucket model
        getBuilder(NeoForgeMod.POTION_BUCKET.getId().withPrefix("item/").toString())
                .parent(new ModelFile.UncheckedModelFile(modLoc("item/bucket")))
                .customLoader((model, fileHelper) -> DynamicFluidContainerModelBuilder.begin(model, fileHelper)
                        .fluid(NeoForgeMod.POTION.getKey())
                        .applyTint(true))
                .end();
    }
}
