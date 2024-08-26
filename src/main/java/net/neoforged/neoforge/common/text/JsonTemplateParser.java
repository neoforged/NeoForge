/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.text;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.common.util.InsertingContents;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class JsonTemplateParser {
    protected static final Gson GSON = new Gson();

    public static String handle(final String template, final Object[] args, final Consumer<FormattedText> consumer) {
        try {
            process(parse(GSON.fromJson(template, JsonArray.class)), args, consumer);
            return "";
        } catch (JsonParseException e) {
            throw new TemplateParser.ParsingException(e.getMessage());
        }
    }

    private static void process(final Component component, final Object[] args, final Consumer<FormattedText> consumer) {
        if (component.getContents() instanceof InsertingContents icon) {
            consumer.accept(getArgument(args, icon.index()).withStyle(component.getStyle()));
        } else if (!component.getSiblings().isEmpty()) {
            consumer.accept(MutableComponent.create(component.getContents()).withStyle(component.getStyle()));
        } else {
            consumer.accept(component);
        }
        for (final Component sibling : component.getSiblings()) {
            process(sibling, args, consumer);
        }
    }

    public static String handle(final String template) {
        try {
            return parse(GSON.fromJson(template, JsonArray.class)).getString();
        } catch (JsonParseException e) {
            throw new TemplateParser.ParsingException(e.getMessage());
        }
    }

    private static Component parse(JsonElement element) {
        return ComponentSerialization.CODEC
                .parse(JsonOps.INSTANCE, element)
                .getOrThrow(msg -> new JsonParseException("Error parsing json: " + msg));
    }

    private static MutableComponent getArgument(final Object[] args, final int index) {
        if (index >= 0 && index < args.length) {
            final Object object = args[index];
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
            throw new TemplateParser.ParsingException("Invalid index: " + index);
        }
    }

    public static String reencodeJson(JsonElement element) {
        return TemplateParser.JSON_MARKER + GSON.toJson(element);
    }
}
