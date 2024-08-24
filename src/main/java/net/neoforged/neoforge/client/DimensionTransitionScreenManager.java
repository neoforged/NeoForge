/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterDimensionTransitionScreenEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class DimensionTransitionScreenManager {
    private static final Map<ResourceKey<Level>, BiFunction<BooleanSupplier, ReceivingLevelScreen.Reason, ReceivingLevelScreen>> toDimensionTransitions = new HashMap<>();
    private static final Map<ResourceKey<Level>, BiFunction<BooleanSupplier, ReceivingLevelScreen.Reason, ReceivingLevelScreen>> fromDimensionTransitions = new HashMap<>();

    @ApiStatus.Internal
    static void init() {
        ModLoader.postEventWrapContainerInModOrder(new RegisterDimensionTransitionScreenEvent(toDimensionTransitions, fromDimensionTransitions));
    }

    public static BiFunction<BooleanSupplier, ReceivingLevelScreen.Reason, ReceivingLevelScreen> getScreen(@Nullable ResourceKey<Level> toDimension, @Nullable ResourceKey<Level> fromDimension) {
        return toDimensionTransitions.getOrDefault(toDimension, fromDimensionTransitions.getOrDefault(fromDimension, ReceivingLevelScreen::new));
    }
}
