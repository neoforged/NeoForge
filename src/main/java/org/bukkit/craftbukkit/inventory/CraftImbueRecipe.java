package org.bukkit.craftbukkit.inventory;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ImbueRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public class CraftImbueRecipe extends ImbueRecipe implements CraftRecipe {

    public CraftImbueRecipe(NamespacedKey key, ItemStack result, RecipeChoice input, RecipeChoice material) {
        super(key, result, input, material);
    }

    public static CraftImbueRecipe fromBukkitRecipe(ImbueRecipe recipe) {
        if (recipe instanceof CraftImbueRecipe) {
            return (CraftImbueRecipe) recipe;
        }
        CraftImbueRecipe ret = new CraftImbueRecipe(recipe.getKey(), recipe.getResult(), recipe.getInput(), recipe.getMaterial());
        ret.setGroup(recipe.getGroup());
        ret.setCategory(recipe.getCategory());
        return ret;
    }

    @Override
    public void addToCraftingManager() {
        MinecraftServer.getServer().getRecipeManager().addRecipe(
                new RecipeHolder<>(CraftRecipe.toMinecraft(this.getKey()),
                        new net.minecraft.world.item.crafting.ImbueRecipe(getCommon(),
                                CraftRecipe.getBook(this),
                                toNMS(this.getInput(), true),
                                toNMS(this.getMaterial(), true),
                                CraftItemStack.asNMSTemplate(this.getResult())
                        )
                )
        );
    }
}
