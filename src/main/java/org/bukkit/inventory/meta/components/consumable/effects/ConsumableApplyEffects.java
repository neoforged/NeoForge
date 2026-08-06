package org.bukkit.inventory.meta.components.consumable.effects;

import java.util.List;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

/**
 * Represent the effects applied when an item is consumed.
 */
public interface ConsumableApplyEffects extends ConsumableEffect {

    /**
     * Gets the effects which may be applied by this item when consumed.
     *
     * @return consumable effects
     */
    @NotNull
    List<PotionEffect> getEffects();

    /**
     * Sets the effects which may be applied by this item when consumed.
     *
     * @param effects new effects
     */
    void setEffects(@NotNull List<PotionEffect> effects);

    /**
     * Adds an effect which may be applied by this item when consumed.
     *
     * @param effect the effect
     * @return the added effect
     */
    @NotNull
    PotionEffect addEffect(@NotNull PotionEffect effect);

    /**
     * Gets the probability of this effect being applied.
     *
     * @return probability
     */
    float getProbability();

    /**
     * Sets the probability of this effect being applied.
     *
     * @param probability between 0 and 1 inclusive.
     */
    void setProbability(float probability);
}
