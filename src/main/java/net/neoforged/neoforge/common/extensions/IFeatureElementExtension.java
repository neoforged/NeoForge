package net.neoforged.neoforge.common.extensions;

import net.minecraft.world.flag.FeatureFlagSet;

public interface IFeatureElementExtension {
    /**
     * Returns {@code true} if this element is disabled, {@code false} otherwise.
     * <p>
     * Recommended to invoke {@linkplain net.minecraft.world.flag.FeatureElement#isEnabled(FeatureFlagSet)} if you have access to {@linkplain net.minecraft.world.level.LevelReader#enabledFeatures()},
     * as that will also check for Vanillas Experiments, this method will only check if modded elements are disabled.
     *
     * @return {@code true} to fully disable this element
     */
    default boolean isDisabled() {
        return false;
    }
}
