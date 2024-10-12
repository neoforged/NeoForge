/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;

/**
 * Additional methods for {@link CommandSourceStack} so that commands and arguments can access various things without directly referencing using server specific classes
 */
public interface ICommandSourceStackExtension {
    private CommandSourceStack self() {
        return (CommandSourceStack) this;
    }

    /**
     * @return the scoreboard
     */
    default Scoreboard getScoreboard() {
        return self().getServer().getScoreboard();
    }

    /**
     * @return the advancement from the id
     */
    @Nullable
    default AdvancementHolder getAdvancement(ResourceLocation id) {
        return self().getServer().getAdvancements().get(id);
    }

    /**
     * @return the level but without being specifically the server side level
     */
    default Level getUnsidedLevel() {
        return self().getLevel();
    }
}
