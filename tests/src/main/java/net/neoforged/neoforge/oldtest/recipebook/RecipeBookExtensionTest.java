/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.recipebook;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(RecipeBookExtensionTest.MOD_ID)
public class RecipeBookExtensionTest {
    public static final boolean ENABLED = false;

    public static final String MOD_ID = "recipe_book_extension_test";
    public static final RecipeBookType TEST_TYPE = RecipeBookType.valueOf("NEOTESTS_TESTING");

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, MOD_ID);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipeBookTestRecipe>> RECIPE_BOOK_TEST_RECIPE_SERIALIZER = RECIPE_SERIALIZER.register("test_recipe", RecipeBookTestRecipeSerializer::new);

    public static final DeferredRegister<MenuType<?>> MENU_TYPE = DeferredRegister.create(BuiltInRegistries.MENU, MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<RecipeBookTestMenu>> RECIPE_BOOK_TEST_MENU_TYPE = MENU_TYPE.register("test_recipe_menu", () -> IMenuTypeExtension.create(RecipeBookTestMenu::new));

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPE = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, MOD_ID);
    public static final DeferredHolder<RecipeType<?>, RecipeType<RecipeBookTestRecipe>> RECIPE_BOOK_TEST_RECIPE_TYPE = RECIPE_TYPE.register("test_recipe", () -> RecipeType.simple(getId("test_recipe")));

    public RecipeBookExtensionTest(IEventBus modBus) {
        if (!ENABLED)
            return;

        RECIPE_SERIALIZER.register(modBus);
        MENU_TYPE.register(modBus);
        RECIPE_TYPE.register(modBus);

        NeoForge.EVENT_BUS.addListener(this::onRightClick);
    }

    private void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide)
            return;
        if (event.getLevel().getBlockState(event.getPos()).getBlock() == Blocks.GRASS_BLOCK) {
            event.getEntity().openMenu(new SimpleMenuProvider((id, inv, p) -> new RecipeBookTestMenu(id, inv, ContainerLevelAccess.create(event.getLevel(), event.getPos())), Component.literal("Test")));
        }
    }

    public static ResourceLocation getId(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ClientHandler {
        @SubscribeEvent
        public static void clientSetup(RegisterMenuScreensEvent event) {
            if (!ENABLED)
                return;
            event.register(RECIPE_BOOK_TEST_MENU_TYPE.get(), RecipeBookTestScreen::new);
        }

        @SubscribeEvent
        public static void onRegisterRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
            if (!ENABLED)
                return;
            RecipeBookExtensionClientHelper.init(event);
        }
    }

    public static class RecipeBookTestContainer extends SimpleContainer {
        public RecipeBookTestContainer() {
            super(8);
        }

        public CraftingInput asCraftingInput() {
            return CraftingInput.of(2, 4, getItems());
        }
    }
}
