/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.LinkedHashSet;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/// The identity and semantics of a {@link TooltipNode}: its plain tags (many allowed), its single negotiated tag
/// (at most one), the mod that produced the content, and a declaration ordinal used only for deterministic
/// tie-breaks. Source and data-component identity are expressed as tags (see {@link VanillaTooltipTags}), not as
/// separate fields.
///
/// Immutable. Use {@link #builder()} / {@link #toBuilder()}.
public final class TooltipMetadata {
    private final Set<TooltipTag<?, ?>> plainTags;
    @Nullable
    private final TooltipTag<?, ?> negotiatedTag;
    private final String providerModId;
    private final long declarationOrdinal;

    private TooltipMetadata(Set<TooltipTag<?, ?>> plainTags, @Nullable TooltipTag<?, ?> negotiatedTag, String providerModId, long declarationOrdinal) {
        this.plainTags = plainTags;
        this.negotiatedTag = negotiatedTag;
        this.providerModId = providerModId;
        this.declarationOrdinal = declarationOrdinal;
    }

    public Set<TooltipTag<?, ?>> plainTags() {
        return plainTags;
    }

    @Nullable
    public TooltipTag<?, ?> negotiatedTag() {
        return negotiatedTag;
    }

    public String providerModId() {
        return providerModId;
    }

    public long declarationOrdinal() {
        return declarationOrdinal;
    }

    /// True if this node carries {@code tag} as a plain or negotiated tag.
    public boolean hasTag(TooltipTag<?, ?> tag) {
        return plainTags.contains(tag) || tag.equals(negotiatedTag);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        Builder builder = new Builder();
        builder.plainTags.addAll(plainTags);
        builder.negotiatedTag = negotiatedTag;
        builder.providerModId = providerModId;
        builder.declarationOrdinal = declarationOrdinal;
        return builder;
    }

    public static final class Builder {
        private final Set<TooltipTag<?, ?>> plainTags = new LinkedHashSet<>();
        @Nullable
        private TooltipTag<?, ?> negotiatedTag;
        private String providerModId = "minecraft";
        private long declarationOrdinal;

        private Builder() {}

        public Builder providerModId(String providerModId) {
            this.providerModId = providerModId;
            return this;
        }

        public Builder declarationOrdinal(long declarationOrdinal) {
            this.declarationOrdinal = declarationOrdinal;
            return this;
        }

        /// Attaches a tag. Plain tags go into the plain set (many allowed). A negotiated tag is held singly:
        /// attaching a second, different negotiated tag throws {@link IllegalStateException} eagerly.
        public Builder addTag(TooltipTag<?, ?> tag) {
            if (tag.isNegotiated()) {
                if (negotiatedTag != null && !negotiatedTag.equals(tag)) {
                    throw new IllegalStateException(
                            "A tooltip node may carry at most one negotiated tag; already has " + negotiatedTag + ", cannot add " + tag);
                }
                negotiatedTag = tag;
            } else {
                plainTags.add(tag);
            }
            return this;
        }

        public TooltipMetadata build() {
            return new TooltipMetadata(Set.copyOf(plainTags), negotiatedTag, providerModId, declarationOrdinal);
        }
    }
}
