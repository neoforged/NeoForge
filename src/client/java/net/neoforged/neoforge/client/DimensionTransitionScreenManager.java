/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.datafixers.util.Pair;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.multiplayer.LevelLoadTracker;
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

    public static ReceivingLevelScreenFactory getScreenFromLevel(@Nullable Level target, @Nullable Level source) {
        if (source == null) { //source level is null on login: transition screen should not appear in this case
            return getScreen(null, null);
        } else if (target == null) { //the target level shouldn't ever be null, but anyone could call Minecraft.setLevel and pass null in
            return getScreen(null, source.dimension());
        }
        return getScreen(target.dimension(), source.dimension());
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
        return LevelLoadingScreen::new;
    }

    // TODO: Porting 1.21.9 Rename?
    public interface ReceivingLevelScreenFactory {
        LevelLoadingScreen create(LevelLoadTracker levelLoadTracker, LevelLoadingScreen.Reason reason);
    }
}
