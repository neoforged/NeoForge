/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.ApiStatus;

/**
 * Flag data attachment, used for saving/loading flag states to/from disk
 * <p>
 * Not to be used by modders.
 */
@ApiStatus.Internal
public record FlagAttachment(Object2BooleanMap<ResourceLocation> flags) {
    public static final Codec<FlagAttachment> CODEC = ExtraCodecs.object2BooleanMap(ResourceLocation.CODEC).xmap(FlagAttachment::new, FlagAttachment::flags);
}
