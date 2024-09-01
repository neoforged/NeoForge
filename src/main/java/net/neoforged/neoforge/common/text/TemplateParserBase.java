/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.text;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public abstract class TemplateParserBase<T> {
    protected static final String TEMPLATE_MARKER_JSON = "%j";
    protected static final Gson GSON = new Gson();

    protected String remaining = "";

    protected TemplateParserBase(String remaining) {
        this.remaining = remaining;
    }

    protected static String stripMarker(String template) {
        return template.replaceFirst(TEMPLATE_MARKER_JSON + ":? *", "");
    }

    protected void parseJson(Consumer<T> consumer) throws ParsingException {
        try {
            consumeComponent(net.minecraft.network.chat.ComponentSerialization.CODEC
                    .parse(com.mojang.serialization.JsonOps.INSTANCE, GSON.fromJson(remaining, JsonArray.class))
                    .getOrThrow(msg -> new ParsingException("Error parsing translation for " + remaining + ": " + msg)), consumer);
            remaining = "";
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            new ParsingException("Error parsing translation for " + remaining + ": " + e.getMessage());
        }
    }

    protected abstract void consumeComponent(Component component, Consumer<T> consumer) throws ParsingException;

    protected static class ParsingException extends RuntimeException {
        // Note: This should extend Exception, but it is thrown through lambdas which cannot have a "throws" declaration
        private static final long serialVersionUID = 6142968319664791595L;

        ParsingException(String message) {
            super(message);
        }
    }
}
