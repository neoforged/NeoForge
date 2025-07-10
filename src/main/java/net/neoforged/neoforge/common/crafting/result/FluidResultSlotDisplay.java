package net.neoforged.neoforge.common.crafting.result;

import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.display.ForFluidStacks;

import java.util.stream.Stream;

/**
 * Superinterface for {@link Result}s of type {@link FluidStack}.
 * Automatically resolves the display for {@link ForFluidStacks}.
 */
public interface FluidResultSlotDisplay extends ResultSlotDisplay<FluidStack> {
    @Override
    default <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        return factory instanceof ForFluidStacks<T> forStacks ? Stream.of(forStacks.forStack(result().resolve())) : Stream.empty();
    }
}
