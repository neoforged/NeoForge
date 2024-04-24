/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.crafting;

import com.google.gson.JsonArray;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.condition.TestEnabledIngredient;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.registration.RegistrationHelper;
import org.jetbrains.annotations.Nullable;

@ForEachTest(groups = "crafting.ingredient")
public class IngredientTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if partial NBT ingredients match the correct stacks")
    static void partialNBTIngredient(final DynamicTest test, final RegistrationHelper reg) {
        reg.addProvider(event -> new RecipeProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void buildRecipes(RecipeOutput output) {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.ALLIUM)
                        .pattern("IDE")
                        .define('I', new TestEnabledIngredient(
                                DataComponentIngredient.of(false, DataComponents.DAMAGE, 2, Items.IRON_AXE),
                                test.framework(), test.id()).toVanilla())
                        .define('D', Items.DIAMOND)
                        .define('E', Items.EMERALD)
                        .unlockedBy("has_axe", has(Items.IRON_AXE))
                        .save(output, new ResourceLocation(reg.modId(), "partial_nbt"));
            }
        });

        test.onGameTest(helper -> helper
                .startSequence()
                .thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.CRAFTER.defaultBlockState().setValue(BlockStateProperties.ORIENTATION, FrontAndTop.UP_NORTH).setValue(CrafterBlock.CRAFTING, true)))
                .thenExecute(() -> helper.setBlock(1, 2, 1, Blocks.CHEST))

                .thenMap(() -> helper.requireBlockEntity(1, 1, 1, CrafterBlockEntity.class))
                .thenExecute(crafter -> crafter.setItem(0, Items.IRON_AXE.getDefaultInstance()))
                .thenExecute(crafter -> crafter.setItem(1, Items.DIAMOND.getDefaultInstance()))
                .thenExecute(crafter -> crafter.setItem(2, Items.EMERALD.getDefaultInstance()))
                .thenIdle(3)

                // Axe is not damaged, the recipe shouldn't work
                .thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
                .thenExecuteAfter(7, () -> helper.assertContainerEmpty(1, 2, 1))

                .thenIdle(5) // Crafter cooldown

                // Axe is now damaged, we expect the recipe to work, even if we also add other random values to the compound
                .thenExecute(crafter -> crafter.getItem(0).hurtAndBreak(2, helper.getLevel().random, null, () -> {}))
                .thenExecute(crafter -> CustomData.update(DataComponents.CUSTOM_DATA, crafter.getItem(0), tag -> tag.putFloat("abcd", helper.getLevel().random.nextFloat())))

                .thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
                .thenExecuteAfter(7, () -> helper.assertContainerContains(1, 2, 1, Items.ALLIUM))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if strict NBT ingredients match the correct stacks")
    static void strictNBTIngredient(final DynamicTest test, final RegistrationHelper reg) {
        reg.addProvider(event -> new RecipeProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void buildRecipes(RecipeOutput output) {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.ACACIA_BOAT)
                        .requires(new TestEnabledIngredient(
                                DataComponentIngredient.of(true, DataComponents.DAMAGE, 4, Items.DIAMOND_PICKAXE),
                                test.framework(), test.id()).toVanilla())
                        .requires(Items.ACACIA_PLANKS)
                        .unlockedBy("has_pick", has(Items.DIAMOND_PICKAXE))
                        .save(output, new ResourceLocation(reg.modId(), "strict_nbt"));
            }
        });

        test.onGameTest(helper -> helper
                .startSequence()
                .thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.CRAFTER.defaultBlockState().setValue(BlockStateProperties.ORIENTATION, FrontAndTop.UP_NORTH).setValue(CrafterBlock.CRAFTING, true)))
                .thenExecute(() -> helper.setBlock(1, 2, 1, Blocks.CHEST))

                .thenMap(() -> helper.requireBlockEntity(1, 1, 1, CrafterBlockEntity.class))
                .thenExecute(crafter -> crafter.setItem(1, new ItemStack(Items.DIAMOND_PICKAXE.builtInRegistryHolder(), 1, net.minecraft.core.component.DataComponentPatch.builder().set(net.minecraft.core.component.DataComponents.DAMAGE, 1).build())))
                .thenExecute(crafter -> crafter.setItem(0, Items.ACACIA_PLANKS.getDefaultInstance()))
                .thenIdle(3)

                // Pickaxe is damaged, but not enough, so, the recipe shouldn't work
                .thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
                .thenExecuteAfter(7, () -> helper.assertContainerEmpty(1, 2, 1))

                .thenIdle(5) // Crafter cooldown

                // Pickaxe now has a damage value of 4, but superfluous nbt tags, so the recipe should still not work
                .thenExecute(crafter -> crafter.getItem(1).hurtAndBreak(3, helper.getLevel().random, null, () -> {}))
                .thenExecute(crafter -> CustomData.update(DataComponents.CUSTOM_DATA, crafter.getItem(1), tag -> tag.putFloat("abcd", 12f)))

                .thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
                .thenExecuteAfter(7, () -> helper.assertContainerEmpty(1, 2, 1))

                .thenIdle(5) // Crafter cooldown

                // The superfluous element is gone, so the recipe should now work
                .thenExecute(crafter -> CustomData.update(DataComponents.CUSTOM_DATA, crafter.getItem(1), tag -> tag.remove("abcd")))
                .thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
                .thenExecuteAfter(7, () -> helper.assertContainerContains(1, 2, 1, Items.ACACIA_BOAT))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Serialization tests for empty ingredients")
    static void emptyIngredientSerialization(ExtendedGameTestHelper helper) {
        // Make sure that empty ingredients serialize to []
        var emptyResult = Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, Ingredient.EMPTY);
        var emptyJson = emptyResult.resultOrPartial(error -> helper.fail("Failed to serialize empty ingredient: " + error)).orElseThrow();
        helper.assertValueEqual("[]", emptyJson.toString(), "empty ingredient");

        // Make sure that [] deserializes to an empty ingredient
        var result = Ingredient.CODEC.parse(JsonOps.INSTANCE, new JsonArray());
        var ingredient = result.resultOrPartial(error -> helper.fail("Failed to deserialize empty ingredient: " + error)).orElseThrow();
        helper.assertValueEqual(Ingredient.EMPTY, ingredient, "empty ingredient");
        helper.assertTrue(Ingredient.EMPTY == ingredient, "Reference equality with Ingredient.EMPTY");

        helper.succeed();
    }

    private static final RegistrationHelper REG_HELPER = RegistrationHelper.create("neotests_ingredients");

    @OnInit
    static void register(final TestFramework framework) {
        REG_HELPER.register(framework.modEventBus());
    }

    private static final DeferredHolder<RecipeSerializer<?>, CompressedShapelessRecipeSerializer> COMPRESSED_SHAPELESS_SERIALIZER = REG_HELPER
            .registrar(Registries.RECIPE_SERIALIZER)
            .register("compressed_shapeless", CompressedShapelessRecipeSerializer::new);

    static class CompressedShapelessRecipe extends ShapelessRecipe {
        public CompressedShapelessRecipe(String group, CraftingBookCategory category, ItemStack result, List<SizedIngredient> ingredients) {
            super(group, category, result, decompressList(ingredients));
        }

        public CompressedShapelessRecipe(ShapelessRecipe uncompressed) {
            this(uncompressed.getGroup(), uncompressed.category(), uncompressed.getResultItem(null), compressIngredients(uncompressed.getIngredients()));
        }

        private static NonNullList<Ingredient> decompressList(List<SizedIngredient> ingredients) {
            var list = new ArrayList<Ingredient>();
            for (var ingredient : ingredients) {
                for (int i = 0; i < ingredient.count(); ++i) {
                    list.add(ingredient.ingredient());
                }
            }
            return NonNullList.copyOf(list);
        }

        private static List<SizedIngredient> compressIngredients(NonNullList<Ingredient> ingredients) {
            Map<Ingredient, Integer> ingredientsMap = new LinkedHashMap<>();
            for (var ingredient : ingredients) {
                ingredientsMap.merge(ingredient, 1, Integer::sum);
            }
            return ingredientsMap.entrySet().stream()
                    .map(entry -> new SizedIngredient(entry.getKey(), entry.getValue()))
                    .toList();
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return COMPRESSED_SHAPELESS_SERIALIZER.get();
        }
    }

    static class CompressedShapelessRecipeSerializer implements RecipeSerializer<CompressedShapelessRecipe> {
        private static final MapCodec<CompressedShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(
                p_337958_ -> p_337958_.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessRecipe::getGroup),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(p_301133_ -> p_301133_.category()),
                        ItemStack.CODEC.fieldOf("result").forGetter(p_301142_ -> p_301142_.getResultItem(null)),
                        SizedIngredient.FLAT_CODEC
                                .listOf()
                                .fieldOf("ingredients")
                                .flatXmap(
                                        ingredients -> {
                                            if (ingredients.isEmpty()) {
                                                return DataResult.error(() -> "No ingredients for shapeless recipe");
                                            } else {
                                                return ingredients.size() > ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth()
                                                        ? DataResult.error(() -> "Too many ingredients for shapeless recipe. The maximum is: %s".formatted(ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth()))
                                                        : DataResult.success(ingredients);
                                            }
                                        },
                                        DataResult::success)
                                .forGetter(r -> CompressedShapelessRecipe.compressIngredients(r.getIngredients())))
                        .apply(p_337958_, CompressedShapelessRecipe::new));

        @Override
        public MapCodec<CompressedShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CompressedShapelessRecipe> streamCodec() {
            // very ugly, don't look too much at this
            return (StreamCodec) ShapelessRecipe.Serializer.STREAM_CODEC;
        }
    }

    static class CompressedShapelessRecipeBuilder extends ShapelessRecipeBuilder {
        public CompressedShapelessRecipeBuilder(RecipeCategory category, ItemStack result) {
            super(category, result);
        }

        @Override
        public void save(RecipeOutput recipeOutput, ResourceLocation location) {
            super.save(new RecipeOutput() {
                @Override
                public Advancement.Builder advancement() {
                    return recipeOutput.advancement();
                }

                @Override
                public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
                    recipeOutput.accept(id, new CompressedShapelessRecipe((ShapelessRecipe) recipe), advancement, conditions);
                }
            }, location);
        }

        public static ShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result) {
            return new CompressedShapelessRecipeBuilder(category, new ItemStack(result, 1));
        }
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if sized ingredients serialize and deserialize correctly")
    static void testSizedIngredient(final DynamicTest test, final RegistrationHelper reg) {
        reg.addProvider(event -> new RecipeProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void buildRecipes(RecipeOutput output) {
                CompressedShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.CHERRY_FENCE)
                        .requires(new TestEnabledIngredient(
                                Ingredient.of(Items.DIAMOND_PICKAXE),
                                test.framework(), test.id()).toVanilla(), 2)
                        .requires(Ingredient.of(Items.COAL, Items.CHARCOAL), 2)
                        .unlockedBy("has_pick", has(Items.DIAMOND_PICKAXE))
                        .save(output, new ResourceLocation(reg.modId(), "sized_ingredient_1"));
            }
        });

        test.onGameTest(helper -> helper
                .startSequence()
                .thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.CRAFTER.defaultBlockState().setValue(BlockStateProperties.ORIENTATION, FrontAndTop.UP_NORTH).setValue(CrafterBlock.CRAFTING, true)))
                .thenExecute(() -> helper.setBlock(1, 2, 1, Blocks.CHEST))

                .thenMap(() -> helper.requireBlockEntity(1, 1, 1, CrafterBlockEntity.class))
                .thenExecute(crafter -> crafter.setItem(1, Items.DIAMOND_PICKAXE.getDefaultInstance()))
                .thenExecute(crafter -> crafter.setItem(2, Items.DIAMOND_PICKAXE.getDefaultInstance()))
                .thenExecute(crafter -> crafter.setItem(0, Items.COAL.getDefaultInstance()))
                .thenIdle(3)

                // Missing an item, the recipe shouldn't work
                .thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
                .thenExecuteAfter(7, () -> helper.assertContainerEmpty(1, 2, 1))

                .thenIdle(5) // Crafter cooldown

                // Add the missing item, the recipe should work
                .thenExecute(crafter -> crafter.setItem(3, Items.CHARCOAL.getDefaultInstance()))
                .thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
                .thenExecuteAfter(7, () -> helper.assertContainerContains(1, 2, 1, Items.CHERRY_FENCE))

                .thenSucceed());
    }
