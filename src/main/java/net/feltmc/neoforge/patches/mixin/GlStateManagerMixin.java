package net.feltmc.neoforge.patches.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.feltmc.feltasm.asm.CreateStatic;

@Mixin(GlStateManager.class)
public class GlStateManagerMixin {
    //TODO felt-asm inject these
    @CreateStatic
    private static float lastBrightnessX = 0.0f;
    @CreateStatic
    private static float lastBrightnessY = 0.0f;


    @Inject(method = "_texParameter(IIF)V", at = @At("TAIL"), remap = false)
    private static void texParamMixin(CallbackInfo info, @Local(ordinal = 1) int x, @Local float y) {
        lastBrightnessX = x;
        lastBrightnessY = y;
    }
}
