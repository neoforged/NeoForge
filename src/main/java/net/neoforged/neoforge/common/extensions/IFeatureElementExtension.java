/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import com.google.errorprone.annotations.ForOverride;
import java.util.function.BooleanSupplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.network.IContainerFactory;

public interface IFeatureElementExtension {
    /**
     * Returns {@code true} if this element is enabled, {@code false} otherwise.
     * <p>
     * This method should not be invoked manually, it is recommended to invoke {@linkplain FeatureElement#isEnabled(FeatureFlagSet)} where possible,
     * as this will also validate against Mojangs {@linkplain FeatureFlags} which must still be done to adhere to their experiments data-packs.
     * <p>
     * That being said, if for what ever reason you do not have access to {@linkplain LevelReader#enabledFeatures()} it is safe to invoke this method.
     * <p>
     * It is also safe to invoke this method to bind multiple elements together so that they use the same flag.<br>
     * Vanilla examples of this include {@linkplain BlockItem#isFeatureEnabled()} and {@linkplain SpawnEggItem#isFeatureEnabled()}
     *
     * @return {@code true} to enable this element, {@code false} to disable.
     */
    @ForOverride
    default boolean isFeatureEnabled() {
        return true;
    }

    /**
     * Used to mark an element as always being enabled
     * <p>
     * To be used in conjunction with
     * <ul>
     * <li>{@linkplain BlockBehaviour.Properties#isFeatureEnabled(BooleanSupplier)}</li>
     * <li>{@linkplain Item.Properties#isFeatureEnabled(BooleanSupplier)}</li>
     * <li>{@linkplain EntityType.Builder#isFeatureEnabled(BooleanSupplier)}</li>
     * <li>{@linkplain IMenuTypeExtension#create(IContainerFactory, BooleanSupplier)}</li>
     * </ul>
     */
    static boolean always() {
        return true;
    }

    /**
     * Used to mark an element as never being enabled
     * <p>
     * To be used in conjunction with
     * <ul>
     * <li>{@linkplain BlockBehaviour.Properties#isFeatureEnabled(BooleanSupplier)}</li>
     * <li>{@linkplain Item.Properties#isFeatureEnabled(BooleanSupplier)}</li>
     * <li>{@linkplain EntityType.Builder#isFeatureEnabled(BooleanSupplier)}</li>
     * <li>{@linkplain IMenuTypeExtension#create(IContainerFactory, BooleanSupplier)}</li>
     * </ul>
     */
    static boolean never() {
        return false;
    }
}
