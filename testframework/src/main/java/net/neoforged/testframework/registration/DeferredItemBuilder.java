/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.registration;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ExtendedModelTemplate;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;

public class DeferredItemBuilder<I extends Item> extends DeferredItem<I> {
    private final RegistrationHelper registrationHelper;

    protected DeferredItemBuilder(ResourceKey<Item> key, RegistrationHelper registrationHelper) {
        super(key);
        this.registrationHelper = registrationHelper;
    }

    public DeferredItemBuilder<I> withLang(String name) {
        registrationHelper.clientProvider(LanguageProvider.class, prov -> prov.add(value(), name));
        return this;
    }

    public DeferredItemBuilder<I> tab(ResourceKey<CreativeModeTab> tab) {
        registrationHelper.eventListeners().accept((final BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey() == tab) {
                event.accept(this);
            }
        });
        return this;
    }

    public DeferredItemBuilder<I> withModel(BiConsumer<I, ItemModelGenerators> consumer) {
        registrationHelper.addClientProvider(client -> new ModelProvider(client.getGenerator().getPackOutput(), registrationHelper.modId()) {
            @Override
            protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
                consumer.accept(value(), itemModels);
            }

            @Override
            protected Stream<? extends Holder<Item>> getKnownItems() {
                return Stream.of(DeferredItemBuilder.this);
            }

            @Override
            protected Stream<? extends Holder<Block>> getKnownBlocks() {
                return Stream.empty();
            }

            @Override
            public String getName() {
                return key.location().getPath() + "-model-generator";
            }
        });
        return this;
    }

    public DeferredItemBuilder<I> withModel(ModelTemplate template) {
        return withModel((item, itemModels) -> itemModels.generateFlatItem(item, template));
    }

    public DeferredItemBuilder<I> withModel(TextureMapping textures, Consumer<ExtendedModelTemplate.Builder> modelConsumer) {
        return withModel((item, itemModels) -> {
            var modelPath = ModelLocationUtils.getModelLocation(item);
            var builder = ExtendedModelTemplate.builder();
            modelConsumer.accept(builder);
            itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(modelPath));
            builder.build().create(item, textures, itemModels.modelOutput);
        });
    }
}
