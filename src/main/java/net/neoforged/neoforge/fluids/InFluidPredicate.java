package net.neoforged.neoforge.fluids;

import net.minecraft.world.entity.Entity;

@FunctionalInterface
public interface InFluidPredicate<E extends Entity> {
    boolean test(E entity, FluidType fluidType, double height);
}
