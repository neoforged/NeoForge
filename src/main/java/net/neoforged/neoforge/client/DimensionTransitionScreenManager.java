/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.datafixers.util.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterDimensionTransitionScreenEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class DimensionTransitionScreenManager {
    private static final Map<Pair<ResourceKey<Level>, ResourceKey<Level>>, ReceivingLevelScreenFactory> conditionalDimensionEffects = new HashMap<>();
    private static final Map<ResourceKey<Level>, ReceivingLevelScreenFactory> toDimensionTransitions = new HashMap<>();
    private static final Map<ResourceKey<Level>, ReceivingLevelScreenFactory> fromDimensionTransitions = new HashMap<>();

    @ApiStatus.Internal
    static void init() {
        ModLoader.postEventWrapContainerInModOrder(new RegisterDimensionTransitionScreenEvent(conditionalDimensionEffects, toDimensionTransitions, fromDimensionTransitions));
    }

    public static ReceivingLevelScreenFactory getScreen(@Nullable ResourceKey<Level> toDimension, @Nullable ResourceKey<Level> fromDimension) {
        var conditionalScreen = conditionalDimensionEffects.get(Pair.of(toDimension, fromDimension));
        if (conditionalScreen != null) {
            return conditionalScreen;
        }
        var toDim = toDimensionTransitions.get(toDimension);
        if (toDim != null) {
            return toDim;
        }
        var fromDim = fromDimensionTransitions.get(fromDimension);
        if (fromDim != null) {
            return fromDim;
        }
        return ReceivingLevelScreen::new;
    }

    public interface ReceivingLevelScreenFactory {
        ReceivingLevelScreen create(BooleanSupplier supplier, ReceivingLevelScreen.Reason reason);
    }
}
