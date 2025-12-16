/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gamerules;

import java.util.Map;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired to allow modders to register custom {@link EditGameRulesScreen.RuleEntry} factories.
 * <p>
 * This event is fired on the mod-specific event bus, only on the {@link LogicalSide#CLIENT logical client}.
 * <p>
 * While you are not required to register a custom factory for your {@link GameRuleType}, it is recommended to do so
 * if your game rule requires more than a simple {@link EditBox}. For example a togglable type similar to {@link GameRuleType#BOOL Boolean}
 * would register a new factory which makes use of a {@link CycleButton} insead of a {@link EditBox}.
 * <p>
 * When no factory exists for a given type the {@link GenericGameRuleEntry generic entry} will be used instead.
 */
public final class RegisterGameRuleEntryFactoryEvent extends Event implements IModBusEvent {
    private final Map<GameRuleType, GameRuleEntryFactory<?>> factories;

    @ApiStatus.Internal
    RegisterGameRuleEntryFactoryEvent(Map<GameRuleType, GameRuleEntryFactory<?>> factories) {
        this.factories = factories;
    }

    public <T> void register(GameRuleType gameRuleType, GameRuleEntryFactory<T> factory) {
        if (gameRuleType == GameRuleType.INT || gameRuleType == GameRuleType.BOOL) {
            throw new IllegalStateException("Registering custom entry factory for vanilla GameRuleTypes is disallowed");
        }

        if (factories.putIfAbsent(gameRuleType, factory) != null) {
            throw new IllegalStateException("Duplicate GameRuleTypeEntryFactory registration!");
        }
    }
}
