/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import net.minecraft.world.Difficulty;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;

/**
 * DifficultyChangeEvent is fired when difficulty is changing. <br>
 * <br>
 * This event is fired via the {@link CommonHooks#onDifficultyChange(Difficulty, Difficulty)}.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link NeoForge#EVENT_BUS}.
 **/
public class DifficultyChangeEvent extends Event {
    private final Difficulty difficulty;
    private final Difficulty oldDifficulty;

    public DifficultyChangeEvent(Difficulty difficulty, Difficulty oldDifficulty) {
        this.difficulty = difficulty;
        this.oldDifficulty = oldDifficulty;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Difficulty getOldDifficulty() {
        return oldDifficulty;
    }
}
