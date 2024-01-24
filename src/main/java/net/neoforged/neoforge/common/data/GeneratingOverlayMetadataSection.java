/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

/**
 * Can be used in place of {@link OverlayMetadataSection} during datagen if you wish to generate conditions.
 */
public record GeneratingOverlayMetadataSection(List<WithConditions<OverlayMetadataSection.OverlayEntry>> overlays) {
    private static final Codec<GeneratingOverlayMetadataSection> CODEC = RecordCodecBuilder.create(i -> i.group(
            NeoForgeExtraCodecs.listWithOptionalElements(
                    ConditionalOps.createConditionalCodecWithConditions(OverlayMetadataSection.OverlayEntry.CODEC)).fieldOf("entries").forGetter(GeneratingOverlayMetadataSection::overlays))
            .apply(i, GeneratingOverlayMetadataSection::new));
    public static final MetadataSectionType<GeneratingOverlayMetadataSection> TYPE = MetadataSectionType.fromCodec("overlays", CODEC);
}
