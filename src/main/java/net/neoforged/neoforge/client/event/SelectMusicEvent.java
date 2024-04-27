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
 * <p>Generally, you should most likely choose something around these lines for your listener's priority:</p>
 *
 * <p>{@linkplain EventPriority#HIGHEST HIGHEST} should generally be used for more ambient situational music tracks such as ones based on biome or dimension that need to be more dynamic than vanilla's system for biome music specified in the biome's json.</p>
 * <p>{@linkplain EventPriority#HIGH HIGH} should generally be used for slightly more dynamic ambience, such as potential nighttime music for instance.</p>
 * <p>{@linkplain EventPriority#NORMAL NORMAL} should generally be used for music tracks that are meant to be played at a certain area, for instance a structure.</p>
 * <p>{@linkplain EventPriority#LOW LOW} would should generally be used for more dynamic instances where you want your music to be fairly high-priority, for instance maybe while in combat.</p>
 * <p>{@linkplain EventPriority#LOWEST LOWEST} should generally be used in situations where you want your music to take priority over any other music tracks, such as in a boss fight.</p>
 *
 * <p>Note that these of course are just general suggestions and your specific use case may call for using a different priority. You can also access the original music track vanilla would have chosen, along with whichever track the event's value was last set to.
 * This can be used for even more dynamic music sequences, such as ones that change based on what music would have played.</p>
 *
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
