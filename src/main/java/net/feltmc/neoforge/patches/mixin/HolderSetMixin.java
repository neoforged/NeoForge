package net.feltmc.neoforge.patches.mixin;

import net.minecraft.core.HolderSet;
import net.minecraftforge.common.extensions.IForgeHolderSet;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HolderSet.class)
public class HolderSetMixin<T> implements IForgeHolderSet<T> {
}
