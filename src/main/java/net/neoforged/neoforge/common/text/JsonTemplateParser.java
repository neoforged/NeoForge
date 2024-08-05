/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.text;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class JsonTemplateParser {
    protected static final Gson GSON = new Gson();

    public static void handle(String template, Object[] args, Consumer<FormattedText> consumer) {
        try {
            consumer.accept(parse(template));
        } catch (JsonSyntaxException e) {
            throw new TemplateParser.ParsingException("Error parsing translation for " + template + ": " + e.getMessage());
        }
    }

    public static void handle(String template, Consumer<String> consumer) {
        try {
            consumer.accept(parse(template).getString());
        } catch (JsonSyntaxException e) {
            throw new TemplateParser.ParsingException("Error parsing translation for " + template + ": " + e.getMessage());
        }
    }

    private static Component parse(String template) {
        return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, GSON.fromJson(template, JsonArray.class)).getOrThrow(msg -> new TemplateParser.ParsingException("Error parsing translation for " + template + ": " + msg));
    }
}
