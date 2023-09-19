package net.feltmc.neoforge.patches.interfaces;

import net.feltmc.neoforge.FeltVars;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.List;

public interface RegistrySetBuilderInterface {
    default List<? extends ResourceKey<? extends Registry<?>>> getEntryKeys() {
        throw new RuntimeException(FeltVars.mixinOverrideException);
    }
}
