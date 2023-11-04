/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event.sound;

import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.event.sound.SoundEvent.SoundSourceEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when a <em>non-streaming</em> sound is being played. A non-streaming sound is loaded fully into memory
 * in a buffer before being played, and used for most sounds of short length such as sound effects for clicking
 * buttons.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 *
 * @see PlayStreamingSourceEvent
 */
public class PlaySoundSourceEvent extends SoundSourceEvent {
    @ApiStatus.Internal
    public PlaySoundSourceEvent(SoundEngine engine, SoundInstance sound, Channel channel) {
        super(engine, sound, channel);
    }
}
