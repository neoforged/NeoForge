package org.bukkit.inventory.meta.components.consumable;

import java.util.List;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.meta.components.consumable.effects.ConsumableEffect;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a component which item can be consumed on use.
 */
@ApiStatus.Experimental
public interface ConsumableComponent extends ConfigurationSerializable {

    /**
     * Gets the time in seconds it will take for this item to be consumed.
     *
     * @return consume time
     */
    float getConsumeSeconds();

    /**
     * Sets the time in seconds it will take for this item to be consumed.
     *
     * @param consumeSeconds new consume time
     */
    void setConsumeSeconds(float consumeSeconds);

    /**
     * Gets the animation used during consumption of the item.
     *
     * @return animation
     */
    @NotNull
    Animation getAnimation();

    /**
     * Sets the animation used during consumption of the item.
     *
     * @param animation the new animation
     */
    void setAnimation(@NotNull Animation animation);

    /**
     * Gets the sound to play during and on completion of the item's
     * consumption.
     *
     * @return the sound
     */
    @Nullable
    Sound getSound();

    /**
     * Sets the sound to play during and on completion of the item's
     * consumption.
     *
     * @param sound sound or null for current default
     */
    void setSound(@Nullable Sound sound);

    /**
     * Gets whether consumption particles are emitted while consuming this item.
     *
     * @return true for particles emitted while consuming, false otherwise
     */
    boolean hasConsumeParticles();

    /**
     * Sets whether consumption particles are emitted while consuming this item.
     *
     * @param consumeParticles if particles need to be emitted while consuming
     * the item
     */
    void setConsumeParticles(boolean consumeParticles);

    /**
     * Gets the effects which may be applied by this item when consumed.
     *
     * @return consumable effects
     */
    @NotNull
    List<ConsumableEffect> getEffects();

    /**
     * Sets the effects which may be applied by this item when consumed.
     *
     * @param effects new effects
     */
    void setEffects(@NotNull List<ConsumableEffect> effects);

    /**
     * Adds an effect which may be applied by this item when consumed.
     *
     * @param effect the effect
     * @return the added effect
     */
    @NotNull
    ConsumableEffect addEffect(@NotNull ConsumableEffect effect);

    /**
     * Represents the animations for an item being consumed.
     */
    public enum Animation {

        DRINK,
        EAT,
        NONE,
        BLOCK,
        BOW,
        BRUSH,
        CROSSBOW,
        SPEAR,
        SPYGLASS,
        TOOT_HORN;
    }
}
