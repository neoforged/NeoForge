/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gamerules;

import java.util.List;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.gamerules.GameRule;

@FunctionalInterface
public interface GameRuleEntryFactory<T> {
    EditGameRulesScreen.RuleEntry create(EditGameRulesScreen screen, Component description, List<FormattedCharSequence> tooltip, String str, GameRule<T> gameRule);

    default EditGameRulesScreen.EntryFactory<T> toVanilla(EditGameRulesScreen screen) {
        return (label, tooltip, str, gameRule) -> create(screen, label, tooltip, str, gameRule);
    }
}
