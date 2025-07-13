package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.outgredient.Outgredient;

public interface IShapelessRecipeBuilderExtension {
    /**
     * Creates a new {@link ShapelessRecipeBuilder} using a {@link TagKey} and {@link ItemLike} combination as the result.
     *
     * @param items    The item holder getter, usually the registry.
     * @param category The {@link RecipeCategory} to use.
     * @param tagKey   The {@link TagKey} to use.
     * @param fallback The fallback {@link ItemLike} to use.
     * @return A new {@link ShapelessRecipeBuilder}.
     */
    static ShapelessRecipeBuilder shapeless(HolderGetter<Item> items, RecipeCategory category, TagKey<Item> tagKey, ItemLike fallback) {
        return shapeless(items, category, tagKey, fallback, 1);
    }

    /**
     * Creates a new {@link ShapelessRecipeBuilder} using a {@link TagKey} and {@link ItemLike} combination as the result.
     *
     * @param items    The item holder getter, usually the registry.
     * @param category The {@link RecipeCategory} to use.
     * @param tagKey   The {@link TagKey} to use.
     * @param fallback The fallback {@link ItemLike} to use.
     * @param count    The result count to use.
     * @return A new {@link ShapelessRecipeBuilder}.
     */
    static ShapelessRecipeBuilder shapeless(HolderGetter<Item> items, RecipeCategory category, TagKey<Item> tagKey, ItemLike fallback, int count) {
        return shapeless(items, category, new net.neoforged.neoforge.common.crafting.outgredient.DefaultedItemTagOutgredient(tagKey, fallback.asItem().builtInRegistryHolder(), count));
    }

    /**
     * Creates a new {@link ShapelessRecipeBuilder} using a generic {@link Outgredient} as the result.
     *
     * @param items    The item holder getter, usually the registry.
     * @param category The {@link RecipeCategory} to use.
     * @param result   The result {@link Outgredient} to use.
     * @return A new {@link ShapelessRecipeBuilder}.
     */
    static ShapelessRecipeBuilder shapeless(HolderGetter<Item> items, RecipeCategory category, net.neoforged.neoforge.common.crafting.outgredient.Outgredient<ItemStack> result) {
        return new ShapelessRecipeBuilder(items, category, result);
    }
}