/*
@GameTest
@EmptyTemplate
@TestHolder(description = "Tests if partial NBT ingredients match the correct stacks using partial attachments")
static void partialAttachmentIngredient(final DynamicTest test, final RegistrationHelper reg) {
var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
.register("test_int", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());
var altAttachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
.register("test_bool", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());

reg.addProvider(event -> new RecipeProvider(event.getGenerator().getPackOutput()) {
@Override
protected void buildRecipes(RecipeOutput output) {
ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.POPPY)
.pattern("IDE")
.define('I', new TestEnabledIngredient(
DataComponentIngredient.ofAttachment(false, putInt(new CompoundTag(), attachmentType.getId().toString(), 1), Items.IRON_PICKAXE),
test.framework(), test.id()))
.define('D', Items.DIAMOND)
.define('E', Items.EMERALD)
.unlockedBy("has_pick", has(Items.IRON_PICKAXE))
.save(output, new ResourceLocation(reg.modId(), "partial_attachments"));
}
});

test.onGameTest(helper -> helper
.startSequence()
.thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.CRAFTER.defaultBlockState().setValue(BlockStateProperties.ORIENTATION, FrontAndTop.UP_NORTH).setValue(CrafterBlock.CRAFTING, true)))
.thenExecute(() -> helper.setBlock(1, 2, 1, Blocks.CHEST))

.thenMap(() -> helper.requireBlockEntity(1, 1, 1, CrafterBlockEntity.class))
.thenExecute(crafter -> crafter.setItem(0, Items.IRON_PICKAXE.getDefaultInstance()))
.thenExecute(crafter -> crafter.setItem(1, Items.DIAMOND.getDefaultInstance()))
.thenExecute(crafter -> crafter.setItem(2, Items.EMERALD.getDefaultInstance()))
.thenIdle(3)

// Pick has no attachments, the recipe shouldn't work
.thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
.thenExecuteAfter(7, () -> helper.assertContainerEmpty(1, 2, 1))

.thenIdle(5) // Crafter cooldown

// Pick now has the attachment, we expect the recipe to work, even if we also add other random attachments and nbt
.thenExecute(crafter -> crafter.getItem(0).setData(attachmentType, 1))
.thenExecute(crafter -> crafter.getItem(0).setData(altAttachmentType, true))
.thenExecute(crafter -> crafter.getItem(0).getTag().putFloat("abcd", helper.getLevel().random.nextFloat()))

.thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
.thenExecuteAfter(7, () -> helper.assertContainerContains(1, 2, 1, Items.POPPY))

.thenSucceed());
}

@GameTest
@EmptyTemplate
@TestHolder(description = "Tests if strict NBT ingredients match the correct stacks using attachments")
static void strictAttachmentIngredient(final DynamicTest test, final RegistrationHelper reg) {
var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
.register("test_int", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());
var altAttachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
.register("test_bool", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());

reg.addProvider(event -> new RecipeProvider(event.getGenerator().getPackOutput()) {
@Override
protected void buildRecipes(RecipeOutput output) {
ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.ACACIA_CHEST_BOAT)
.requires(new TestEnabledIngredient(
DataComponentIngredient.ofAttachment(true, putInt(new CompoundTag(), attachmentType.getId().toString(), 4), Items.APPLE),
test.framework(), test.id()))
.requires(Items.ACACIA_PLANKS)
.unlockedBy("has_apple", has(Items.APPLE))
.save(output, new ResourceLocation(reg.modId(), "strict_attachments"));
}
});

test.onGameTest(helper -> helper
.startSequence()
.thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.CRAFTER.defaultBlockState().setValue(BlockStateProperties.ORIENTATION, FrontAndTop.UP_NORTH).setValue(CrafterBlock.CRAFTING, true)))
.thenExecute(() -> helper.setBlock(1, 2, 1, Blocks.CHEST))

.thenMap(() -> helper.requireBlockEntity(1, 1, 1, CrafterBlockEntity.class))
.thenExecute(crafter -> crafter.setItem(1, new ItemStack(Items.APPLE, 1, putInt(new CompoundTag(), attachmentType.getId().toString(), 3))))
.thenExecute(crafter -> crafter.setItem(0, Items.ACACIA_PLANKS.getDefaultInstance()))
.thenIdle(3)

// Axe has the attachment but at the wrong value, so, the recipe shouldn't work
.thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
.thenExecuteAfter(7, () -> helper.assertContainerEmpty(1, 2, 1))

.thenIdle(5) // Crafter cooldown

// Axe now has the attachment at the correct value of 4, but has other superfluous attachments, so the recipe should still not work
.thenExecute(crafter -> crafter.getItem(1).setData(attachmentType, 4))
.thenExecute(crafter -> crafter.getItem(1).setData(altAttachmentType, true))

.thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
.thenExecuteAfter(7, () -> helper.assertContainerEmpty(1, 2, 1))

.thenIdle(5) // Crafter cooldown

// Axe now has the attachment at the correct value of 4, without superfluous attachments, but has nbt, so the recipe should still not work
.thenExecute(crafter -> crafter.getItem(1).removeData(altAttachmentType))
.thenExecute(crafter -> crafter.getItem(1).getOrCreateTag().putFloat("abcd", 12f))

.thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
.thenExecuteAfter(7, () -> helper.assertContainerEmpty(1, 2, 1))

.thenIdle(5) // Crafter cooldown

// The superfluous element is gone, so the recipe should now work
.thenExecute(crafter -> crafter.getItem(1).removeTagKey("abcd"))
.thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
.thenExecuteAfter(7, () -> helper.assertContainerContains(1, 2, 1, Items.ACACIA_CHEST_BOAT))

.thenSucceed());
}

@GameTest
@EmptyTemplate
@TestHolder(description = "Tests if partial NBT ingredients match the correct stacks using partial attachments and nbt")
static void partialNbtAttachmentIngredient(final DynamicTest test, final RegistrationHelper reg) {
var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
.register("test_int", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());
var altAttachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
.register("test_bool", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());

reg.addProvider(event -> new RecipeProvider(event.getGenerator().getPackOutput()) {
@Override
protected void buildRecipes(RecipeOutput output) {
ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.AZALEA)
.pattern("IDE")
.define('I', new TestEnabledIngredient(
DataComponentIngredient.of(false, putInt(new CompoundTag(), ItemStack.TAG_DAMAGE, 2), putInt(new CompoundTag(), attachmentType.getId().toString(), 1), Items.NETHERITE_PICKAXE),
test.framework(), test.id()))
.define('D', Items.DIAMOND)
.define('E', Items.EMERALD)
.unlockedBy("has_pick", has(Items.NETHERITE_PICKAXE))
.save(output, new ResourceLocation(reg.modId(), "partial_nbt_and_attachments"));
}
});

test.onGameTest(helper -> helper
.startSequence()
.thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.CRAFTER.defaultBlockState().setValue(BlockStateProperties.ORIENTATION, FrontAndTop.UP_NORTH).setValue(CrafterBlock.CRAFTING, true)))
.thenExecute(() -> helper.setBlock(1, 2, 1, Blocks.CHEST))

.thenMap(() -> helper.requireBlockEntity(1, 1, 1, CrafterBlockEntity.class))
.thenExecute(crafter -> crafter.setItem(0, Items.NETHERITE_PICKAXE.getDefaultInstance()))
.thenExecute(crafter -> crafter.setItem(1, Items.DIAMOND.getDefaultInstance()))
.thenExecute(crafter -> crafter.setItem(2, Items.EMERALD.getDefaultInstance()))
.thenIdle(3)

// Pick has no attachments, the recipe shouldn't work
.thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
.thenExecuteAfter(7, () -> helper.assertContainerEmpty(1, 2, 1))

.thenIdle(5) // Crafter cooldown

// Pick now has the correct damage and attachment, we expect the recipe to work, even if we also add other random attachments and nbt
.thenExecute(crafter -> crafter.getItem(0).hurt(2, helper.getLevel().random, null))
.thenExecute(crafter -> crafter.getItem(0).setData(attachmentType, 1))
.thenExecute(crafter -> crafter.getItem(0).setData(altAttachmentType, true))
.thenExecute(crafter -> crafter.getItem(0).getTag().putFloat("abcd", helper.getLevel().random.nextFloat()))

.thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
.thenExecuteAfter(7, () -> helper.assertContainerContains(1, 2, 1, Items.AZALEA))

.thenSucceed());
}

@GameTest
@EmptyTemplate
@TestHolder(description = "Tests if strict NBT ingredients match the correct stacks using attachments and nbt")
static void strictNbtAttachmentIngredient(final DynamicTest test, final RegistrationHelper reg) {
var attachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
.register("test_int", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());
var altAttachmentType = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
.register("test_bool", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());

reg.addProvider(event -> new RecipeProvider(event.getGenerator().getPackOutput()) {
@Override
protected void buildRecipes(RecipeOutput output) {
ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.ACACIA_FENCE)
.requires(new TestEnabledIngredient(
DataComponentIngredient.of(true, putInt(new CompoundTag(), ItemStack.TAG_DAMAGE, 4), putInt(new CompoundTag(), attachmentType.getId().toString(), 4), Items.DIAMOND_AXE),
test.framework(), test.id()))
.requires(Items.ACACIA_PLANKS)
.unlockedBy("has_axe", has(Items.DIAMOND_AXE))
.save(output, new ResourceLocation(reg.modId(), "strict_attachments"));
}
});

test.onGameTest(helper -> helper
.startSequence()
.thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.CRAFTER.defaultBlockState().setValue(BlockStateProperties.ORIENTATION, FrontAndTop.UP_NORTH).setValue(CrafterBlock.CRAFTING, true)))
.thenExecute(() -> helper.setBlock(1, 2, 1, Blocks.CHEST))

.thenMap(() -> helper.requireBlockEntity(1, 1, 1, CrafterBlockEntity.class))
.thenExecute(crafter -> crafter.setItem(1, new ItemStack(Items.DIAMOND_AXE, 1, putInt(new CompoundTag(), attachmentType.getId().toString(), 3))))
.thenExecute(crafter -> crafter.getItem(1).setTag(putInt(new CompoundTag(), ItemStack.TAG_DAMAGE, 1)))
.thenExecute(crafter -> crafter.setItem(0, Items.ACACIA_PLANKS.getDefaultInstance()))
.thenIdle(3)

// Axe is damaged and has the attachment but at the wrong value, so, the recipe shouldn't work
.thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
.thenExecuteAfter(7, () -> helper.assertContainerEmpty(1, 2, 1))

.thenIdle(5) // Crafter cooldown

// Axe now has the attachment and damage at the correct value of 4, but has other superfluous attachments, so the recipe should still not work
.thenExecute(crafter -> crafter.getItem(1).hurt(3, helper.getLevel().random, null))
.thenExecute(crafter -> crafter.getItem(1).setData(attachmentType, 4))
.thenExecute(crafter -> crafter.getItem(1).setData(altAttachmentType, true))

.thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
.thenExecuteAfter(7, () -> helper.assertContainerEmpty(1, 2, 1))

.thenIdle(5) // Crafter cooldown

// Axe now has the attachment at the correct value of 4, without superfluous attachments, but has superfluous nbt, so the recipe should still not work
.thenExecute(crafter -> crafter.getItem(1).removeData(altAttachmentType))
.thenExecute(crafter -> crafter.getItem(1).getOrCreateTag().putFloat("abcd", 12f))

.thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
.thenExecuteAfter(7, () -> helper.assertContainerEmpty(1, 2, 1))

.thenIdle(5) // Crafter cooldown

// The superfluous element is gone, so the recipe should now work
.thenExecute(crafter -> crafter.getItem(1).removeTagKey("abcd"))
.thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
.thenExecuteAfter(7, () -> helper.assertContainerContains(1, 2, 1, Items.ACACIA_FENCE))

.thenSucceed());
}

private static CompoundTag putInt(CompoundTag tag, String key, int value) {
tag.putInt(key, value);
return tag;
}*/
}
