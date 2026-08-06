package org.bukkit.inventory;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a recipe which will take an {@code input} surrounded by 8
 * {@code material}, with the {@code source} potion contents copied into the
 * {@code result}.
 */
@ApiStatus.Experimental
public class ImbueRecipe extends CraftingRecipe implements ComplexRecipe {

    private final RecipeChoice input;
    private final RecipeChoice material;

    /**
     * Create an imbue recipe to produce a result of the specified type.
     *
     * @param key the unique recipe key
     * @param result the imbued result item
     * @param input the input ingredient
     * @param material the additional ingredient material
     */
    public ImbueRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, @NotNull RecipeChoice input, @NotNull RecipeChoice material) {
        super(key, checkResult(result));
        this.input = input;
        this.material = material;
    }

    /**
     * Create an imbue recipe to produce a result of the specified type.
     *
     * @param key the unique recipe key
     * @param result the imbued result material
     * @param input the input ingredient
     * @param material the additional ingredient material
     */
    public ImbueRecipe(@NotNull NamespacedKey key, @NotNull Material result, @NotNull RecipeChoice input, @NotNull RecipeChoice material) {
        this(key, new ItemStack(result), input, material);
    }

    /**
     * Gets the input material, which will be imbued.
     *
     * @return the input
     */
    @NotNull
    public RecipeChoice getInput() {
        return input.clone();
    }

    /**
     * Gets the additional ingredients required to cause the imbuing.
     *
     * @return the imbuing material
     */
    @NotNull
    public RecipeChoice getMaterial() {
        return material.clone();
    }
}
