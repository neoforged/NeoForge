package org.bukkit.inventory;

import org.bukkit.entity.AbstractHorse;
import org.jetbrains.annotations.Nullable;

/**
 * An interface to the inventory of an {@link AbstractHorse}.
 */
public interface AbstractHorseInventory extends Inventory {

    /**
     * Gets the item in the horse's saddle slot.
     *
     * @return the saddle item
     * @deprecated llama's cannot have saddles
     */
    @Nullable
    @Deprecated(since = "1.21.5")
    ItemStack getSaddle();

    /**
     * Sets the item in the horse's saddle slot.
     *
     * @param stack the new item
     * @deprecated llama's cannot have saddles
     */
    @Deprecated(since = "1.21.5")
    void setSaddle(@Nullable ItemStack stack);
}
