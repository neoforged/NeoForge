/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.resources;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import org.apache.commons.lang3.mutable.MutableBoolean;

@ForEachTest(groups = RichTranslationsTest.GROUP)
public class RichTranslationsTest {
    public static final String GROUP = "resources";

    @GameTest
    @EmptyTemplate("1x1x1")
    @TestHolder(description = "Tests that rich translations work properly")
    static void richTranslations(final DynamicTest test) {
        test.onGameTest(helper -> {
            String arg = "Example argument";
            Component simple = Component.translatable("rich_translations_test.simple_translation", arg);
            Component simpleRich = Component.translatable("rich_translations_test.simple_rich_translation", arg);

            helper.assertTrue(simpleRich.getString().equals(simple.getString()), "Rich translation isn't equivalent to simple translation");

            String translation = Language.getInstance().getOrDefault("rich_translations_test.simple_rich_translation");
            helper.assertTrue(
                    String.format(translation, arg).equals(simpleRich.getString()),
                    "Translatable component isn't equivalent to I18n");

            Component fancy = Component.translatable("rich_translations_test.fancy_rich_translation", arg);
            MutableBoolean foundRed = new MutableBoolean();
            MutableBoolean foundBlue = new MutableBoolean();

            fancy.visit((style, content) -> {
                if (TextColor.fromLegacyFormat(ChatFormatting.RED).equals(style.getColor()) && content.equals("Ooo, colors!"))
                    foundRed.setTrue();

                if (TextColor.fromLegacyFormat(ChatFormatting.BLUE).equals(style.getColor()) && content.equals(arg))
                    foundBlue.setTrue();

                return Optional.empty();
            }, Style.EMPTY);

            helper.assertTrue(foundBlue.isTrue() && foundRed.isTrue(), "Rich translation lost colors");

            helper.succeed();
        });
    }
}
