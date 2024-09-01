/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.text;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class TemplateStripper extends TemplateParserBase<String> {
    public TemplateStripper(String remaining) {
        super(remaining);
    }

    public static String strip(String template) {
        if (template.startsWith(TEMPLATE_MARKER_JSON)) {
            try {
                StringBuilder result = new StringBuilder();
                new TemplateStripper(stripMarker(template)).parseJson(result::append);
                return result.toString();
            } catch (ParsingException e) {
                // NOP
            }
        }
        return template;
    }

    @Override
    protected void consumeComponent(Component component, Consumer<String> consumer) throws ParsingException {
        consumer.accept(component.getString());
    }
}
