/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.brewing;

import net.minecraft.world.item.alchemy.PotionBrewing;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event to register new brewing recipes.
 *
 * <p>Fired on both client and server side, on the main event bus.
 */
public class RegisterBrewingRecipesEvent extends Event {
    private final PotionBrewing.Builder builder;

    @ApiStatus.Internal
    public RegisterBrewingRecipesEvent(PotionBrewing.Builder builder) {
        this.builder = builder;
    }

    public PotionBrewing.Builder getBuilder() {
        return builder;
    }
}
