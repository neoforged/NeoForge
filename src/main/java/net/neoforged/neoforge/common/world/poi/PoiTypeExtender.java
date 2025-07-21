/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world.poi;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.registries.GameData;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

@ApiStatus.Internal
public final class PoiTypeExtender {
    public static void extendPoiTypes() {
        ModLoader.postEvent(new ExtendPoiTypesEvent(PoiTypeExtender::register));
    }

    private static void register(ResourceKey<PoiType> typeKey, Set<BlockState> states) {
        Map<BlockState, Holder<PoiType>> statePoiMap = GameData.getBlockStatePointOfInterestTypeMap();
        Holder<PoiType> type = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getOrThrow(typeKey);
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

        if (type.value().matchingStates() instanceof PoiStateSet poiStateSet) {
            poiStateSet.addCustomStates(states);
        } else {
            // Detect accessor mixins which may have replaced the set to add additional BlockStates after the PoiType's construction
            List<String> accessors = Arrays.stream(PoiType.class.getDeclaredMethods())
                    .filter(method -> method.isSynthetic() &&
                            method.isAnnotationPresent(Accessor.class) &&
                            method.getReturnType() == void.class &&
                            method.getParameterCount() == 1 &&
                            method.getParameterTypes()[0] == Set.class)
                    .map(mth -> mth.getAnnotation(MixinMerged.class))
                    .filter(Objects::nonNull)
                    .map(MixinMerged::mixin)
                    .toList();

            String message;
            if (accessors.isEmpty()) {
                message = String.format(
                        Locale.ROOT,
                        "The matchingStates set of PoiType %s was replaced after construction",
                        Objects.requireNonNull(type.getKey()).location());
            } else {
                StringBuilder accessorList = new StringBuilder();
                for (String accessor : accessors) {
                    accessorList.append("\n").append(accessor);
                }
                message = String.format(
                        Locale.ROOT,
                        "The matchingStates set of PoiType %s was replaced after construction. Accessor mixins for mutating the set were found:%s",
                        Objects.requireNonNull(type.getKey()).location(),
                        accessorList);
            }
            throw new IllegalStateException(message);
        }
    }

    private PoiTypeExtender() {}
}
