/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gamerules;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.client.gui.screens.worldselection.AbstractGameRulesScreen;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.neoforged.fml.ModLoader;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class GameRuleEntryFactoryManager {
    private static final Map<GameRuleType, GameRuleEntryFactory<?>> FACTORIES = Maps.newEnumMap(GameRuleType.class);
    private static final GameRuleEntryFactory<?> GENERIC_FACTORY = GenericGameRuleEntry::new;

    public static void register() {
        ModLoader.postEvent(new RegisterGameRuleEntryFactoryEvent(FACTORIES));
    }

    @SuppressWarnings("unchecked")
    public static <T> void appendGameRuleEntry(AbstractGameRulesScreen screen, GameRule<T> gameRule, BiConsumer<GameRule<T>, AbstractGameRulesScreen.EntryFactory<T>> addEntry) {
        var ruleType = gameRule.gameRuleType();

        if (ruleType == GameRuleType.BOOL || ruleType == GameRuleType.INT) {
            // vanilla rule types are appened using the `visitInteger`/`visitBoolean` overloads
            return;
        }

        var factory = FACTORIES.get(gameRule.gameRuleType());
        var vanillaFactory = ((GameRuleEntryFactory<T>) (factory == null ? GENERIC_FACTORY : factory)).toVanilla(screen);
        addEntry.accept(gameRule, vanillaFactory);
    }
}
