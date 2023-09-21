package net.feltmc.neoforge.patches.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.feltmc.neoforge.patches.interfaces.EnchantmentInterface;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.extensions.IForgeEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Enchantment.class)
public class EnchantmentMixin implements IForgeEnchantment, EnchantmentInterface {
    @Redirect(method = "canEnchant", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentCategory;canEnchant(Lnet/minecraft/world/item/Item;)Z"))
    private boolean canEnchantForge(EnchantmentCategory instance, Item item, @Local(ordinal = 0) ItemStack stack) {
        return canApplyAtEnchantingTable(stack);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.canApplyAtEnchantingTable(this);
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }
}
