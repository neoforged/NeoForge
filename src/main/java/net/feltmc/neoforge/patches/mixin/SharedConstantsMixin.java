package net.feltmc.neoforge.patches.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.netty.util.ResourceLeakDetector;
import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SharedConstants.class)
public class SharedConstantsMixin {
    @WrapWithCondition(method = "<clinit>", at = @At(value = "INVOKE",
            target = "Lio/netty/util/ResourceLeakDetector;setLevel(Lio/netty/util/ResourceLeakDetector$Level;)V", remap = false))
    private static boolean dontSetBaseLeakDetectorLevel(ResourceLeakDetector.Level level) {
        return System.getProperty("io.netty.leakDetection.level") == null;
    }
}
