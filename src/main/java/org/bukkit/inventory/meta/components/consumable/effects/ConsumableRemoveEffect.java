package org.bukkit.inventory.meta.components.consumable.effects;

import java.util.List;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

/**
 * Represent the effects to be removed when an item is consumed.
 */
public interface ConsumableRemoveEffect extends ConsumableEffect {

    /**
     * Gets the effects which may be removed by this item when consumed.
     *
     * @return the effects
     */
    @NotNull
    List<PotionEffectType> getEffectTypes();

    /**
     * Sets the effects which may be removed by this item when consumed.
     *
     * @param effects new effects
     */
    void setEffectTypes(@NotNull List<PotionEffectType> effects);

    /**
     * Adds an effect which may be applied by this item when consumed.
     *
     * @param effect the effect
     * @return the added effect
     */
    @NotNull
    PotionEffectType addEffectType(@NotNull PotionEffectType effect);
}
