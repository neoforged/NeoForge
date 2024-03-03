package net.neoforged.neoforge.debug.mixin;

import net.neoforged.neoforge.debug.MixinTests;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MixinTests.Target.class)
public class TestMixin {
    @Redirect(method = "crash", at = @At(value = "NEW", target = "(Ljava/lang/String;)Ljava/lang/RuntimeException;"))
    private static RuntimeException redirectCrash(String message) {
        return new RuntimeException(message);
    }

    @Inject(at = @At("HEAD"), method = "applied", cancellable = true)
    private static void apply(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
