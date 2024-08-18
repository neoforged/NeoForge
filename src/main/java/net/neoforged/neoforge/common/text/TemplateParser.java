/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.text;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.TranslatableFormatException;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

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

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String TEMPLATE_MARKER = "%n";
    private static final Pattern MARKER_PATTERN = Pattern.compile("^" + TEMPLATE_MARKER + "\\(([a-z0-9_.-]+:[a-z0-9_.-]+)\\) *(.*)$");
    private static final ResourceLocation SJSON = ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "sjson");
    private static final ResourceLocation VANILLA = ResourceLocation.withDefaultNamespace("default");

    private static final Map<ResourceLocation, Pair<TriFunction<String, Object[], Consumer<FormattedText>, String>, Function<String, String>>> PARSERS = new HashMap<>();

    static {
        register(VANILLA, (template, args, consumer) -> template, template -> template);
        register(SJSON, JsonTemplateParser::handle, JsonTemplateParser::handle);
    }

    /**
     * Registers custom format parsers for the given {@link ResourceLocation}.
     * 
     * @param id         The {@link ResourceLocation} as present in the <code>%n(...)</code> marker.
     * @param decomposer A function to handle converting the raw translation text into {@link FormattedText}s. Can throw {@link ParsingException} to report errors.
     *                   Note that this is responsible for inserting any parameters that may be there. The returned string will be handed over to vanilla parsing, so
     *                   this function also can restrict itself to transform the translation string.
     * @param stripper   A function to handle converting the raw translation into format-free texts. Can throw {@link ParsingException} to report errors.
     *                   Note that this shall convert any parameters into their vanilla form ("%1$s"...).
     * @return <code>true</code> if the parser was registered, <code>false</code> otherwise.
     */
    public static boolean register(ResourceLocation id, TriFunction<String, Object[], Consumer<FormattedText>, String> decomposer, Function<String, String> stripper) {
        synchronized (PARSERS) {
            return PARSERS.putIfAbsent(id, Pair.of(decomposer, stripper)) == null;
        }
    }

    @ApiStatus.Internal
    protected static Pair<ResourceLocation, String> getFormat(String template) {
        if (template.startsWith(TEMPLATE_MARKER)) {
            Matcher match = MARKER_PATTERN.matcher(template);
            if (match.matches()) {
                return Pair.of(ResourceLocation.parse(match.group(1)), match.group(2));
            } else {
                return Pair.of(SJSON, template.replaceFirst(TEMPLATE_MARKER, ""));
            }
        } else {
            return Pair.of(VANILLA, template);
        }
    }

    @ApiStatus.Internal
    public static String decomposeTemplate(TranslatableContents translatableContents, Object[] args, String template, Consumer<FormattedText> consumer) {
        try {
            Pair<ResourceLocation, String> format = getFormat(template);
            return PARSERS.computeIfAbsent(format.getKey(), rl -> {
                throw new TemplateParser.ParsingException("Unknown format specified: " + rl);
            }).getLeft().apply(format.getValue(), args, consumer);
        } catch (TemplateParser.ParsingException e) {
            LOGGER.error("Error parsing language string for key {} with value '{}': {}", translatableContents.getKey(), template, e.getMessage());
            throw new TranslatableFormatException(translatableContents, e.getMessage());
        }
    }

    @ApiStatus.Internal
    public static String stripTemplate(String key, String template) {
        try {
            Pair<ResourceLocation, String> format = getFormat(template);
            return PARSERS.computeIfAbsent(format.getKey(), rl -> {
                throw new TemplateParser.ParsingException("Unknown format specified: " + rl);
            }).getRight().apply(format.getValue());
        } catch (TemplateParser.ParsingException e) {
            LOGGER.error("Error parsing language string for key {} with value '{}': {}", key, template, e.getMessage());
            return template;
        }
    }
}
