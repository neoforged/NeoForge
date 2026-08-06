package org.bukkit.craftbukkit.inventory;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.DyeRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public class CraftDyeRecipe extends DyeRecipe implements CraftRecipe {

    public CraftDyeRecipe(NamespacedKey key, ItemStack result, RecipeChoice input, RecipeChoice material) {
        super(key, result, input, material);
    }

    public static CraftDyeRecipe fromBukkitRecipe(DyeRecipe recipe) {
        if (recipe instanceof CraftDyeRecipe) {
            return (CraftDyeRecipe) recipe;
        }
        CraftDyeRecipe ret = new CraftDyeRecipe(recipe.getKey(), recipe.getResult(), recipe.getInput(), recipe.getMaterial());
        ret.setGroup(recipe.getGroup());
        ret.setCategory(recipe.getCategory());
        return ret;
    }

    @Override
    public void addToCraftingManager() {
        MinecraftServer.getServer().getRecipeManager().addRecipe(
                new RecipeHolder<>(CraftRecipe.toMinecraft(this.getKey()),
                        new net.minecraft.world.item.crafting.DyeRecipe(getCommon(),
                                CraftRecipe.getBook(this),
                                toNMS(this.getInput(), true),
                                toNMS(this.getMaterial(), true),
                                CraftItemStack.asNMSTemplate(this.getResult())
                        )
                )
        );
    }
}
