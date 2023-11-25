package net.neoforged.testframework.registration;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.function.Consumer;

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
        consumer.accept(helper.items().registerBlockItem(this, properties));
        hasItem = true;
        return this;
    }

    public DeferredBlockBuilder<T> withLang(String name) {
        helper.provider(LanguageProvider.class, prov -> prov.add(value(), name));
        return this;
    }

    private boolean hasItem = false;
    private boolean hasColor = false;

    public DeferredBlockBuilder<T> withDefaultWhiteModel() {
        helper.provider(BlockStateProvider.class, prov -> {
            final BlockModelBuilder model;
            if (hasColor) {
                model = prov.models().getBuilder(key.location().getPath())
                        .element()
                        .from(0, 0, 0)
                        .to(16, 16, 16)
                        .allFaces((direction, faceBuilder) -> faceBuilder.uvs(0, 0, 16, 16).texture("#all").tintindex(0).cullface(direction))
                        .end()
                        .texture("all", new ResourceLocation("testframework:block/white"))
                        .texture("particle", new ResourceLocation("testframework:block/white"));
            } else {
                model = prov.models().cubeAll(key.location().getPath(), new ResourceLocation("testframework:block/white"));
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
        helper.framework().modEventBus().addListener((final RegisterColorHandlersEvent.Block event) -> event.register((p_92567_, p_92568_, p_92569_, p_92570_) -> color, value()));
        helper.framework().modEventBus().addListener((final RegisterColorHandlersEvent.Item event) -> {
            if (hasItem) {
                event.register((stack, index) -> color, value());
            }
        });
    }
}
