package net.neoforged.neoforge.common.crafting.result;

import net.minecraft.world.item.crafting.display.SlotDisplay;

/**
 * This interface represents a generic recipe result. The result can be resolved to a {@code T} when required.
 *
 * @param <T> The type of the recipe result, i.e. what it ultimately resolves to.
 */
public interface Result<T> {
    /**
     * Resolves the result into a specific {@code T}.
     *
     * @return The resolved result.
     */
    T resolve();

    /**
     * @return The registered {@link ResultType}.
     */
    ResultType<?> type();

    /**
     * Returns the associated {@link SlotDisplay} for the result. The {@link SlotDisplay} should,
     * in similar fashion to the result itself, only be resolved when actually required.
     *
     * @return A {@link SlotDisplay}.
     */
    SlotDisplay display();
}
