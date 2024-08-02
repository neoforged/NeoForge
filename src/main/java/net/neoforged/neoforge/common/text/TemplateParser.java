/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.text;

import com.ibm.icu.text.PluralFormat;
import com.ibm.icu.util.ULocale;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.TranslatableFormatException;
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
    private static final Pattern COLOR_PATTERN = Pattern.compile("^<color\\s+(-?[1-9]\\d*|0|#[0-9a-fA-F]{6,8})>(.*)$");

    public static Supplier<com.ibm.icu.util.ULocale> lang = () -> ULocale.US;

    private final TranslatableContents translatableContents;
    private final Object[] args;

    TemplateParser(TranslatableContents translatableContents, Object[] args) {
        this.translatableContents = translatableContents;
        this.args = args;
    }

    public static String decomposeTemplate(TranslatableContents translatableContents, Object[] args, String template, Consumer<FormattedText> consumer) {
        if (template.startsWith(TEMPLATE_MARKER)) {
            new TemplateParser(translatableContents, args).parse(template.replaceFirst(TEMPLATE_MARKER + ":?\\s*", ""), consumer);
            return "";
        }
        return template;
    }

    private void parse(String template, Consumer<FormattedText> consumer) {
        List<MutableComponent> inner = new ArrayList<>();
        String remains = parse(template, inner);
        if (!remains.isEmpty()) {
            throw new TranslatableFormatException(translatableContents, "Expected text or valid tag but found: \"" + remains + "\"");
        }
        inner.forEach(consumer);
    }

    private String parse(String partial, List<MutableComponent> stack) {
        OUTER:
        while (!partial.isEmpty()) {
            if (partial.startsWith(END_TAG_START)) {
                return partial;
            } else if (partial.startsWith(TAG_START)) {
                // 1.) Any chat formatting code as a tag, e.g. <red>foo</red>
                for (ChatFormatting format : ChatFormatting.values()) {
                    final String key = format.name().toLowerCase(Locale.ENGLISH);
                    final String start = TAG_START + key + TAG_END;
                    if (partial.startsWith(start)) {
                        List<MutableComponent> inner = new ArrayList<>();
                        partial = parse(partial.substring(start.length()), inner);
                        final String end = END_TAG_START + key + TAG_END;
                        if (partial.startsWith(end)) {
                            partial = partial.substring(end.length());
                            inner.forEach(v -> stack.add(v.withStyle(format)));
                            continue OUTER;
                        } else {
                            throw new TranslatableFormatException(translatableContents, "Expected " + end + " but found: \"" + partial + "\"");
                        }
                    }
                }
                // 2.) A simple group, e.g. <0>
                Matcher match = NUMBER_PATTERN.matcher(partial);
                if (match.matches()) {
                    final String number_string = match.group(1);
                    final int index = Integer.valueOf(number_string);
                    stack.add(getArgument(index));
                    partial = match.group(2);
                    continue OUTER;
                }
                // 3.) A plural sub-pattern, e.g. <0:plural:...>
                match = PLURAL_PATTERN.matcher(partial);
                if (match.matches()) {
                    final String number_string = match.group(1);
                    final int index = Integer.valueOf(number_string);
                    final String plural_string = match.group(2);
                    final Eater eater = new Eater(plural_string, TAG_START, TAG_END);
                    if (!eater.getRight().startsWith(TAG_END)) {
                        throw new TranslatableFormatException(translatableContents, "Expected valid tag but found: \"" + partial + "\"");
                    }
                    parse(new PluralFormat(lang.get(), eater.getLeft()).format(getArgumentNumeric(index)), stack);
                    partial = eater.getRight().substring(1);
                    continue OUTER;
                }
                // 4.) A reference, e.g. <ref:other.lang.key>
                match = REF_PATTERN.matcher(partial);
                if (match.matches()) {
                    final String ref_string = match.group(1);
                    stack.add(Component.translatable(ref_string));
                    partial = match.group(2);
                    continue OUTER;
                }
                // 5.) An RGB text color, e.g. <color #ff0000> or <color 56372>
                match = COLOR_PATTERN.matcher(partial);
                if (match.matches()) {
                    final String color_string = match.group(1);
                    final int color = decodeColor(color_string);
                    List<MutableComponent> inner = new ArrayList<>();
                    partial = parse(match.group(2), inner);
                    final String end = END_TAG_START + "color" + TAG_END;
                    if (partial.startsWith(end)) {
                        partial = partial.substring(end.length());
                        inner.forEach(v -> stack.add(v.withColor(color)));
                        continue OUTER;
                    } else {
                        throw new TranslatableFormatException(translatableContents, "Expected " + end + " but found: \"" + partial + "\"");
                    }
                }
                throw new TranslatableFormatException(translatableContents, "Expected valid tag but found: \"" + partial + "\"");
            } else {
                // else.) Literal text
                final Eater eater = new Eater(partial, "", TAG_START);
                stack.add(Component.literal(eater.getLeft()));
                partial = eater.getRight();
            }
        }
        return partial;
    }

    private int decodeColor(final String color_string) {
        try {
            return Integer.decode(color_string);
        } catch (NumberFormatException e) {
            throw new TranslatableFormatException(translatableContents, "Expected a color value but found: \"" + color_string + "\"");
        }
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
    private static class Eater {
        private String left = "";
        private String right;

        Eater(String right, String begin, String end) {
            this.right = right;
            eat(begin, end, false);
        }

        String getLeft() {
            return left;
        }

        String getRight() {
            return right;
        }

        private void eat(String begin, String end, boolean consume) {
            while (!right.isEmpty()) {
                if (right.length() >= 2 && right.startsWith("\\")) {
                    right = right.substring(1);
                    eat(1);
                } else if (right.startsWith(end)) {
                    if (consume) {
                        eat(end.length());
                    }
                    return;
                } else if (!begin.isEmpty() && right.startsWith(begin)) {
                    eat(begin.length());
                    eat(begin, end, true);
                } else {
                    eat(1);
                }
            }
        }

        private void eat(int amount) {
            for (int i = 0; i < amount; i++) {
                left += right.charAt(0);
                right = right.substring(1);
            }
        }
    }
}
