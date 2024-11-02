/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.brewing;

import net.minecraft.core.RegistryAccess;
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
    private final RegistryAccess registryAccess;

    @ApiStatus.Internal
    public RegisterBrewingRecipesEvent(PotionBrewing.Builder builder, RegistryAccess registryAccess) {
        this.builder = builder;
        this.registryAccess = registryAccess;
    }

    public PotionBrewing.Builder getBuilder() {
        return builder;
    }

    public RegistryAccess getRegistryAccess() {
        return registryAccess;
    }
}
