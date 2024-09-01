/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.text;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.TranslatableFormatException;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class TemplateParser extends TemplateParserBase<FormattedText> {
    TemplateParser(String template) {
        super(template);
    }

    public static String decomposeTemplate(TranslatableContents translatableContents, Object[] args, String template, Consumer<FormattedText> consumer) {
        if (template.startsWith(TEMPLATE_MARKER_JSON)) {
            try {
                new TemplateParser(stripMarker(template)).parseJson(consumer);
                return "";
            } catch (ParsingException e) {
                throw new TranslatableFormatException(translatableContents, e.getMessage());
            }
        }
        return template;
    }

    @Override
    protected void consumeComponent(Component component, Consumer<FormattedText> consumer) throws ParsingException {
        consumer.accept(component);
    }
}
