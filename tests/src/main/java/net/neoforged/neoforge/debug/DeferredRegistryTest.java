/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
@Mod(DeferredRegistryTest.MODID)
public class DeferredRegistryTest {
    static final String MODID = "deferred_registry_test";
    private static final Logger LOGGER = LogManager.getLogger();

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, MODID);
    private static final ResourceKey<Registry<Custom>> CUSTOM_REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(MODID, "test_registry"));
    private static final Registry<Custom> CUSTOM_REG = new RegistryBuilder<>(CUSTOM_REGISTRY_KEY).onAdd((owner, id, key, obj) -> LOGGER.info("Custom Added: " + id + " " + obj.foo())).create();
    private static final DeferredRegister<Custom> CUSTOMS = DeferredRegister.create(CUSTOM_REG, MODID);
    private static final DeferredRegister<Object> DOESNT_EXIST_REG = DeferredRegister.create(new ResourceLocation(MODID, "doesnt_exist"), MODID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, MODID);
    // Vanilla Registry - filled directly after all RegistryEvent.Register events are fired
    private static final DeferredRegister<PosRuleTestType<?>> POS_RULE_TEST_TYPES = DeferredRegister.create(Registries.POS_RULE_TEST, MODID);

    private static final DeferredHolder<Block, Block> BLOCK = BLOCKS.register("test", () -> new Block(Block.Properties.of().mapColor(MapColor.STONE)));
    private static final DeferredHolder<Item, Item> ITEM = ITEMS.register("test", () -> new BlockItem(BLOCK.get(), new Item.Properties()));
    private static final DeferredHolder<Custom, Custom> CUSTOM = CUSTOMS.register("test", () -> new Custom() {});
    // Should never be created as the registry doesn't exist - this should silently fail and remain empty
    private static final DeferredHolder<Object, Object> DOESNT_EXIST = DOESNT_EXIST_REG.register("test", Object::new);
    private static final DeferredHolder<RecipeType<?>, RecipeType<?>> RECIPE_TYPE = RECIPE_TYPES.register("test", () -> new RecipeType<>() {});
    private static final DeferredHolder<PosRuleTestType<?>, PosRuleTestType<?>> POS_RULE_TEST_TYPE = POS_RULE_TEST_TYPES.register("test", () -> () -> null);

//    private static final TagKey<Custom> CUSTOM_TAG_KEY = CUSTOMS.createOptionalTagKey("test_tag", Set.of(CUSTOM));

    public DeferredRegistryTest(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        CUSTOMS.register(modBus);
        RECIPE_TYPES.register(modBus);
        POS_RULE_TEST_TYPES.register(modBus);
        modBus.addListener(this::gatherData);

        NeoForge.EVENT_BUS.addListener(this::serverStarted);
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
            event.accept(ITEM);
    }

    public void serverStarted(ServerStartedEvent event) {
        // Validate all the RegistryObjects are filled / not filled
        BLOCK.get();
        ITEM.get();
        CUSTOM.get();
        if (DOESNT_EXIST.isPresent())
            throw new IllegalStateException("DeferredRegistryTest#DOESNT_EXIST should not be present!");
        RECIPE_TYPE.get();
        //POS_RULE_TEST_TYPE.get();
        //PLACED_FEATURE.get();
    }

    public void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();

        gen.addProvider(event.includeClient(), new BlockStateProvider(gen.getPackOutput(), MODID, event.getExistingFileHelper()) {
            @Override
            protected void registerStatesAndModels() {
                simpleBlockWithItem(BLOCK.get(), models().cubeAll(BLOCK.getId().getPath(), mcLoc("block/furnace_top")));
            }
        });
    }

    public static class Custom {
        public String foo() {
            return this.getClass().getName();
        }
    }
}
