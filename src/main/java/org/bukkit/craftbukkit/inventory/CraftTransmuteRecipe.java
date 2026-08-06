package org.bukkit.craftbukkit.inventory;

import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.TransmuteRecipe;

public class CraftTransmuteRecipe extends TransmuteRecipe implements CraftRecipe {

    public CraftTransmuteRecipe(NamespacedKey key, ItemStack result, RecipeChoice input, RecipeChoice material, int minimumMaterialCount, int maximumMaterialCount, boolean addMaterialCountToResult) {
        super(key, result, input, material, minimumMaterialCount, maximumMaterialCount, addMaterialCountToResult);
    }

    public static CraftTransmuteRecipe fromBukkitRecipe(TransmuteRecipe recipe) {
        if (recipe instanceof CraftTransmuteRecipe) {
            return (CraftTransmuteRecipe) recipe;
        }
        CraftTransmuteRecipe ret = new CraftTransmuteRecipe(recipe.getKey(), recipe.getResult(), recipe.getInput(), recipe.getMaterial(), recipe.getMinimumMaterialCount(), recipe.getMaximumMaterialCount(), recipe.isAddMaterialCountToResult());
        ret.setGroup(recipe.getGroup());
        ret.setCategory(recipe.getCategory());
        return ret;
    }

    @Override
    public void addToCraftingManager() {
        MinecraftServer.getServer().getRecipeManager().addRecipe(
                new RecipeHolder<>(CraftRecipe.toMinecraft(this.getKey()),
                        new net.minecraft.world.item.crafting.TransmuteRecipe(getCommon(),
                                CraftRecipe.getBook(this),
                                toNMS(this.getInput(), true),
                                toNMS(this.getMaterial(), true),
                                MinMaxBounds.Ints.between(this.getMinimumMaterialCount(), this.getMaximumMaterialCount()),
                                CraftItemStack.asNMSTemplate(this.getResult()),
                                this.isAddMaterialCountToResult()
                        )
                )
        );
    }
}
