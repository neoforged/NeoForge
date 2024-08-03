/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.text;

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
import net.minecraft.network.chat.contents.TranslatableFormatException;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public abstract class TemplateParserBase<T> {
    protected static final String TAG_END = ">";
    protected static final String TAG_START = "<";
    protected static final String END_TAG_START = "</";
    protected static final String TEMPLATE_MARKER = "%n";
    protected static final Pattern NUMBER_PATTERN = Pattern.compile("^<([1-9]\\d*|0)>(.*)$");
    protected static final Pattern PLURAL_PATTERN = Pattern.compile("^<([1-9]\\d*|0):plural:(.*)$");
    protected static final Pattern REF_PATTERN = Pattern.compile("^<(?:ref|lang|tr|translate):([^>]+)>(.*)$");
    protected static final Pattern COLOR_PATTERN = Pattern.compile("^<color:(-?[1-9]\\d*|0|#[0-9a-fA-F]{6,8})>(.*)$");
    protected static final Pattern COLORNAME_PATTERN = Pattern.compile("^<color:([a-z_]+)>(.*)$");
    protected static final Pattern KEY_PATTERN = Pattern.compile("^<key:([^>]+)>(.*)$");
    protected static final Pattern FONT_PATTERN = Pattern.compile("^<font:([^>]+)>(.*)$");

    protected static final Map<String, ChatFormatting> FORMAT_MAPPING = new HashMap<>();

    static {
        for (ChatFormatting format : ChatFormatting.values()) {
            FORMAT_MAPPING.put(format.name().toLowerCase(Locale.ENGLISH), format);
        }
        FORMAT_MAPPING.put("b", ChatFormatting.BOLD);
        FORMAT_MAPPING.put("i", ChatFormatting.ITALIC);
        FORMAT_MAPPING.put("em", ChatFormatting.ITALIC);
        FORMAT_MAPPING.put("u", ChatFormatting.UNDERLINE);
        FORMAT_MAPPING.put("underlined", ChatFormatting.UNDERLINE); // MM compat
        FORMAT_MAPPING.put("o", ChatFormatting.OBFUSCATED);
        FORMAT_MAPPING.put("obf", ChatFormatting.OBFUSCATED);
        FORMAT_MAPPING.put("s", ChatFormatting.STRIKETHROUGH);
        FORMAT_MAPPING.put("st", ChatFormatting.STRIKETHROUGH);
        FORMAT_MAPPING.put("grey", ChatFormatting.GRAY);
        FORMAT_MAPPING.put("dark_grey", ChatFormatting.DARK_GRAY);
        FORMAT_MAPPING.remove("reset");
    }

    public static Supplier<com.ibm.icu.util.ULocale> lang = () -> ULocale.US;

    protected String remaining = "";

    protected TemplateParserBase(String remaining) {
        this.remaining = remaining;
    }

    protected static String stripMarker(String template) {
        return template.replaceFirst(TEMPLATE_MARKER + ":? *", "");
    }

    protected void parse(Consumer<T> consumer) throws ParsingException {
        parse().forEach(consumer);
        if (!remaining.isEmpty()) {
            throw new ParsingException("Expected text or valid tag but found: \"" + remaining + "\"");
        }
    }

    /**
     * Parse the remaining string until either the end of the string or an end tag is encountered.
     */
    protected List<T> parse() throws ParsingException {
        List<T> stack = new ArrayList<>();
        OUTER:
        while (!remaining.isEmpty()) {
            if (remaining.startsWith(END_TAG_START)) {
                return stack;
            } else if (remaining.startsWith(TAG_START)) {
                // 1.) Any chat formatting code as a tag, e.g. <red>foo</red>
                for (Entry<String, ChatFormatting> entry : FORMAT_MAPPING.entrySet()) {
                    final String start = TAG_START + entry.getKey() + TAG_END;
                    if (consume(start)) {
                        parseToEndTag(END_TAG_START + entry.getKey() + TAG_END).forEach(v -> stack.add(applyChatFormatting(v, entry.getValue())));
                        continue OUTER;
                    }
                }
                // 2.) A simple group, e.g. <0>
                Matcher match = NUMBER_PATTERN.matcher(remaining);
                if (match.matches()) {
                    final int index = decodeInteger(match.group(1));
                    remaining = match.group(2);
                    stack.add(generateArgument(index));
                    continue OUTER;
                }
                // 3.) A plural sub-pattern, e.g. <0:plural:...>
                match = PLURAL_PATTERN.matcher(remaining);
                if (match.matches()) {
                    final int index = decodeInteger(match.group(1));
                    remaining = match.group(2);
                    parsePluralArgument(index, new Eater(TAG_START, TAG_END).getLeft(), stack::add);
                    if (!consume(TAG_END)) {
                        throw new ParsingException("Expected valid tag but found: \"" + remaining + "\"");
                    }
                    continue OUTER;
                }
                // 4.) A reference, e.g. <ref:other.lang.key>
                match = REF_PATTERN.matcher(remaining);
                if (match.matches()) {
                    final String ref_string = match.group(1);
                    remaining = match.group(2);
                    stack.add(generateReference(ref_string));
                    continue OUTER;
                }
                // 5.) An RGB text color, e.g. <color:#ff0000> or <color:56372>
                match = COLOR_PATTERN.matcher(remaining);
                if (match.matches()) {
                    final int color = decodeColor(match.group(1));
                    remaining = match.group(2);
                    parseToEndTag(END_TAG_START + "color" + TAG_END).forEach(v -> stack.add(applyColor(v, color)));
                    continue OUTER;
                }
                // 5a.) An text color name, e.g. <color:gray>
                match = COLORNAME_PATTERN.matcher(remaining);
                if (match.matches()) {
                    final String color = match.group(1);
                    remaining = match.group(2);
                    ChatFormatting format = FORMAT_MAPPING.get(color);
                    if (format != null && format.isColor()) {
                        parseToEndTag(END_TAG_START + "color" + TAG_END).forEach(v -> stack.add(applyChatFormatting(v, format)));
                        continue OUTER;
                    }
                    throw new ParsingException("Expected color name but found: \"" + color + "\"");
                }
                // 6.) A keybind, e.g. <key:key.jump>
                match = KEY_PATTERN.matcher(remaining);
                if (match.matches()) {
                    final String key_string = match.group(1);
                    remaining = match.group(2);
                    stack.add(generateKeybind(key_string));
                    continue OUTER;
                }
                // 7.) An font, e.g. <font:minecraft:alt>...</font>
                match = FONT_PATTERN.matcher(remaining);
                if (match.matches()) {
                    final ResourceLocation font = decodeFont(match.group(1));
                    remaining = match.group(2);
                    parseToEndTag(END_TAG_START + "font" + TAG_END).forEach(v -> stack.add(applyFont(v, font)));
                    continue OUTER;
                }
                // Reject unknown tags
                throw new ParsingException("Expected valid tag but found: \"" + remaining + "\"");
            } else {
                // else.) Literal text
                stack.add(generateLiteral(new Eater("", TAG_START).getLeft()));
            }
        }
        return stack;
    }

    protected abstract T applyChatFormatting(T element, ChatFormatting formatting) throws ParsingException;

    protected abstract T generateArgument(int index) throws ParsingException;

    protected abstract void parsePluralArgument(int index, String format, Consumer<T> consumer) throws ParsingException;

    protected abstract T generateReference(String reference) throws ParsingException;

    protected abstract T applyColor(T element, int color) throws ParsingException;

    protected abstract T generateKeybind(String keybind) throws ParsingException;

    protected abstract T applyFont(T element, ResourceLocation font) throws ParsingException;

    protected abstract T generateLiteral(String text) throws ParsingException;

    /**
     * Continue parsing the remaining string, then check if the parser stopped at the given end end tag (and consume it).
     */
    protected List<T> parseToEndTag(String end) throws ParsingException {
        List<T> result = parse();
        if (!consume(end)) {
            throw new ParsingException("Expected " + end + " but found: \"" + remaining + "\"");
        }
        return result;
    }

    /**
     * Parse a literal integer, e.g. for group numbers. Throw a {@link TranslatableFormatException} exception if that fails.
     */
    protected int decodeInteger(final String int_string) throws ParsingException {
        try {
            return Integer.valueOf(int_string);
        } catch (NumberFormatException e) {
            throw new ParsingException("Expected a number but found: \"" + int_string + "\"");
        }
    }

    /**
     * Parse an integer for a color value, i.e. with #ff/0xff/07 support. Throw a {@link TranslatableFormatException} exception if that fails.
     */
    protected int decodeColor(final String color_string) throws ParsingException {
        try {
            return Integer.decode(color_string);
        } catch (NumberFormatException e) {
            throw new ParsingException("Expected a color value but found: \"" + color_string + "\"");
        }
    }

    /**
     * Parse {@link ResourceLocation} for a font. Throw a {@link TranslatableFormatException} exception if that fails.
     */
    protected ResourceLocation decodeFont(final String font_string) throws ParsingException {
        final ResourceLocation font = ResourceLocation.tryParse(font_string);
        if (font == null) {
            throw new ParsingException("Expected resource location for font but found: \"" + font_string + "\"");
        }
        return font;
    }

    /**
     * Try to remove the given string from the beginning of the remaining string, return if it succeeded.
     */
    protected boolean consume(String what) {
        if (remaining.startsWith(what)) {
            remaining = remaining.substring(what.length());
            return true;
        } else {
            return false;
        }
    }

    protected static class ParsingException extends RuntimeException {
        // Note: This should extend Exception, but it is thrown through lambdas which cannot have a "throws" declaration
        private static final long serialVersionUID = 6142968319664791595L;

        ParsingException(String message) {
            super(message);
        }
    }

    /**
     * A helper to consume a String to an marker that supports recursively nested markers, like <code>(a(b(c)))</code>, and escaped characters.
     */
    protected class Eater {
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
