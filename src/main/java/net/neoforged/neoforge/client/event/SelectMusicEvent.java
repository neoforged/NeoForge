/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when the {@link net.minecraft.client.sounds.MusicManager} checks what situational music should be used. This fires before the music begins playing.
 * This can be used to change or prevent (by passing {@code null)} any {@link Music} from being selected, which will
 * either change what the music will be next time it plays based on the situation, or prevent it from playing any if {@code null} is passed in.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 *
 * @see Minecraft#getSituationalMusic()
 */
public class SelectMusicEvent extends Event {
    private @Nullable Music music;
    private final Music originalMusic;

    public SelectMusicEvent(Music music) {
        this.music = music;
        this.originalMusic = music;
    }

    /**
     * {@return the original music that was to be played}
     */
    public Music getOriginalMusic() {
        return originalMusic;
    }

    /**
     * {@return the Music to be played, or {@code null} if there should be no music based on the situation}
     */
    @Nullable
    public Music getMusic() {
        return music;
    }

    /**
     * Changes what music should be selected, which may be {@code null} to keep the music from being played
     *
     * @param newMusic the new {@link Music} to be played, or {@code null} for no music
     */
    public void setMusic(@Nullable Music newMusic) {
        this.music = newMusic;
    }
}
