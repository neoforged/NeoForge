/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gamerules;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.gamerules.GameRule;

/**
 * Copy of {@link EditGameRulesScreen.IntegerRuleEntry} updated to be used in a static context for any {@link GameRule}
 * <p>
 * It is recommended for advanced types to make use of {@link RegisterGameRuleEntryFactoryEvent}
 */
public class GenericGameRuleEntry<T> extends GameRuleEntry {
    private final EditBox input;

    public GenericGameRuleEntry(EditGameRulesScreen screen, Component label, List<FormattedCharSequence> tooltip, String str, GameRule<T> gameRule) {
        super(screen.getFont(), tooltip, label);

        input = new EditBox(font, 10, 5, 44, 20, label.copy().append("\n").append(str).append("\n"));
        input.setValue(screen.gameRules.getAsString(gameRule));
        input.setResponder(value -> {
            var dataresult = gameRule.deserialize(value);

            if (dataresult.isSuccess()) {
                input.setTextColor(CommonColors.TEXT_GRAY);
                screen.clearInvalid(this);
                screen.gameRules.set(gameRule, dataresult.getOrThrow(), null);
            } else {
                input.setTextColor(CommonColors.RED);
                screen.markInvalid(this);
            }
        });

        children.add(input);
    }

    @Override
    public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float particlTick) {
        renderLabel(graphics, getContentY(), getContentX());
        input.setX(getContentRight() - 45);
        input.setY(getContentY());
        input.render(graphics, mouseX, mouseY, particlTick);
    }
}
