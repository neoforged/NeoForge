/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record InsertingContents(int index) implements ComponentContents {
    public static final MapCodec<InsertingContents> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("index").forGetter(InsertingContents::index))
            .apply(i, InsertingContents::new));

    public static final ComponentContents.Type<InsertingContents> TYPE = new ComponentContents.Type<>(CODEC, "neoforge:inserting");

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> visitor) {
        return visitor.accept("%" + (index + 1) + "$s");
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> visitor, Style style) {
        return visitor.accept(style, "%" + (index + 1) + "$s");
    }

    @Override
    public Type<?> type() {
        return TYPE;
    }
}
