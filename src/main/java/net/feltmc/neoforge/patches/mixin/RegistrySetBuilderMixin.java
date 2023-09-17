package net.feltmc.neoforge.patches.mixin;

import net.feltmc.neoforge.patches.interfaces.RegistrySetBuilderInterface;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(RegistrySetBuilder.class)
public class RegistrySetBuilderMixin implements RegistrySetBuilderInterface {

    public List<? extends ResourceKey<? extends Registry<?>>> getEntryKeys() {
        return ((RegistrySetBuilder) (Object) this).entries.stream().map(RegistrySetBuilder.RegistryStub::key).toList();
    }
}
