package net.feltmc.neoforge.patches.mixin;

import net.feltmc.neoforge.patches.interfaces.ResourceLocationInterface;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ResourceLocation.class)
public class ResourceLocationMixin implements ResourceLocationInterface {
    public int compareNamespaced(ResourceLocation o) {
        int ret = ((ResourceLocation) (Object) this).getNamespace().compareTo(o.getNamespace());
        return ret != 0 ? ret : ((ResourceLocation) (Object) this).getPath().compareTo(o.getPath());
    }
}
