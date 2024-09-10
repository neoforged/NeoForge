/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.recipebook;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.deferred.DeferredMenuType;
import net.neoforged.neoforge.registries.deferred.DeferredMenuTypes;
import net.neoforged.neoforge.registries.deferred.DeferredRecipeSerializer;
import net.neoforged.neoforge.registries.deferred.DeferredRecipeSerializers;
import net.neoforged.neoforge.registries.deferred.DeferredRecipeType;
import net.neoforged.neoforge.registries.deferred.DeferredRecipeTypes;

@Mod(RecipeBookExtensionTest.MOD_ID)
public class RecipeBookExtensionTest {
    public static final boolean ENABLED = false;

    public static final String MOD_ID = "recipe_book_extension_test";
    public static final RecipeBookType TEST_TYPE = RecipeBookType.valueOf("NEOTESTS_TESTING");

    public static final MapCodec<RecipeBookTestRecipe> RECIPE_BOOK_CODEC = RecipeBookTestRecipe.Ingredients.CODEC.xmap(RecipeBookTestRecipe::new, recipeBookTestRecipe -> recipeBookTestRecipe.ingredients);
    public static final DeferredRecipeSerializers RECIPE_SERIALIZER = DeferredRecipeSerializers.createRecipeSerializers(MOD_ID);
    public static final DeferredRecipeSerializer<RecipeBookTestRecipe> RECIPE_BOOK_TEST_RECIPE_SERIALIZER = RECIPE_SERIALIZER.registerRecipeSerializer("test_recipe", RECIPE_BOOK_CODEC, ByteBufCodecs.fromCodecWithRegistries(RECIPE_BOOK_CODEC.codec()));

    public static final DeferredMenuTypes MENU_TYPE = DeferredMenuTypes.createMenuTypes(MOD_ID);
    public static final DeferredMenuType<RecipeBookTestMenu> RECIPE_BOOK_TEST_MENU_TYPE = MENU_TYPE.registerMenu("test_recipe_menu", RecipeBookTestMenu::new);

    public static final DeferredRecipeTypes RECIPE_TYPE = DeferredRecipeTypes.createRecipeTypes(MOD_ID);
    public static final DeferredRecipeType<RecipeBookTestRecipe> RECIPE_BOOK_TEST_RECIPE_TYPE = RECIPE_TYPE.registerRecipeType("test_recipe");

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
