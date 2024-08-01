/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.text;

import java.util.function.Consumer;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;

public class TemplateParser {
    public static java.util.function.Supplier<com.ibm.icu.util.ULocale> lang = () -> com.ibm.icu.util.ULocale.US;

    public static String decomposeTemplate(TranslatableContents translatableContents, Object[] args, String template, Consumer<FormattedText> object) {
        if (template.startsWith("%n")) {
            String s = template.replaceAll("%n:?\\s*", "");
            // processing goes here
            return "";
        }
        return template;
    }
}
