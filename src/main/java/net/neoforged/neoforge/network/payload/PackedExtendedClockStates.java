/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.world.clock.WorldClock;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record PackedExtendedClockStates(Map<Holder<WorldClock>, ExtendedClockState> clocks) {
    public static final PackedExtendedClockStates EMPTY = new PackedExtendedClockStates(Map.of());
    public static final Codec<PackedExtendedClockStates> CODEC = Codec.unboundedMap(WorldClock.CODEC, ExtendedClockState.CODEC)
            .xmap(PackedExtendedClockStates::new, PackedExtendedClockStates::clocks);
}
