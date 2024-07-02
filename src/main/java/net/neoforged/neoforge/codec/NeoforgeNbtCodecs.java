/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public class NeoforgeNbtCodecs {
    public static final Codec<Tag> TAG_CODEC = Codec.PASSTHROUGH.comapFlatMap(
            serialized -> {
                Tag tag = serialized.convert(NbtOps.INSTANCE).getValue();
                return tag instanceof Tag nbt
                        ? DataResult.success(nbt == serialized.getValue() ? tag.copy() : tag)
                        : DataResult.error(() -> "Did not deserialize into the proper type.");
            },
            toDeserialize -> new Dynamic<>(NbtOps.INSTANCE, toDeserialize));
}
