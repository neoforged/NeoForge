/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
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
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.crafting.BlockTagIngredient;
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
import net.neoforged.testframework.registration.RegistrationHelper;
import org.jetbrains.annotations.Nullable;

@ForEachTest(groups = "crafting.ingredient")
public class IngredientTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if BlockTagIngredient works")
    static void blockTagIngredient(final DynamicTest test, final RegistrationHelper reg) {
        reg.addClientProvider(event -> new RecipeProvider.Runner(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
                return new RecipeProvider(registries, output) {
                    @Override
                    protected void buildRecipes() {
                        this.shapeless(RecipeCategory.MISC, Items.MUD)
                                .requires(new TestEnabledIngredient(new BlockTagIngredient(BlockTags.CONVERTABLE_TO_MUD).toVanilla(), test.framework(), test.id()).toVanilla())
                                .requires(Items.WATER_BUCKET)
                                .unlockedBy("has_item", has(Items.WATER_BUCKET))
                                .save(output, ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath(reg.modId(), "block_tag")));
                    }
                };
            }

            @Override
            public String getName() {
                return "blockTagIngredient Recipes";
            }
        });

        test.onGameTest(helper -> helper
                .startSequence()
                .thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.CRAFTER.defaultBlockState().setValue(BlockStateProperties.ORIENTATION, FrontAndTop.UP_NORTH).setValue(CrafterBlock.CRAFTING, true)))
                .thenExecute(() -> helper.setBlock(1, 2, 1, Blocks.CHEST))

                .thenMap(() -> helper.requireBlockEntity(1, 1, 1, CrafterBlockEntity.class))
                .thenExecute(crafter -> crafter.setItem(0, Items.DIRT.getDefaultInstance()))
                .thenExecute(crafter -> crafter.setItem(1, Items.WATER_BUCKET.getDefaultInstance()))
                .thenIdle(3)

                .thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
                .thenExecuteAfter(7, () -> helper.assertContainerContains(1, 2, 1, Items.MUD))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if partial NBT ingredients match the correct stacks")
    static void partialNBTIngredient(final DynamicTest test, final RegistrationHelper reg) {
        reg.addClientProvider(event -> new RecipeProvider.Runner(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
                return new RecipeProvider(registries, output) {
                    @Override
                    protected void buildRecipes() {
                        this.shaped(RecipeCategory.MISC, Items.ALLIUM)
                                .pattern("IDE")
                                .define('I', new TestEnabledIngredient(
                                        DataComponentIngredient.of(false, DataComponents.DAMAGE, 2, Items.IRON_AXE),
                                        test.framework(), test.id()).toVanilla())
                                .define('D', Items.DIAMOND)
                                .define('E', Items.EMERALD)
                                .unlockedBy("has_axe", has(Items.IRON_AXE))
                                .save(output, ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath(reg.modId(), "partial_nbt")));
                    }
                };
            }

            @Override
            public String getName() {
                return "partialNBTIngredient Recipes";
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
                .thenExecute(crafter -> crafter.getItem(0).hurtAndBreak(2, helper.getLevel(), null, item -> {}))
                .thenExecute(crafter -> CustomData.update(DataComponents.CUSTOM_DATA, crafter.getItem(0), tag -> tag.putFloat("abcd", helper.getLevel().random.nextFloat())))

                .thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
                .thenExecuteAfter(7, () -> helper.assertContainerContains(1, 2, 1, Items.ALLIUM))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if strict NBT ingredients match the correct stacks")
    static void strictNBTIngredient(final DynamicTest test, final RegistrationHelper reg) {
        reg.addClientProvider(event -> new RecipeProvider.Runner(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
                return new RecipeProvider(registries, output) {
                    @Override
                    protected void buildRecipes() {
                        this.shapeless(RecipeCategory.MISC, Items.ACACIA_BOAT)
                                .requires(new TestEnabledIngredient(
                                        DataComponentIngredient.of(true, DataComponents.DAMAGE, 4, Items.DIAMOND_PICKAXE),
                                        test.framework(), test.id()).toVanilla())
                                .requires(Items.ACACIA_PLANKS)
                                .unlockedBy("has_pick", has(Items.DIAMOND_PICKAXE))
                                .save(output, ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath(reg.modId(), "strict_nbt")));
                    }
                };
            }

            @Override
            public String getName() {
                return "strictNBTIngredient Recipes";
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
                .thenExecute(crafter -> crafter.getItem(1).hurtAndBreak(3, helper.getLevel(), null, item -> {}))
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

    private static final RegistrationHelper REG_HELPER = RegistrationHelper.create("neotests_ingredients");

    @OnInit
    static void register(final TestFramework framework) {
        REG_HELPER.register(framework.modEventBus(), framework.container());
    }

    private static final DeferredHolder<RecipeSerializer<?>, CompressedShapelessRecipeSerializer> COMPRESSED_SHAPELESS_SERIALIZER = REG_HELPER
            .registrar(Registries.RECIPE_SERIALIZER)
            .register("compressed_shapeless", CompressedShapelessRecipeSerializer::new);

    private static List<Ingredient> shapelessRecipeIngredients(ShapelessRecipe recipe) {
        return ObfuscationReflectionHelper.getPrivateValue(ShapelessRecipe.class, recipe, "ingredients");
    }

    static class CompressedShapelessRecipe extends ShapelessRecipe {
        public CompressedShapelessRecipe(String group, CraftingBookCategory category, ItemStack result, List<SizedIngredient> ingredients) {
            super(group, category, result, decompressList(ingredients));
        }

        public CompressedShapelessRecipe(ShapelessRecipe uncompressed) {
            this(uncompressed.group(), uncompressed.category(), uncompressed.assemble(null, null), compressIngredients(shapelessRecipeIngredients(uncompressed)));
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

        private static List<SizedIngredient> compressIngredients(List<Ingredient> ingredients) {
            Map<Ingredient, Integer> ingredientsMap = new LinkedHashMap<>();
            for (var ingredient : ingredients) {
                ingredientsMap.merge(ingredient, 1, Integer::sum);
            }
            return ingredientsMap.entrySet().stream()
                    .map(entry -> new SizedIngredient(entry.getKey(), entry.getValue()))
                    .toList();
        }

        @Override
        public RecipeSerializer<ShapelessRecipe> getSerializer() {
            return (RecipeSerializer) COMPRESSED_SHAPELESS_SERIALIZER.get();
        }
    }

    static class CompressedShapelessRecipeSerializer implements RecipeSerializer<CompressedShapelessRecipe> {
        private static final MapCodec<CompressedShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(
                p_337958_ -> p_337958_.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessRecipe::group),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(p_301133_ -> p_301133_.category()),
                        ItemStack.CODEC.fieldOf("result").forGetter(p_301142_ -> p_301142_.assemble(null, null)),
                        SizedIngredient.NESTED_CODEC
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
                                .forGetter(r -> CompressedShapelessRecipe.compressIngredients(shapelessRecipeIngredients(r))))
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

    static class CompressedShapelessRecipeBuilder implements RecipeBuilder {
        private final RecipeCategory category;
        private final ItemStack result;
        private final List<Ingredient> ingredients = new ArrayList<>();
        private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
        @org.jetbrains.annotations.Nullable
        private String group;

        private CompressedShapelessRecipeBuilder(RecipeCategory p_250837_, ItemStack p_363612_) {
            this.category = p_250837_;
            this.result = p_363612_;
        }

        public static CompressedShapelessRecipeBuilder compressedShapeless(RecipeCategory p_250714_, ItemLike p_249659_) {
            return compressedShapeless(p_250714_, p_249659_, 1);
        }

        public static CompressedShapelessRecipeBuilder compressedShapeless(RecipeCategory p_252339_, ItemLike p_250836_, int p_249928_) {
            return new CompressedShapelessRecipeBuilder(p_252339_, p_250836_.asItem().getDefaultInstance().copyWithCount(p_249928_));
        }

        public CompressedShapelessRecipeBuilder requires(Ingredient p_126187_, int p_126188_) {
            for (int i = 0; i < p_126188_; i++) {
                this.ingredients.add(p_126187_);
            }

            return this;
        }

        public CompressedShapelessRecipeBuilder unlockedBy(String p_176781_, Criterion<?> p_300897_) {
            this.criteria.put(p_176781_, p_300897_);
            return this;
        }

        public CompressedShapelessRecipeBuilder group(@Nullable String p_126195_) {
            this.group = p_126195_;
            return this;
        }

        @Override
        public Item getResult() {
            return this.result.getItem();
        }

        @Override
        public void save(RecipeOutput p_301215_, ResourceKey<Recipe<?>> p_379987_) {
            this.ensureValid(p_379987_);
            Advancement.Builder advancement$builder = p_301215_.advancement()
                    .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(p_379987_))
                    .rewards(AdvancementRewards.Builder.recipe(p_379987_))
                    .requirements(AdvancementRequirements.Strategy.OR);
            this.criteria.forEach(advancement$builder::addCriterion);
            ShapelessRecipe shapelessrecipe = new CompressedShapelessRecipe(
                    Objects.requireNonNullElse(this.group, ""), RecipeBuilder.determineBookCategory(this.category), this.result, CompressedShapelessRecipe.compressIngredients(this.ingredients));
            p_301215_.accept(
                    p_379987_, shapelessrecipe, advancement$builder.build(p_379987_.location().withPrefix("recipes/" + this.category.getFolderName() + "/")));
        }

        private void ensureValid(ResourceKey<Recipe<?>> p_379745_) {
            if (this.criteria.isEmpty()) {
                throw new IllegalStateException("No way of obtaining recipe " + p_379745_.location());
            }
        }
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if sized ingredients serialize and deserialize correctly")
    static void testSizedIngredient(final DynamicTest test, final RegistrationHelper reg) {
        reg.addClientProvider(event -> new RecipeProvider.Runner(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
                return new RecipeProvider(registries, output) {
                    @Override
                    protected void buildRecipes() {
                        CompressedShapelessRecipeBuilder.compressedShapeless(RecipeCategory.MISC, Items.CHERRY_FENCE)
                                .requires(new TestEnabledIngredient(
                                        Ingredient.of(Items.DIAMOND_PICKAXE),
                                        test.framework(), test.id()).toVanilla(), 2)
                                .requires(Ingredient.of(Items.COAL, Items.CHARCOAL), 2)
                                .unlockedBy("has_pick", has(Items.DIAMOND_PICKAXE))
                                .save(output, ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath(reg.modId(), "sized_ingredient_1")));
                    }
                };
            }

            @Override
            public String getName() {
                return "testSizedIngredient Recipes";
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
}
