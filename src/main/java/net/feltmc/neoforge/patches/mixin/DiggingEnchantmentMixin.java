package net.feltmc.neoforge.patches.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.enchantment.DiggingEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DiggingEnchantment.class)
public class DiggingEnchantmentMixin {
    @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true)
    private void canEnchantForge(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (itemStack.getItem() instanceof ShearsItem) cir.setReturnValue(true);
    }
}
