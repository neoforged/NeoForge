/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import net.minecraft.resources.Identifier;

/// Cross-mod negotiated channels shared by mods that agree on a meaning. The canonical example is
/// {@link #MOD_NAME}: multiple mods may emit a "Mod Name" line, all tagged as a candidate, and the
/// {@link TooltipResolver#chooseOne() chooseOne} resolver collapses them to a single survivor.
public final class CommonTooltipTags {
    /// Value = the claimant mod id; resolved by {@link TooltipResolver#chooseOne()}.
    public static final TooltipTagType.Negotiated<TooltipEntry, String> MOD_NAME =
            TooltipTagType.Negotiated.create(Identifier.fromNamespaceAndPath("c", "mod_name"), TooltipEntry.class, TooltipResolver.chooseOne());

    static {
        TooltipTagType.register(MOD_NAME);
    }

    /// Attach the {@code mod_name} negotiated channel to a mod-name entry, value = the claimant mod id.
    public static TooltipTag<TooltipEntry, String> modName(String claimantModId) {
        return MOD_NAME.tag(claimantModId);
    }

    private CommonTooltipTags() {}
}
