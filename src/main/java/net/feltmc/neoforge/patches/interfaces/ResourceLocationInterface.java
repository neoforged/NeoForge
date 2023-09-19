package net.feltmc.neoforge.patches.interfaces;

import net.feltmc.neoforge.FeltVars;
import net.minecraft.resources.ResourceLocation;

public interface ResourceLocationInterface {
    default int compareNamespaced(ResourceLocation o) {
      throw new RuntimeException(FeltVars.mixinOverrideException);
    }
}
