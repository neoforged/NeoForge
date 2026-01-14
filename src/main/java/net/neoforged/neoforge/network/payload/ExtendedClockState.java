/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.clock.ClockState;
import org.jetbrains.annotations.ApiStatus;

/**
 * Carries the extended information that Vanilla normally stores in {@link net.minecraft.world.clock.ClockState}.
 */
@ApiStatus.Internal
public record ExtendedClockState(ClockState state, float fractionalTick, float speed) {
    public static final Codec<ExtendedClockState> CODEC = RecordCodecBuilder.create(
            i -> i.group(
                    ClockState.CODEC.fieldOf("clock_state").forGetter(ExtendedClockState::state),
                    Codec.FLOAT.fieldOf("fractional_tick").forGetter(ExtendedClockState::fractionalTick),
                    Codec.FLOAT.fieldOf("speed").forGetter(ExtendedClockState::speed))
                    .apply(i, ExtendedClockState::new));
    public static final StreamCodec<ByteBuf, ExtendedClockState> STREAM_CODEC = StreamCodec.composite(
            ClockState.STREAM_CODEC, ExtendedClockState::state,
            ByteBufCodecs.FLOAT, ExtendedClockState::fractionalTick,
            ByteBufCodecs.FLOAT, ExtendedClockState::speed,
            ExtendedClockState::new);
}
