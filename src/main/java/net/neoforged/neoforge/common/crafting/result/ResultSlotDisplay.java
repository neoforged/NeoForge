package net.neoforged.neoforge.common.crafting.result;

import net.minecraft.world.item.crafting.display.SlotDisplay;

/**
 * Superinterface for {@link Result}-dependent {@link SlotDisplay}s.
 *
 * @param <T> The generic type of the {@link Result}.
 */
public interface ResultSlotDisplay<T> extends SlotDisplay {
    Result<T> result();
}
