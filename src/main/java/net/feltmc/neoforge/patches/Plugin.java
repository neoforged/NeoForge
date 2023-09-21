package net.feltmc.neoforge.patches;

import fr.catcore.cursedmixinextensions.CursedMixinExtensions;
import io.github.feltmc.feltasm.platform.fabric.FeltASMMixinPlugin;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class Plugin extends FeltASMMixinPlugin {
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        super.postApply(targetClassName, targetClass, mixinClassName, mixinInfo);
        CursedMixinExtensions.postApply(targetClass);
    }
}
