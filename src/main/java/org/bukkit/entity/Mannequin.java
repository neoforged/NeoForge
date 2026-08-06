package org.bukkit.entity;

import org.bukkit.entity.model.PlayerModelPart;
import org.bukkit.inventory.MainHand;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a mannequin.
 */
public interface Mannequin extends LivingEntity {

    /**
     * Gets the mannequin's main hand.
     *
     * @return the mannequin's main hand
     */
    @NotNull
    MainHand getMainHand();

    /**
     * Gets the mannequin's main hand.
     *
     * @param hand the mannequin's main hand
     */
    void setMainHand(@NotNull MainHand hand);

    /**
     * Gets whether a part of the mannequin model is shown.
     *
     * @param part model part
     * @return if it is shown
     */
    boolean isModelPartShown(@NotNull PlayerModelPart part);

    /**
     * Sets whether a part of the mannequin model is shown.
     *
     * @param part model part
     * @param shown whether it is shown
     */
    void setModelPartShown(@NotNull PlayerModelPart part, boolean shown);

    /**
     * Gets the profile of the player used to texture the mannequin.
     *
     * @return the profile of the owning player
     */
    @Nullable
    PlayerProfile getPlayerProfile();

    /**
     * Sets the profile of the player used to texture the mannequin.
     * <p>
     * The profile must already contain a skin texture for it to be displayed.
     *
     * @param profile the profile of the player texture.
     * @throws IllegalArgumentException if the profile does not contain the
     * necessary information
     */
    void setPlayerProfile(@Nullable PlayerProfile profile);

    /**
     * Set the mannequin pose.
     *
     * @param pose new pose
     * @throws IllegalArgumentException if the pose is not valid for a mannequin
     */
    void setPose(@NotNull Pose pose);

    /**
     * Gets whether this mannequin can be moved/pushed.
     *
     * @return whether immovable
     */
    boolean isImmovable();

    /**
     * Sets whether this mannequin can be moved/pushed.
     *
     * @param immovable new state
     */
    void setImmovable(boolean immovable);

    /**
     * Gets the description which shows as part of the mannequin's name.
     *
     * @return description the description text
     * @deprecated typo, use {@link #getDescription()}
     */
    @Nullable
    @Deprecated(since = "1.21.11")
    String getDescripion();

    /**
     * Gets the description which shows as part of the mannequin's name.
     *
     * @return description the description text
     */
    @Nullable
    String getDescription();

    /**
     * Sets the description which shows as part of the mannequin's name.
     *
     * @param description the description to show or null for default
     */
    void setDescription(@Nullable String description);

    /**
     * Gets whether the mannequin description is hidden.
     *
     * @return hide description status
     */
    boolean isHideDescription();

    /**
     * Sets whether the mannequin description is hidden.
     *
     * @param hide whether to hide description
     */
    void setHideDescription(boolean hide);
}
