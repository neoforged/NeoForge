package org.bukkit.inventory;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a recipe which will change the type of the input material when
 * combined with an additional material, but preserve all custom data. Only the
 * item type of the result stack will be used.
 * <br>
 * Used for dyeing shulker boxes in Vanilla.
 */
public class TransmuteRecipe extends CraftingRecipe implements ComplexRecipe {

    private final RecipeChoice input;
    private final RecipeChoice material;
    private final int minimumMaterialCount;
    private final int maximumMaterialCount;
    private final boolean addMaterialCountToResult;

    /**
     * Create a transmute recipe to produce a result of the specified type.
     *
     * @param key the unique recipe key
     * @param result the transmuted result item
     * @param input the input ingredient
     * @param material the additional ingredient
     * @param minimumMaterialCount minimum count of the material, default 1, range [1, 8]
     * @param maximumMaterialCount maximum count of the material, default 1, range [1, 8]
     * @param addMaterialCountToResult whether the material count should be added to the result stack count, default false
     */
    public TransmuteRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, @NotNull RecipeChoice input, @NotNull RecipeChoice material, int minimumMaterialCount, int maximumMaterialCount, boolean addMaterialCountToResult) {
        super(key, checkResult(result));
        this.input = input;
        this.material = material;
        this.minimumMaterialCount = minimumMaterialCount;
        this.maximumMaterialCount = maximumMaterialCount;
        this.addMaterialCountToResult = addMaterialCountToResult;
    }

    /**
     * Create a transmute recipe to produce a result of the specified type.
     *
     * @param key the unique recipe key
     * @param result the transmuted result item
     * @param input the input ingredient
     * @param material the additional ingredient
     */
    public TransmuteRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, @NotNull RecipeChoice input, @NotNull RecipeChoice material) {
        this(key, result, input, material, 1, 1, false);
    }

    /**
     * Create a transmute recipe to produce a result of the specified type.
     *
     * @param key the unique recipe key
     * @param result the transmuted result material
     * @param input the input ingredient
     * @param material the additional ingredient
     */
    public TransmuteRecipe(@NotNull NamespacedKey key, @NotNull Material result, @NotNull RecipeChoice input, @NotNull RecipeChoice material) {
        this(key, new ItemStack(result), input, material);
    }

    /**
     * Gets the input material, which will be transmuted.
     *
     * @return the input from transmutation
     */
    @NotNull
    public RecipeChoice getInput() {
        return input.clone();
    }

    /**
     * Gets the additional material required to cause the transmutation.
     *
     * @return the ingredient material
     */
    @NotNull
    public RecipeChoice getMaterial() {
        return material.clone();
    }

    /**
     * Gets the minimum amount of items matched by the material ingredient.
     *
     * @return minimum count of the material, default 1, range [1, 8]
     */
    public int getMinimumMaterialCount() {
        return minimumMaterialCount;
    }

    /**
     * Gets the maximum amount of items matched by the material ingredient.
     *
     * @return maximum count of the material, default 1, range [1, 8]
     */
    public int getMaximumMaterialCount() {
        return maximumMaterialCount;
    }

    /**
     * Gets whether the material ingredient count should be added to the result
     * stack count after crafting.
     *
     * @return whether the material ingredient count should be added to the
     * result stack count, default false
     */
    public boolean isAddMaterialCountToResult() {
        return addMaterialCountToResult;
    }
}
