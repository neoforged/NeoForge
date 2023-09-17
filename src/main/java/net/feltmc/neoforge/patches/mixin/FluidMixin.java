package net.feltmc.neoforge.patches.mixin;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.extensions.IForgeFluid;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Fluid.class)
public class FluidMixin implements IForgeFluid {
    private FluidType forgeFluidType;
    @Override
    public FluidType getFluidType() {
        if (forgeFluidType == null) forgeFluidType = ForgeHooks.getVanillaFluidType(((Fluid) (Object) this));
        return forgeFluidType;
    }
}
