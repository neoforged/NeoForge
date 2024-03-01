/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.mixin;

import net.neoforged.neoforge.debug.MixinTests;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MixinTests.Target.class)
public class TestTargetMixin {
    @Inject(at = @At("HEAD"), method = "wasMixinApplied", cancellable = true)
    private static void onInvoke(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
