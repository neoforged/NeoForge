/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.text;

import com.ibm.icu.text.PluralFormat;
import com.ibm.icu.util.ULocale;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class TemplateParser {
    private static final String TAG_END = ">";
    private static final String TAG_START = "<";
    private static final String END_TAG_START = "</";
    private static final String TEMPLATE_MARKER = "%n";
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^<([1-9]\\d*|0)>(.*)$");
    private static final Pattern PLURAL_PATTERN = Pattern.compile("^<([1-9]\\d*|0):plural:(.*)$");
    private static final Pattern REF_PATTERN = Pattern.compile("^<ref:([^>]+)>(.*)$");
    private static final Pattern COLOR_PATTERN = Pattern.compile("^<color:(-?[1-9]\\d*|0|#[0-9a-fA-F]{6,8})>(.*)$");
    private static final Pattern KEY_PATTERN = Pattern.compile("^<key:([^>]+)>(.*)$");
    private static final Pattern FONT_PATTERN = Pattern.compile("^<font:([^>]+)>(.*)$");

    private static final Map<String, ChatFormatting> FORMAT_MAPPING = new HashMap<>();

    static {
        for (ChatFormatting format : ChatFormatting.values()) {
            FORMAT_MAPPING.put(format.name().toLowerCase(Locale.ENGLISH), format);
        }
        FORMAT_MAPPING.put("b", ChatFormatting.BOLD);
        FORMAT_MAPPING.put("i", ChatFormatting.ITALIC);
        FORMAT_MAPPING.put("em", ChatFormatting.ITALIC);
        FORMAT_MAPPING.put("u", ChatFormatting.UNDERLINE);
        FORMAT_MAPPING.put("o", ChatFormatting.OBFUSCATED);
        FORMAT_MAPPING.put("obf", ChatFormatting.OBFUSCATED);
        FORMAT_MAPPING.put("s", ChatFormatting.STRIKETHROUGH);
        FORMAT_MAPPING.put("st", ChatFormatting.STRIKETHROUGH);
        FORMAT_MAPPING.put("grey", ChatFormatting.GRAY);
        FORMAT_MAPPING.put("dark_grey", ChatFormatting.DARK_GRAY);
    }

    public static Supplier<com.ibm.icu.util.ULocale> lang = () -> ULocale.US;

    private final TranslatableContents translatableContents;
    private final Object[] args;
    private String remaining = "";

    TemplateParser(TranslatableContents translatableContents, Object[] args, String template) {
        this.translatableContents = translatableContents;
        this.args = args;
        this.remaining = template;
    }

    public static String decomposeTemplate(TranslatableContents translatableContents, Object[] args, String template, Consumer<FormattedText> consumer) {
        if (template.startsWith(TEMPLATE_MARKER)) {
            new TemplateParser(translatableContents, args, template.replaceFirst(TEMPLATE_MARKER + ":?\\s*", "")).parse(consumer::accept);
            return "";
        }
        return template;
    }

    private void parse(Consumer<MutableComponent> consumer) {
        parse().forEach(consumer);
        if (!remaining.isEmpty()) {
            throw new TranslatableFormatException(translatableContents, "Expected text or valid tag but found: \"" + remaining + "\"");
        }
    }

    /**
     * Parse the remaining string until either the end of the string or an end tag is encountered.
     */
    private List<MutableComponent> parse() {
        List<MutableComponent> stack = new ArrayList<>();
        OUTER:
        while (!remaining.isEmpty()) {
            if (remaining.startsWith(END_TAG_START)) {
                return stack;
            } else if (remaining.startsWith(TAG_START)) {
                // 1.) Any chat formatting code as a tag, e.g. <red>foo</red>
                for (Entry<String, ChatFormatting> entry : FORMAT_MAPPING.entrySet()) {
                    final String start = TAG_START + entry.getKey() + TAG_END;
                    if (consume(start)) {
                        parseToEndTag(END_TAG_START + entry.getKey() + TAG_END).forEach(v -> stack.add(v.withStyle(entry.getValue())));
                        continue OUTER;
                    }
                }
                // 2.) A simple group, e.g. <0>
                Matcher match = NUMBER_PATTERN.matcher(remaining);
                if (match.matches()) {
                    final int index = decodeInteger(match.group(1));
                    remaining = match.group(2);
                    stack.add(getArgument(index));
                    continue OUTER;
                }
                // 3.) A plural sub-pattern, e.g. <0:plural:...>
                match = PLURAL_PATTERN.matcher(remaining);
                if (match.matches()) {
                    final int index = decodeInteger(match.group(1));
                    remaining = match.group(2);
                    new TemplateParser(translatableContents, args, new PluralFormat(lang.get(), new Eater(TAG_START, TAG_END).getLeft()).format(getArgumentNumeric(index))).parse(stack::add);
                    if (!consume(TAG_END)) {
                        throw new TranslatableFormatException(translatableContents, "Expected valid tag but found: \"" + remaining + "\"");
                    }
                    continue OUTER;
                }
                // 4.) A reference, e.g. <ref:other.lang.key>
                match = REF_PATTERN.matcher(remaining);
                if (match.matches()) {
                    final String ref_string = match.group(1);
                    remaining = match.group(2);
                    stack.add(Component.translatable(ref_string));
                    continue OUTER;
                }
                // 5.) An RGB text color, e.g. <color #ff0000> or <color 56372>
                match = COLOR_PATTERN.matcher(remaining);
                if (match.matches()) {
                    final int color = decodeColor(match.group(1));
                    remaining = match.group(2);
                    parseToEndTag(END_TAG_START + "color" + TAG_END).forEach(v -> stack.add(v.withColor(color)));
                    continue OUTER;
                }
                // 6.) A keybind, e.g. <key:key.jump>
                match = KEY_PATTERN.matcher(remaining);
                if (match.matches()) {
                    final String key_string = match.group(1);
                    remaining = match.group(2);
                    stack.add(MutableComponent.create(new KeybindContents(key_string)));
                    continue OUTER;
                }
                // 7.) An font, e.g. <font:minecraft:alt>...</font>
                match = FONT_PATTERN.matcher(remaining);
                if (match.matches()) {
                    final ResourceLocation font = decodeFont(match.group(1));
                    remaining = match.group(2);
                    parseToEndTag(END_TAG_START + "font" + TAG_END).forEach(v -> stack.add(v.withStyle(Style.EMPTY.withFont(font))));
                    continue OUTER;
                }
                // Reject unknown tags
                throw new TranslatableFormatException(translatableContents, "Expected valid tag but found: \"" + remaining + "\"");
            } else {
                // else.) Literal text
                stack.add(Component.literal(new Eater("", TAG_START).getLeft()));
            }
        }
        return stack;
    }

    /**
     * Try to remove the given string from the beginning of the remaining string, return if it succeeded.
     */
    private boolean consume(String what) {
        if (remaining.startsWith(what)) {
            remaining = remaining.substring(what.length());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Continue parsing the remaining string, then check if the parser stopped at the given end end tag (and consume it).
     */
    private List<MutableComponent> parseToEndTag(String end) {
        List<MutableComponent> result = parse();
        if (!consume(end)) {
            throw new TranslatableFormatException(translatableContents, "Expected " + end + " but found: \"" + remaining + "\"");
        }
        return result;
    }

    /**
     * Parse a literal integer, e.g. for group numbers. Throw a {@link TranslatableFormatException} exception if that fails.
     */
    private int decodeInteger(final String int_string) {
        try {
            return Integer.valueOf(int_string);
        } catch (NumberFormatException e) {
            throw new TranslatableFormatException(translatableContents, "Expected a number but found: \"" + int_string + "\"");
        }
    }

    /**
     * Parse an integer for a color value, i.e. with #ff/0xff/07 support. Throw a {@link TranslatableFormatException} exception if that fails.
     */
    private int decodeColor(final String color_string) {
        try {
            return Integer.decode(color_string);
        } catch (NumberFormatException e) {
            throw new TranslatableFormatException(translatableContents, "Expected a color value but found: \"" + color_string + "\"");
        }
    }

    /**
     * Parse {@link ResourceLocation} for a font. Throw a {@link TranslatableFormatException} exception if that fails.
     */
    private ResourceLocation decodeFont(final String font_string) {
        final ResourceLocation font = ResourceLocation.tryParse(font_string);
        if (font == null) {
            throw new TranslatableFormatException(translatableContents, "Expected resource location for font but found: \"" + font_string + "\"");
        }
        return font;
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

    /**
     * A helper to consume a String to an marker that supports recursively nested markers, like <code>(a(b(c)))</code>, and escaped characters.
     */
    private class Eater {
        private String left = "";

        Eater(String begin, String end) {
            eat(begin, end, false);
        }

        String getLeft() {
            return left;
        }

        private void eat(String begin, String end, boolean consume) {
            while (!remaining.isEmpty()) {
                if (remaining.length() >= 2 && remaining.startsWith("\\")) {
                    remaining = remaining.substring(1);
                    eat(1);
                } else if (remaining.startsWith(end)) {
                    if (consume) {
                        eat(end.length());
                    }
                    return;
                } else if (!begin.isEmpty() && remaining.startsWith(begin)) {
                    eat(begin.length());
                    eat(begin, end, true);
                } else {
                    eat(1);
                }
            }
        }

        private void eat(int amount) {
            for (int i = 0; i < amount; i++) {
                left += remaining.charAt(0);
                remaining = remaining.substring(1);
            }
        }
    }
}
