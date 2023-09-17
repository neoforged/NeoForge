package net.feltmc.neoforge.patches.mixin;

import io.github.feltmc.feltasm.asm.CreateStatic;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(VanillaRegistries.class)
public class VanillaRegistriesMixin {
    @Shadow @Final
    private static RegistrySetBuilder BUILDER;

    
    @CreateStatic
    private static final List<? extends ResourceKey<? extends Registry<?>>> DATAPACK_REGISTRY_KEYS = BUILDER.getEntryKeys();
}
