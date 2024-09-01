/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.text;

import com.ibm.icu.text.PluralFormat;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.TranslatableFormatException;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class TemplateParser extends TemplateParserBase<MutableComponent> {
    private final TranslatableContents translatableContents;
    private final Object[] args;

    TemplateParser(TranslatableContents translatableContents, Object[] args, String template) {
        super(template);
        this.translatableContents = translatableContents;
        this.args = args;
    }

    public static String decomposeTemplate(TranslatableContents translatableContents, Object[] args, String template, Consumer<FormattedText> consumer) {
        if (template.startsWith(TEMPLATE_MARKER)) {
            try {
                new TemplateParser(translatableContents, args, stripMarker(template)).parse(consumer::accept);
                return "";
            } catch (ParsingException e) {
                throw new TranslatableFormatException(translatableContents, e.getMessage());
            }
        }
        return template;
    }

    @Override
    protected MutableComponent applyChatFormatting(MutableComponent element, ChatFormatting formatting) {
        return element.withStyle(formatting);
    }

    @Override
    protected MutableComponent generateArgument(int index) {
        return getArgument(index);
    }

    @Override
    protected void parsePluralArgument(int index, String format, Consumer<MutableComponent> consumer) throws ParsingException {
        new TemplateParser(translatableContents, args, new PluralFormat(TemplateParserBase.lang.get(), format).format(getArgumentNumeric(index))).parse(consumer);
    }

    @Override
    protected MutableComponent generateReference(String reference) {
        return Component.translatable(reference);
    }

    @Override
    protected MutableComponent applyColor(MutableComponent element, int color) {
        return element.withColor(color);
    }

    @Override
    protected MutableComponent generateKeybind(String keybind) {
        return MutableComponent.create(new KeybindContents(keybind));
    }

    @Override
    protected MutableComponent applyFont(MutableComponent element, ResourceLocation font) {
        return element.withStyle(Style.EMPTY.withFont(font));
    }

    @Override
    protected MutableComponent generateLiteral(String text) {
        return Component.literal(text);
    }

    /**
     * Get a parameter as {@link MutableComponent}, like {@link TranslatableContents#getArgument(int)}.
     */
    private MutableComponent getArgument(int index) {
        if (index >= 0 && index < args.length) {
            Object object = args[index];
            if (object instanceof MutableComponent) {
                return (MutableComponent) object;
            } else if (object instanceof Component) {
                return Component.empty().append((Component) object);
            } else if (object == null) {
                return Component.literal("null");
            } else {
                return Component.literal(object.toString());
            }
        } else {
            throw new TranslatableFormatException(translatableContents, index);
        }
    }

    /**
     * Get a parameter as double so it's suitable for the plural format.
     */
    private double getArgumentNumeric(int index) {
        if (index >= 0 && index < args.length) {
            Object object = args[index];
            try {
                if (object instanceof Number number) {
                    return number.doubleValue();
                } else if (object instanceof Component component) {
                    return Double.valueOf(component.getString());
                } else if (object == null) {
                    return 0d;
                } else {
                    return Double.valueOf(object.toString());
                }
            } catch (NumberFormatException e) {
                return 0d;
            }
        } else {
            throw new TranslatableFormatException(translatableContents, index);
        }
    }
}
