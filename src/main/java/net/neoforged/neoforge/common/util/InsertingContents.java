/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.ApiStatus;

public record InsertingContents(int index) implements ComponentContents {
    public static final MapCodec<InsertingContents> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("index").forGetter(InsertingContents::index))
            .apply(i, InsertingContents::new));

    public static final ComponentContents.Type<InsertingContents> TYPE = new ComponentContents.Type<>(CODEC, "inserting");

    private static final ThreadLocal<Deque<TranslatableContents>> translationStack = ThreadLocal.withInitial(ArrayDeque::new);

    @ApiStatus.Internal
    public static boolean pushTranslation(TranslatableContents contents) {
        for (TranslatableContents other : translationStack.get()) {
            if (contents == other) {
                return false;
            }
        }

        translationStack.get().push(contents);
        return true;
    }

    @ApiStatus.Internal
    public static void popTranslation() {
        translationStack.get().pop();
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> visitor) {
        var translation = translationStack.get().peek();

        if (translation == null || translation.getArgs().length <= index)
            return visitor.accept("%" + (index + 1) + "$s");

        Object arg = translation.getArgs()[index];

        if (arg instanceof Component component) {
            return component.visit(visitor);
        } else {
            return visitor.accept(arg.toString());
        }
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> visitor, Style style) {
        var translation = translationStack.get().peek();

        if (translation == null || translation.getArgs().length <= index)
            return visitor.accept(style, "%" + (index + 1) + "$s");

        Object arg = translation.getArgs()[index];

        if (arg instanceof Component component) {
            return component.visit(visitor, style);
        } else {
            return visitor.accept(style, arg.toString());
        }
    }

    @Override
    public Type<?> type() {
        return TYPE;
    }
}
