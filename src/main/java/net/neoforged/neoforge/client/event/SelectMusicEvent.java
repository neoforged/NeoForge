/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when the {@link net.minecraft.client.sounds.MusicManager} checks what situational music should be used. This fires before the music begins playing.
 * This can be used to change or prevent (by passing {@code null)} any {@link Music} from being selected, which will
 * either change what the music will be next time it plays based on the situation, or prevent it from playing any if {@code null} is passed in.
 *
 * <p>Note that you should listen to a different {@link EventPriority} based on what music you are using for the replacement.</p>
 *
 * <p>Somewhat counterintuitively, you should listen with a *lower* {@link EventPriority} for music tracks you want to have higher priority.</p>
 * This is because higher event priorities will fire before lower ones, meaning that the *lower* priority listeners end up setting the value <i>later,</i>
 *  therefore making the last listener to set the music the one that gets used.</p>
 *
 * <p>Generally, you should listen with lower event priorities if you want your music to take priority over others. For instance, high priority might be used for biome or dimension-based music,
 * whereas normal might be used for structures, and low might be used for combat or boss battles. This may of course vary based on use case.</p>

 * <p>Note as well that if you want your music track to <i>instantly</i> start and cancel any other playing music, it should be {@linkplain Music#replaceCurrentMusic() set up to do so.}</p>
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 *
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
