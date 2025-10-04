/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

/**
 * Can be used in place of {@link OverlayMetadataSection} during datagen if you wish to generate conditions.
 */
public record GeneratingOverlayMetadataSection(List<WithConditions<OverlayMetadataSection.OverlayEntry>> overlays) {
    private static final Codec<GeneratingOverlayMetadataSection> CLIENT_CODEC = codec(PackType.CLIENT_RESOURCES);
    private static final Codec<GeneratingOverlayMetadataSection> SERVER_CODEC = codec(PackType.SERVER_DATA);
    private static final MetadataSectionType<GeneratingOverlayMetadataSection> CLIENT_TYPE = new MetadataSectionType<>("overlays", CLIENT_CODEC);
    private static final MetadataSectionType<GeneratingOverlayMetadataSection> SERVER_TYPE = new MetadataSectionType<>("overlays", SERVER_CODEC);
    private static final MetadataSectionType<GeneratingOverlayMetadataSection> CLIENT_NEOFORGE_TYPE = new MetadataSectionType<>("neoforge:overlays", CLIENT_CODEC);
    private static final MetadataSectionType<GeneratingOverlayMetadataSection> SERVER_NEOFORGE_TYPE = new MetadataSectionType<>("neoforge:overlays", SERVER_CODEC);

    public static MetadataSectionType<GeneratingOverlayMetadataSection> type(PackType packType) {
        return switch (packType) {
            case CLIENT_RESOURCES -> CLIENT_TYPE;
            case SERVER_DATA -> SERVER_TYPE;
        };
    }

    public static MetadataSectionType<GeneratingOverlayMetadataSection> neoforgeType(PackType packType) {
        return switch (packType) {
            case CLIENT_RESOURCES -> CLIENT_NEOFORGE_TYPE;
            case SERVER_DATA -> SERVER_NEOFORGE_TYPE;
        };
    }

    private static Codec<GeneratingOverlayMetadataSection> codec(PackType packType) {
        int lastPreMinorVersion = PackFormat.lastPreMinorVersion(packType);
        Codec<OverlayMetadataSection.OverlayEntry> entryCodec = OverlayMetadataSection.OverlayEntry.IntermediateEntry.CODEC.flatXmap(
                entry -> DataResult.error(() -> "Cannot decode with this codec"),
                entry -> DataResult.success(new OverlayMetadataSection.OverlayEntry.IntermediateEntry(
                        PackFormat.IntermediaryFormat.fromRange(entry.format(), lastPreMinorVersion), entry.overlay())));
        return NeoForgeExtraCodecs.listWithOptionalElements(ConditionalOps.createConditionalCodecWithConditions(entryCodec))
                .fieldOf("entries")
                .xmap(GeneratingOverlayMetadataSection::new, GeneratingOverlayMetadataSection::overlays)
                .codec();
    }
}
