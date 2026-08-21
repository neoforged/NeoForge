package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;

public interface BootstrapContextAccessExtension {
    default <S> Optional<HolderLookup<S>> holderLookup(ResourceKey<? extends Registry<? extends S>> registry) {
        return Optional.empty();
    }
}
