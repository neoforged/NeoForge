/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import com.mojang.serialization.Codec;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
@Mod(DeferredRegistryTest.MODID)
public class DeferredRegistryTest {
    static final String MODID = "deferred_registry_test";
    private static final Logger LOGGER = LogManager.getLogger();

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final ResourceKey<Registry<Custom>> CUSTOM_REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MODID, "test_registry"));
    private static final DeferredRegister<Custom> CUSTOMS = DeferredRegister.create(CUSTOM_REGISTRY_KEY, MODID);
    private static final DeferredRegister<Object> DOESNT_EXIST_REG = DeferredRegister.create(Identifier.fromNamespaceAndPath(MODID, "doesnt_exist"), MODID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, MODID);
    private static final DeferredRegister<PosRuleTestType<?>> POS_RULE_TEST_TYPES = DeferredRegister.create(Registries.POS_RULE_TEST, MODID);

    private static final DeferredBlock<Block> BLOCK = BLOCKS.registerSimpleBlock("test", props -> props.mapColor(MapColor.STONE));
    private static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COMPONENT_TYPE = COMPONENTS.registerComponentType("test", builder -> builder.persistent(Codec.INT));
    private static final DeferredItem<BlockItem> ITEM_STANDARD = ITEMS.registerSimpleBlockItem(BLOCK);
    private static final DeferredItem<Item> ITEM_WITH_COMPONENT = ITEMS.registerItem("test_with_component", properties -> new Item(properties.component(COMPONENT_TYPE.get(), 3)));

    // New convenience methods
    private static final DeferredItem<Item> ITEM_SHORT = ITEMS.registerItem("test_item_short", Item::new);
    private static final DeferredItem<BlockItem> BLOCK_ITEM_FROM_HOLDER = ITEMS.registerBlockItem(BLOCK);
    private static final DeferredItem<BlockItem> BLOCK_ITEM_FROM_HOLDER_WITH_PROPS = ITEMS.registerBlockItem(BLOCK, () -> new Item.Properties().stacksTo(32));
    private static final DeferredItem<BlockItem> BLOCK_ITEM_FROM_HOLDER_WITH_OPERATOR = ITEMS.registerBlockItem(BLOCK, props -> props.stacksTo(64).fireResistant());
    private static final DeferredItem<BlockItem> BLOCK_ITEM_WITH_NAME = ITEMS.registerBlockItem("test_block_item_with_name", BLOCK::get);
    private static final DeferredItem<BlockItem> BLOCK_ITEM_WITH_NAME_AND_PROPS = ITEMS.registerBlockItem("test_block_item_with_name_props", BLOCK::get, () -> new Item.Properties().stacksTo(16));
    private static final DeferredItem<BlockItem> BLOCK_ITEM_WITH_NAME_AND_OPERATOR = ITEMS.registerBlockItem("test_block_item_with_name_operator", BLOCK::get, props -> props.stacksTo(1).fireResistant());

    // CustomBlockItem tests - using registerCustomBlockItem
    private static final DeferredItem<CustomBlockItem> CUSTOM_BLOCK_ITEM = ITEMS.registerCustomBlockItem("test_custom_block_item",
        () -> new CustomBlockItem(new Item.Properties()));

    private static final DeferredItem<CustomBlockItem> CUSTOM_BLOCK_ITEM_WITH_PROPS = ITEMS.registerCustomBlockItem("test_custom_block_item_props",
        () -> new CustomBlockItem(new Item.Properties().stacksTo(8)),
        () -> new Item.Properties().stacksTo(8));

    private static final DeferredHolder<Custom, Custom> CUSTOM = CUSTOMS.register("test", () -> new Custom() {});
    private static final DeferredHolder<Object, Object> DOESNT_EXIST = DOESNT_EXIST_REG.register("test", Object::new);
    private static final DeferredHolder<RecipeType<?>, RecipeType<?>> RECIPE_TYPE = RECIPE_TYPES.register("test", () -> new RecipeType<>() {});
    private static final DeferredHolder<PosRuleTestType<?>, PosRuleTestType<?>> POS_RULE_TEST_TYPE = POS_RULE_TEST_TYPES.register("test", () -> () -> null);
    private static final Registry<Custom> CUSTOM_REG = CUSTOMS.makeRegistry(builder -> builder.onAdd((owner, id, key, obj) -> LOGGER.info("Custom Added: " + id + " " + obj.foo())));

    public DeferredRegistryTest(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        COMPONENTS.register(modBus);
        CUSTOMS.register(modBus);
        RECIPE_TYPES.register(modBus);
        POS_RULE_TEST_TYPES.register(modBus);
        modBus.addListener(this::gatherData);
        NeoForge.EVENT_BUS.addListener(this::serverStarted);
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ITEM_STANDARD);
            event.accept(ITEM_SHORT);
            event.accept(BLOCK_ITEM_FROM_HOLDER);
            event.accept(BLOCK_ITEM_FROM_HOLDER_WITH_PROPS);
            event.accept(BLOCK_ITEM_WITH_NAME);
            event.accept(CUSTOM_BLOCK_ITEM);
        }
    }

    public void serverStarted(ServerStartedEvent event) {
        LOGGER.info("=== Testing DeferredRegister Convenience Methods ===");

        BLOCK.get();
        COMPONENT_TYPE.get();
        ITEM_STANDARD.get();
        ITEM_WITH_COMPONENT.get();

        ITEM_SHORT.get();
        BLOCK_ITEM_FROM_HOLDER.get();
        BLOCK_ITEM_FROM_HOLDER_WITH_PROPS.get();
        BLOCK_ITEM_FROM_HOLDER_WITH_OPERATOR.get();
        BLOCK_ITEM_WITH_NAME.get();
        BLOCK_ITEM_WITH_NAME_AND_PROPS.get();
        BLOCK_ITEM_WITH_NAME_AND_OPERATOR.get();
        CUSTOM_BLOCK_ITEM.get();
        CUSTOM_BLOCK_ITEM_WITH_PROPS.get();

        if (DOESNT_EXIST.isBound()) {
            throw new IllegalStateException("DeferredRegistryTest#DOESNT_EXIST should not be present!");
        }

        RECIPE_TYPE.get();
        POS_RULE_TEST_TYPE.get();
        CUSTOM.get();

        LOGGER.info("=== All DeferredRegistryTest validations passed! ===");
    }

    public void gatherData(GatherDataEvent.Client event) {
        DataGenerator gen = event.getGenerator();
        gen.addProvider(true, new ModelProvider(gen.getPackOutput(), MODID) {
            @Override
            protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
                blockModels.createTrivialBlock(BLOCK.value(), TexturedModel.CUBE.updateTexture(textures -> textures.put(TextureSlot.ALL, TextureMapping.getBlockTexture(Blocks.FURNACE, "_top"))));
            }
        });
    }

    public static class Custom {
        public String foo() {
            return this.getClass().getName();
        }
    }

    public static class CustomBlockItem extends BlockItem {
        public CustomBlockItem(Item.Properties properties) {
            super(BLOCK.get(), properties);
        }
    }
}
