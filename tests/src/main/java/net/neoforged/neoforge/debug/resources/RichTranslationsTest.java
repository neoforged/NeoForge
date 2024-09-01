/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.resources;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import org.apache.commons.lang3.mutable.MutableBoolean;

@ForEachTest(groups = RichTranslationsTest.GROUP)
public class RichTranslationsTest {
    public static final String GROUP = "resources";

    @TestHolder(description = "Tests that rich translations work properly", enabledByDefault = true)
    @GameTest
    @EmptyTemplate("1x1x1")
    static void richTranslations(final DynamicTest test) {
        test.onGameTest(helper -> {
            final String arg = "Example argument";
            final Component simple = Component.translatable("rich_translations_test.simple_translation", arg);
            final Component simpleRich = Component.translatable("rich_translations_test.simple_rich_translation", arg);

            helper.assertTrue(simpleRich.getString().equals(simple.getString()), "Rich translation isn't equivalent to simple translation");

            final String translation = Language.getInstance().getOrDefault("rich_translations_test.simple_rich_translation");
            helper.assertTrue(
                    String.format(translation, arg).equals(simpleRich.getString()),
                    "Translatable component isn't equivalent to I18n");

            final Component fancy = Component.translatable("rich_translations_test.fancy_rich_translation", arg);
            final MutableBoolean foundRed = new MutableBoolean();
            final MutableBoolean foundBlue = new MutableBoolean();

            fancy.visit((style, content) -> {
                if (TextColor.fromLegacyFormat(ChatFormatting.RED).equals(style.getColor()) && content.equals("Ooo, colors!")) {
                    foundRed.setTrue();
                }

                if (TextColor.fromLegacyFormat(ChatFormatting.BLUE).equals(style.getColor()) && content.equals(arg)) {
                    foundBlue.setTrue();
                }

                return Optional.empty();
            }, Style.EMPTY);

            helper.assertTrue(foundBlue.isTrue() && foundRed.isTrue(), "Rich translation lost colors");

            helper.succeed();
        });
    }

    @TestHolder(description = "Tests that rich translations (text json style) work properly", enabledByDefault = true)
    @GameTest
    @EmptyTemplate("1x1x1")
    static void richTranslations3(final DynamicTest test) {
        test.onGameTest(helper -> {
            final String arg = "Example argument";
            final Component simple = Component.translatable("rich_translations_test.simple_translation", arg);
            final Component simpleRich = Component.translatable("rich_translations_test.simple_rich_translation3", arg);

            helper.assertTrue(simpleRich.getString().equals(simple.getString()), "Rich translation isn't equivalent to simple translation");

            final String translation = Language.getInstance().getOrDefault("rich_translations_test.simple_rich_translation3");
            helper.assertTrue(
                    String.format(translation, arg).equals(simpleRich.getString()),
                    "Translatable component isn't equivalent to I18n");

            final Component fancy = Component.translatable("rich_translations_test.fancy_rich_translation3", arg);
            final MutableBoolean foundRed = new MutableBoolean();
            final MutableBoolean foundBlue = new MutableBoolean();

            fancy.visit((style, content) -> {
                if (TextColor.fromLegacyFormat(ChatFormatting.RED).equals(style.getColor()) && content.equals("Ooo, colors!")) {
                    foundRed.setTrue();
                }

                if (TextColor.fromLegacyFormat(ChatFormatting.BLUE).equals(style.getColor()) && content.equals(arg)) {
                    foundBlue.setTrue();
                }

                return Optional.empty();
            }, Style.EMPTY);

            helper.assertTrue(foundBlue.isTrue() && foundRed.isTrue(), "Rich translation lost colors");

            helper.succeed();
        });
    }
}
