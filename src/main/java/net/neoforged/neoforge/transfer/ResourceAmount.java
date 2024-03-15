package net.neoforged.neoforge.transfer;

import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Represents an immutable resource and an amount.
 * Can be seen as an immutable version of {@link ItemStack} or {@link FluidStack}.
 *
 * @param <T> the held resource type
 */
public record ResourceAmount<T extends IResource>(T resource, int amount) {
    // TODO: currently very awkward to use without codecs and stream codecs
    // TODO: also very painful to convert to/from ItemStack/FluidStack
    public ResourceAmount {
        Objects.requireNonNull(resource, "resource");
    }

    /**
     * Checks if this is empty, meaning that the amount is not positive
     * or that the resource is {@link IResource#isBlank() blank}.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return amount <= 0 || resource.isBlank();
    }
}
