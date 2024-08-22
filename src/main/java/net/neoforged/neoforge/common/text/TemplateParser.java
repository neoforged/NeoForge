/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.text;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.locale.Language;
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

    private static Pair<TriFunction<String, Object[], Consumer<FormattedText>, String>, Function<String, String>> getParser(ResourceLocation key) throws ParsingException {
        return PARSERS.computeIfAbsent(key, rl -> {
            throw new ParsingException("Unknown format specified: " + rl);
        });
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
            return getParser(format.getKey()).getLeft().apply(format.getValue(), args, consumer);
        } catch (ParsingException e) {
            LOGGER.error("Error parsing language string for key {} with value '{}': {}", translatableContents.getKey(), template, e.getMessage());
            throw new TranslatableFormatException(translatableContents, e.getMessage());
        }
    }

    @ApiStatus.Internal
    public static String stripTemplate(String key, String template) {
        try {
            Pair<ResourceLocation, String> format = getFormat(template);
            return getParser(format.getKey()).getRight().apply(format.getValue());
        } catch (ParsingException e) {
            LOGGER.error("Error parsing language string for key {} with value '{}': {}", key, template, e.getMessage());
            return template;
        }
    }

    /**
     * Tries to parse all translation values in the currently loaded language that match the given predicate and returns all found errors.
     * 
     * @param filter Predicate on the key.
     * @return A list of pairs, with the translation key in the left and the error message in the right value.
     */
    public static List<Pair<String, String>> test(Predicate<String> filter) {
        return Language.getInstance().getLanguageData().entrySet().stream().filter(entry -> filter.test(entry.getKey())).map(
                entry -> {
                    try {
                        Pair<ResourceLocation, String> format = getFormat(entry.getValue());
                        getParser(format.getKey()).getRight().apply(format.getValue());
                        return null;
                    } catch (ParsingException e) {
                        return Pair.of(entry.getKey(), e.getMessage());
                    }
                }).filter(p -> p != null).toList();
    }

    /**
     * Tries to parse all translation values in the specified language file and returns all found errors.
     * 
     * @param modid    The namespace (mod id) of the language file.
     * @param language The language (e.g. "en_us") of the language file.
     * @return A list of pairs, with the translation key in the left and the error message in the right value.
     */
    public static List<Pair<String, String>> test(String modid, String language) {
        if (!ResourceLocation.isValidNamespace(modid)) {
            return List.of(Pair.of(modid, "Not a valid mod id (directory traversal attack?)"));
        }
        if (!ResourceLocation.isValidNamespace(language)) {
            return List.of(Pair.of(language, "Not a valid language name (directory traversal attack?)"));
        }
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("assets/" + modid + "/lang/" + language + ".json")) {
            assert input != null;
            List<Pair<String, String>> result = new ArrayList<>();
            Language.loadFromJson(input, (key, value) -> {
                try {
                    Pair<ResourceLocation, String> format = getFormat(value);
                    getParser(format.getKey()).getRight().apply(format.getValue());
                } catch (ParsingException e) {
                    result.add(Pair.of(key, e.getMessage()));
                }
            }, (key, value) -> {});
            return result;
        } catch (Exception exception) {
            return List.of(Pair.of(modid + "/" + language, "Failed to read language file: " + exception.getMessage()));
        }
    }
}
