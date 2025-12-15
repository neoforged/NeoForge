/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gamerules;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.neoforged.fml.ModLoader;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class GameRuleEntryFactoryManager {
    private static final Map<GameRuleType, GameRuleEntryFactory<?>> FACTORIES = Maps.newEnumMap(GameRuleType.class);

    public static void register() {
        ModLoader.postEvent(new RegisterGameRuleEntryFactoryEvent(FACTORIES));
    }

    @SuppressWarnings("unchecked")
    public static <T> EditGameRulesScreen.RuleEntry createEntry(EditGameRulesScreen screen, Component label, List<FormattedCharSequence> tooltip, String str, GameRule<T> gameRule) {
        var factory = FACTORIES.get(gameRule.gameRuleType());

        if (factory != null) {
            return ((GameRuleEntryFactory<T>) factory).create(screen, label, tooltip, str, gameRule);
        }

        return new GenericGameRuleEntry<>(screen, label, tooltip, str, gameRule);
    }
}
