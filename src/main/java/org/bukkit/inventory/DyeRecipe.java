package org.bukkit.inventory;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a recipe which will mix the dye color from the {@code input} with
 * the dye color from the dye {@code material} into the {@code result} but with
 * the mixed dye color.
 */
@ApiStatus.Experimental
public class DyeRecipe extends CraftingRecipe implements ComplexRecipe {

    private final RecipeChoice input;
    private final RecipeChoice material;

    /**
     * Create a dye recipe to produce a result of the specified type.
     *
     * @param key the unique recipe key
     * @param result the dyed result item
     * @param input the input ingredient
     * @param material the additional dye ingredient
     */
    public DyeRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, @NotNull RecipeChoice input, @NotNull RecipeChoice material) {
        super(key, checkResult(result));
        this.input = input;
        this.material = material;
    }

    /**
     * Create a dye recipe to produce a result of the specified type.
     *
     * @param key the unique recipe key
     * @param result the dyed result material
     * @param input the input ingredient
     * @param material the additional dye ingredient
     */
    public DyeRecipe(@NotNull NamespacedKey key, @NotNull Material result, @NotNull RecipeChoice input, @NotNull RecipeChoice material) {
        this(key, new ItemStack(result), input, material);
    }

    /**
     * Gets the input material, which will be dyed.
     *
     * @return the input
     */
    @NotNull
    public RecipeChoice getInput() {
        return input.clone();
    }

    /**
     * Gets the additional dye material required to cause the dyeing.
     *
     * @return the dye material
     */
    @NotNull
    public RecipeChoice getMaterial() {
        return material.clone();
    }
}
