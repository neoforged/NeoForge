package net.feltmc.neoforge.patches.interfaces;

import net.minecraft.world.item.ItemStack;

public interface EnchantmentInterface {
    boolean canApplyAtEnchantingTable(ItemStack stack);
    boolean isAllowedOnBooks();
}
