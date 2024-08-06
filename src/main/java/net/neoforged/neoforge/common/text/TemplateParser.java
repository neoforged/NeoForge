/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.text;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.TranslatableFormatException;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class TemplateParser {
    /**
     * Indicates that parsing has failed and that the raw string value shall be used instead.
     */
    public static class ParsingException extends RuntimeException {
        // Note: This should extend Exception, but it is thrown through lambdas which cannot have a "throws" declaration
        private static final long serialVersionUID = 6142968319664791595L;

        ParsingException(String message) {
            super(message);
        }
    }

    private static final String TEMPLATE_MARKER = "%j";
    private static final Pattern MARKER_PATTERN = Pattern.compile("^" + TEMPLATE_MARKER + "\\(([a-z0-9_.-]+:[a-z0-9_.-]+)\\) *(.*)$");
    private static final ResourceLocation DEFAULT = ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "default");

    private static final Map<ResourceLocation, Pair<TriFunction<String, Object[], Consumer<FormattedText>, String>, BiConsumer<String, Consumer<String>>>> PARSERS = new HashMap<>();

    static {
        register(DEFAULT, JsonTemplateParser::handle, JsonTemplateParser::handle);
    }

    /**
     * Registers custom format parsers for the given {@link ResourceLocation}.
     * 
     * @param id         The {@link ResourceLocation} as present in the <code>%j(...)</code> marker.
     * @param decomposer A function to handle converting the raw translation text into {@link FormattedText}s. Can throw {@link ParsingException} to report errors.
     *                   Note that this is responsible for inserting any parameters that may be there. The returned string will be handed over to vanilla parsing, so
     *                   this function also can restrict itself to transform the translation string.
     * @param stripper   A function to handle converting the raw translation into format-free texts. Can throw {@link ParsingException} to report errors.
     *                   Note that this shall convert any parameters into their vanilla form ("%1$s"...).
     * @return <code>true</code> if the parser was registered, <code>false</code> otherwise.
     */
    public static boolean register(ResourceLocation id, TriFunction<String, Object[], Consumer<FormattedText>, String> decomposer, BiConsumer<String, Consumer<String>> stripper) {
        synchronized (PARSERS) {
            return PARSERS.putIfAbsent(id, Pair.of(decomposer, stripper)) == null;
        }
    }

    @Nullable
    @ApiStatus.Internal
    protected static Pair<ResourceLocation, String> getFormat(String template) {
        if (template.startsWith(TEMPLATE_MARKER)) {
            Matcher match = MARKER_PATTERN.matcher(template);
            if (match.matches()) {
                return Pair.of(ResourceLocation.parse(match.group(1)), match.group(2));
            } else {
                return Pair.of(DEFAULT, template.replaceFirst(TEMPLATE_MARKER, ""));
            }
        } else {
            return null;
        }
    }

    @ApiStatus.Internal
    public static String decomposeTemplate(TranslatableContents translatableContents, Object[] args, String template, Consumer<FormattedText> consumer) {
        @Nullable
        Pair<ResourceLocation, String> format = getFormat(template);
        if (format != null) {
            try {
                return PARSERS.computeIfAbsent(format.getKey(), rl -> {
                    throw new TemplateParser.ParsingException("Unknown format specified: " + rl);
                }).getLeft().apply(format.getValue(), args, consumer);
            } catch (TemplateParser.ParsingException e) {
                e.printStackTrace();
                throw new TranslatableFormatException(translatableContents, e.getMessage());
            }
        }
        return template;
    }

    @ApiStatus.Internal
    public static String stripTemplate(String template) {
        @Nullable
        Pair<ResourceLocation, String> format = getFormat(template);
        if (format != null) {
            try {
                StringBuilder result = new StringBuilder();
                PARSERS.computeIfAbsent(format.getKey(), rl -> {
                    throw new TemplateParser.ParsingException("Unknown format specified: " + rl);
                }).getRight().accept(format.getValue(), result::append);
                return result.toString();
            } catch (TemplateParser.ParsingException e) {
                e.printStackTrace();
                // NOP
            }
        }
        return template;
    }
}
