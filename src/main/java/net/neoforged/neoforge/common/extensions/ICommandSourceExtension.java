/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import com.mojang.authlib.GameProfile;
import java.util.Optional;
import net.minecraft.commands.CommandSource;

/**
 * Additional methods for {@link CommandSource} so that command source stacks can retrieve the {@link GameProfile}s.
 */
public interface ICommandSourceExtension {
    /**
     * @return the game profile of this command source if available
     */
    default Optional<GameProfile> getGameProfile() {
        return Optional.empty();
    }
}
