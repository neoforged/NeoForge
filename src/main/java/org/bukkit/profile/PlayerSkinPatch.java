package org.bukkit.profile;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.Nullable;

public interface PlayerSkinPatch extends ConfigurationSerializable {

    /**
     * Gets the path to the profile texture patched onto this profile.
     *
     * @return the model texture
     */
    @Nullable
    public NamespacedKey getTexturePatch();

    /**
     * Gets the path to the profile texture patched onto this profile.
     *
     * @param texture the model texture
     */
    public void setTexturePatch(@Nullable NamespacedKey texture);

    /**
     * Gets the path to the profile cape texture patched onto this profile.
     *
     * @return the cape texture
     */
    @Nullable
    public NamespacedKey getCapeTexturePatch();

    /**
     * Gets the path to the profile cape texture patched onto this profile.
     *
     * @param cape the cape texture
     */
    public void setCapeTexturePatch(@Nullable NamespacedKey cape);

    /**
     * Gets the path to the profile elytra texture patched onto this profile.
     *
     * @return the elyra texture
     */
    @Nullable
    public NamespacedKey getElytraTexturePatch();

    /**
     * Gets the path to the profile elytra texture patched onto this profile.
     *
     * @param elytra the elyra texture
     */
    public void setElytraTexturePatch(@Nullable NamespacedKey elytra);

    /**
     * Gets the type of model patched onto this profile.
     *
     * @return model type
     */
    @Nullable
    public PlayerTextures.SkinModel getModelPatch();

    /**
     * Gets the type of model patched onto this profile.
     *
     * @param model model type
     */
    public void setModelPatch(@Nullable PlayerTextures.SkinModel model);

    /**
     * Checks if the patch has no patched components.
     *
     * @return <code>true</code> if the patch has no patched components
     */
    boolean isEmpty();
}
