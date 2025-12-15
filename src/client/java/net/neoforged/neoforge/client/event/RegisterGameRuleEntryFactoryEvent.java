/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.gui.widget.gamerule.GenericGameRuleEntry;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired to allow modders to register custom {@link EditGameRulesScreen.RuleEntry} factories.
 * <p>
 * This event is fired on the mod-specific event bus, only on the {@link LogicalSide#CLIENT logical client}.
 * <p>
 * While you are not required to register a custom factory for your {@link GameRuleType}, it is recommended to do so
 * of your game rule requires more than a simple {@link EditBox}. For example a togglable types similar to {@link GameRuleType#BOOL Boolean}
 * would register a new factory which makes use of a {@link CycleButton} insead of a {@link EditBox}.
 * <p>
 * When no factory exists for a given type the {@link GenericGameRuleEntry generic entry} will be used instead.
 */
public final class RegisterGameRuleEntryFactoryEvent extends Event implements IModBusEvent {
    private final Map<GameRuleType, Factory<?>> factories;

    @ApiStatus.Internal
    public RegisterGameRuleEntryFactoryEvent(Map<GameRuleType, Factory<?>> factories) {
        this.factories = factories;
    }

    public <T> void register(GameRuleType gameRuleType, Factory<T> factory) {
        if (gameRuleType == GameRuleType.INT || gameRuleType == GameRuleType.BOOL) {
            throw new IllegalStateException("Registering custom entry factory for vanilla GameRuleTypes is disallowed");
        }

        if (factories.putIfAbsent(gameRuleType, factory) != null) {
            throw new IllegalStateException("Duplicate GameRuleTypeEntryFactory registration!");
        }
    }

    @FunctionalInterface
    public interface Factory<T> {
        EditGameRulesScreen.RuleEntry create(EditGameRulesScreen screen, Component description, List<FormattedCharSequence> tooltip, String str, GameRule<T> gameRule);
    }
}
