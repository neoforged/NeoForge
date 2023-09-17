package net.feltmc.neoforge.patches.interfaces;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.List;

public interface RegistrySetBuilderInterface {
    default List<? extends ResourceKey<? extends Registry<?>>> getEntryKeys() {
        return null;
    }
}
