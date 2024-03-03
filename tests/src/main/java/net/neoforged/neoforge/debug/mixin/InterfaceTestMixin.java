package net.neoforged.neoforge.debug.mixin;

import net.neoforged.neoforge.debug.MixinTests;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MixinTests.InterfaceTarget.class)
public interface InterfaceTestMixin {
    @Inject(at = @At("HEAD"), method = "getNumber", cancellable = true)
    default void numberProvider(String argument, CallbackInfoReturnable<Integer> cir) {
        if (argument.equals(MixinTests.InterfaceTarget.MAXINT)) {
            cir.setReturnValue(Integer.MAX_VALUE);
        }
    }
}
