/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.registration;

import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jspecify.annotations.Nullable;

public class DeferredBlockBuilder<T extends Block> extends DeferredBlock<T> {
    private final RegistrationHelper helper;

    protected DeferredBlockBuilder(ResourceKey<Block> key, RegistrationHelper helper) {
        super(key);
        this.helper = helper;
    }

    public DeferredBlockBuilder<T> withBlockItem() {
        return withBlockItem(UnaryOperator.identity(), c -> {});
    }

    public DeferredBlockBuilder<T> withBlockItem(Consumer<DeferredItemBuilder<BlockItem>> consumer) {
        return withBlockItem(UnaryOperator.identity(), consumer);
    }

    /**
     * @deprecated Use {@link #withBlockItem(Supplier, Consumer)} or {@link #withBlockItem(UnaryOperator, Consumer)} instead
     */
    @Deprecated(since = "1.21.10", forRemoval = true)
    public DeferredBlockBuilder<T> withBlockItem(Item.Properties properties, Consumer<DeferredItemBuilder<BlockItem>> consumer) {
        return this.withBlockItem(() -> properties, consumer);
    }

    public DeferredBlockBuilder<T> withBlockItem(Supplier<Item.Properties> properties, Consumer<DeferredItemBuilder<BlockItem>> consumer) {
        consumer.accept(helper.items().registerSimpleBlockItem(this, properties));
        hasItem = true;
        return this;
    }

    public DeferredBlockBuilder<T> withBlockItem(UnaryOperator<Item.Properties> properties, Consumer<DeferredItemBuilder<BlockItem>> consumer) {
        consumer.accept(helper.items().registerSimpleBlockItem(this, properties));
        hasItem = true;
        return this;
    }

    public DeferredBlockBuilder<T> withLang(String name) {
        helper.clientProvider(LanguageProvider.class, prov -> prov.add(value(), name));
        return this;
    }

    private boolean hasItem = false;
    private boolean hasColor = false;

    public DeferredBlockBuilder<T> withDefaultWhiteModel() {
        if (!FMLEnvironment.getDist().isClient()) {
            return this;
        }

        helper.addClientProvider(event -> event.addProvider(new ModelProvider(event.getGenerator().getPackOutput(), helper.modId()) {
            @Override
            protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
                ModelTemplate template;

                if (hasColor) {
                    template = ExtendedModelTemplateBuilder.builder()
                            .element(element -> element
                                    .from(0F, 0F, 0F)
                                    .to(16F, 16F, 16F)
                                    .allFaces((face, builder) -> builder
                                            .uvs(0F, 0F, 16F, 16F)
                                            .texture(TextureSlot.ALL)
                                            .tintindex(0)
                                            .cullface(face)))
                            .requiredTextureSlot(TextureSlot.ALL)
                            .build();
                } else {
                    template = ModelTemplates.CUBE_ALL;
                }

                var modelPath = template.create(value(), TextureMapping.cube(new Material(Identifier.fromNamespaceAndPath("testframework", "block/white"))), blockModels.modelOutput);
                blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(value(), BlockModelGenerators.plainVariant(modelPath)));
            }

            @Override
            protected Stream<? extends Holder<Item>> getKnownItems() {
                return hasItem ? Stream.of(helper.items().createHolder(Registries.ITEM, key.identifier())) : Stream.empty();
            }

            @Override
            protected Stream<? extends Holder<Block>> getKnownBlocks() {
                return Stream.of(DeferredBlockBuilder.this);
            }

            @Override
            public String getName() {
                return key.identifier().toDebugFileName() + "-default-white-model-generator";
            }
        }));
        return this;
    }

    public DeferredBlockBuilder<T> withColor(int color) {
        if (FMLEnvironment.getDist().isClient()) {
            colorInternal(color);
        }
        hasColor = true;
        return this;
    }

    private void colorInternal(int color) {
        //Capture the color into a local tint source, which has a unit mapcodec for serialization
        final ConstantItemTintSourceBuilder source = new ConstantItemTintSourceBuilder(color);

        helper.eventListeners().accept((final RegisterColorHandlersEvent.Block event) -> event.register((state, level, pos, tintIndex) -> color, value()));
        helper.eventListeners().accept((final RegisterColorHandlersEvent.ItemTintSources event) -> {
            if (hasItem) {
                event.register(key.identifier(), source.type());
            }
        });
    }

    private static final class ConstantItemTintSourceBuilder implements ItemTintSource {
        public final MapCodec<ConstantItemTintSourceBuilder> codec = MapCodec.unit(this);

        private final int color;

        private ConstantItemTintSourceBuilder(int color) {
            this.color = color;
        }

        @Override
        public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            return color;
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() {
            return codec;
        }

        public int color() {
            return color;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (ConstantItemTintSourceBuilder) obj;
            return this.color == that.color;
        }

        @Override
        public int hashCode() {
            return Objects.hash(color);
        }

        @Override
        public String toString() {
            return "ConstantItemTintSourceBuilder[" +
                    "color=" + color + ']';
        }
    }
}
