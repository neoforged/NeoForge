/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.registration;

import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jetbrains.annotations.Nullable;

public class DeferredBlockBuilder<T extends Block> extends DeferredBlock<T> {
    private final RegistrationHelper helper;

    protected DeferredBlockBuilder(ResourceKey<Block> key, RegistrationHelper helper) {
        super(key);
        this.helper = helper;
    }

    public DeferredBlockBuilder<T> withBlockItem() {
        return withBlockItem(new Item.Properties(), c -> {});
    }

    public DeferredBlockBuilder<T> withBlockItem(Consumer<DeferredItemBuilder<BlockItem>> consumer) {
        return withBlockItem(new Item.Properties(), consumer);
    }

    public DeferredBlockBuilder<T> withBlockItem(Item.Properties properties, Consumer<DeferredItemBuilder<BlockItem>> consumer) {
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
        helper.clientProvider(BlockStateProvider.class, prov -> {
            final BlockModelBuilder model;
            if (hasColor) {
                model = prov.models().getBuilder(key.location().getPath())
                        .element()
                        .from(0, 0, 0)
                        .to(16, 16, 16)
                        .allFaces((direction, faceBuilder) -> faceBuilder.uvs(0, 0, 16, 16).texture("#all").tintindex(0).cullface(direction))
                        .end()
                        .texture("all", ResourceLocation.fromNamespaceAndPath("testframework", "block/white"))
                        .texture("particle", ResourceLocation.fromNamespaceAndPath("testframework", "block/white"));
            } else {
                model = prov.models().cubeAll(key.location().getPath(), ResourceLocation.fromNamespaceAndPath("testframework", "block/white"));
            }
            if (hasItem) {
                prov.simpleBlockWithItem(value(), model);
            } else {
                prov.simpleBlock(value(), model);
            }
        });
        return this;
    }

    public DeferredBlockBuilder<T> withColor(int color) {
        if (FMLLoader.getDist().isClient()) {
            colorInternal(color);
        }
        hasColor = true;
        return this;
    }

    private void colorInternal(int color) {
        //Capture the color into a local tint source, which has a unit mapcodec for serialization
        final ConstantItemTintSourceBuilder source = new ConstantItemTintSourceBuilder(color);

        helper.eventListeners().accept((final RegisterColorHandlersEvent.Block event) -> event.register((p_92567_, p_92568_, p_92569_, p_92570_) -> color, value()));
        helper.eventListeners().accept((final RegisterColorHandlersEvent.ItemTintSources event) -> {
            if (hasItem) {
                event.register(key.location(), source.type());
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
        public int calculate(ItemStack p_388652_, @Nullable ClientLevel p_390356_, @Nullable LivingEntity p_390510_) {
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
