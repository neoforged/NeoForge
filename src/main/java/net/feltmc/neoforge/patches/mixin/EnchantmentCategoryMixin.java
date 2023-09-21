package net.feltmc.neoforge.patches.mixin;

import fr.catcore.cursedmixinextensions.annotations.NewConstructor;
import fr.catcore.cursedmixinextensions.annotations.Public;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.IExtensibleEnum;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Predicate;

@Mixin(EnchantmentCategory.class)
public class EnchantmentCategoryMixin implements IExtensibleEnum {
    private Predicate<Item> delegate;

    @NewConstructor
    private void ctr() {}

    @NewConstructor
    private void ctr(Predicate<Item> delegate) {
        this.delegate = delegate;
    }

    @Public
    private static EnchantmentCategory create(String name, java.util.function.Predicate<Item> delegate) {
        throw new IllegalStateException("Enum not extended");
    }

    public boolean canEnchant(Item stack) {
        return this.delegate != null && this.delegate.test(stack);
    }
}
