/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.data.internal;

import java.util.concurrent.CompletableFuture;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;

public class VanillaModelProvider extends ModelProvider {
    private final PackOutput.PathProvider items;
    private final PackOutput.PathProvider models;

    public VanillaModelProvider(PackOutput packOutput) {
        super(packOutput, "minecraft");
        this.items = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
        this.models = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        ItemInfoCollector itemModelOutput = new ItemInfoCollector(this::getKnownItems);
        SimpleModelCollector modelOutput = new SimpleModelCollector();
        new VanillaItemModelProvider(itemModelOutput, modelOutput).run();
        return CompletableFuture.allOf(modelOutput.save(output, this.models), itemModelOutput.save(output, this.items));
    }
}
