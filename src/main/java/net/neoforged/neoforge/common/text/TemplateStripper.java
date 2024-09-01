/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.text;

import com.ibm.icu.text.PluralFormat;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class TemplateStripper extends TemplateParserBase<String> {
    public TemplateStripper(String remaining) {
        super(remaining);
    }

    public static String strip(String template) {
        if (template.startsWith(TEMPLATE_MARKER)) {
            try {
                StringBuilder result = new StringBuilder();
                new TemplateStripper(stripMarker(template)).parse(result::append);
                return result.toString();
            } catch (ParsingException e) {
                // NOP
            }
        }
        return template;
    }

    @Override
    protected String applyChatFormatting(String element, ChatFormatting formatting) throws ParsingException {
        return element;
    }

    @Override
    protected String generateArgument(int index) throws ParsingException {
        return "%" + (index + 1) + "$s";
    }

    @Override
    protected void parsePluralArgument(int index, String format, Consumer<String> consumer) throws ParsingException {
        new TemplateStripper(new PluralFormat(TemplateParserBase.lang.get(), format).format(99999)).parse(s -> consumer.accept(s.replace("99999", generateArgument(index))));
    }

    @Override
    protected String generateReference(String reference) throws ParsingException {
        return Language.getInstance().getOrDefault(reference);
    }

    @Override
    protected String applyColor(String element, int color) throws ParsingException {
        return element;
    }

    @Override
    protected String generateKeybind(String keybind) throws ParsingException {
        return MutableComponent.create(new KeybindContents(keybind)).getString();
    }

    @Override
    protected String applyFont(String element, ResourceLocation font) throws ParsingException {
        return element;
    }

    @Override
    protected String generateLiteral(String text) throws ParsingException {
        return text;
    }
}
