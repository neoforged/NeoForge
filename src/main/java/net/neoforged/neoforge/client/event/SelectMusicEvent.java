/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.Music;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when the {@link net.minecraft.client.sounds.MusicManager} checks what situational music should be used. This fires before the music begins playing.<br>
 * If the music is set to {@code null} by a modder, it will cancel any music that was already playing.<br>
 * <br>
 * Note that the higher priority you make your event listener, the earlier the music will be set.<br>
 * Because of this, if you want your music to take precedence over others (perhaps you want to have seperate nighttime music for a biome for instance) then you may want it to have a lower priority.<br>
 * <br>
 * To make your music instantly play rather than waiting for the playing music to stop, set the music to one that {@linkplain Music#replaceCurrentMusic() is set to replace the current music.}<br>
 * <br>
 * Higher priorities would likely be better suited for biome-based or dimension-based musics, whereas lower priority is likely good for specific structures or situations.<br>
 * <br>
 * This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.<br>
 * If the event is cancelled, then no further modification to the {@link Music} value will be allowed. This should only be used if your music should take priority over others, such as for boss music.<br>
 * <br>
 * This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},<br>
 * only on the {@linkplain LogicalSide#CLIENT logical client}.
 *
 */
public class SelectMusicEvent extends Event implements ICancellableEvent {
    private @Nullable Music music;
    private final Music originalMusic;
    private final @Nullable SoundInstance playingMusic;

    public SelectMusicEvent(Music music, @Nullable SoundInstance playingMusic) {
        this.music = music;
        this.originalMusic = music;
        this.playingMusic = playingMusic;
    }

    /**
     * {@return the original situational music that was selected}
     */
    public Music getOriginalMusic() {
        return originalMusic;
    }

    /**
     * {@return the current track that the {@link net.minecraft.client.sounds.MusicManager} is playing, or {@code null} if there is none.}
     */
    @Nullable
    public SoundInstance getPlayingMusic() {
        return playingMusic;
    }

    /**
     * {@return the Music to be played, or {@code null} if any playing music should be cancelled}
     */
    @Nullable
    public Music getMusic() {
        return music;
    }

    /**
     * Changes the situational music. If this is set to {@code null}, any currently playing music will be cancelled.<br>
     * If this <i>was</i> {@code null} but on the next tick isn't, the muisc given will be immediately played.<br>
     * <br>
     * Note that if {@link #isCanceled()} is {@code true}, this method will do nothing, as it is meant to override other musics.
     */
    public void setMusic(@Nullable Music newMusic) {
        if (!this.isCanceled()) {
            this.music = newMusic;
        }
    }
}
