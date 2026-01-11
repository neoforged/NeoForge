/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.function.Consumer;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired to allow modders to register custom {@link GameRuleCategory gamerule categories}.
 * <p>
 * This event is fired on the mod-specific event bus.
 */
public final class RegisterGameRuleCategoryEvent extends Event implements IModBusEvent {
    private final Consumer<GameRuleCategory> registrar;

    @ApiStatus.Internal
    public RegisterGameRuleCategoryEvent(Consumer<GameRuleCategory> registrar) {
        this.registrar = registrar;
    }

    public void register(GameRuleCategory category) {
        registrar.accept(category);
    }
}
