/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world.poi;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.registries.GameData;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class PoiTypeExtender {
    public static void extendPoiTypes() {
        ModLoader.postEvent(new ExtendPoiTypesEvent(PoiTypeExtender::register));
    }

    private static void register(Holder<PoiType> type, Set<BlockState> states) {
        Map<BlockState, Holder<PoiType>> statePoiMap = GameData.getBlockStatePointOfInterestTypeMap();
        for (BlockState state : states) {
            Holder<PoiType> prevType = statePoiMap.putIfAbsent(state, type);
            if (prevType != null) {
                throw new IllegalStateException(String.format(
                        Locale.ROOT,
                        "%s is defined in more than one PoI type (old: %s, new: %s)",
                        state,
                        prevType.value(),
                        type.value()));
            }
        }
        ((PoiStateSet) type.value().matchingStates()).addCustomStates(states);
    }

    private PoiTypeExtender() {}
}
