/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.Music;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;

/// Fired when the [net.minecraft.client.sounds.MusicManager] checks what situational music should be used. This fires before the music begins playing.<br>
/// If the music is set to `null` by a modder, it will cancel any music that was already playing.
///
/// Note that the higher priority you make your event listener, the earlier the music will be set.<br>
/// Because of this, if you want your music to take precedence over others (perhaps you want to have seperate nighttime music for a biome for instance) then you may want it to have a lower priority.
///
/// To make your music instantly play rather than waiting for the playing music to stop, set the music to one that [is set to replace the current music.][Music#replaceCurrentMusic()]
///
/// Higher priorities would likely be better suited for biome-based or dimension-based musics, whereas lower priority is likely good for specific structures or situations.
///
/// This event is [cancellable][ICancellableEvent].<br>
/// Cancelling the event **does not prevent the music from being changed**, rather it stops propagation to further event handlers, and uses whatever the music was set to in the event.<br>
/// When cancelling the event, it is therefore preferred to call the [SelectMusicEvent#overrideMusic(Music)][SelectMusicEvent#overrideMusic(Music)] helper with your music, which automatically calls [SelectMusicEvent#setCanceled(boolean)][SelectMusicEvent#setCanceled(boolean)] for you.
///
/// This event is fired on the [main Forge event bus][NeoForge#EVENT_BUS],<br>
/// only on the [logical client][LogicalSide#CLIENT].
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
     * {@return the current track that the {@link net.minecraft.client.sounds.MusicManager} is playing, or {@code null} if there is none}
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
     * If this <i>was</i> {@code null} but on the next tick isn't, the music given will be immediately played.<br>
     * <br>
     */
    public void setMusic(@Nullable Music newMusic) {
        this.music = newMusic;
    }

    /**
     * Sets the music and then cancels the event so that other listeners will not be invoked.<br>
     * Note that listeners using {@link SubscribeEvent#receiveCanceled()} will still be able to override this, but by default they will not
     */
    public void overrideMusic(@Nullable Music newMusic) {
        this.music = newMusic;
        this.setCanceled(true);
    }
}
