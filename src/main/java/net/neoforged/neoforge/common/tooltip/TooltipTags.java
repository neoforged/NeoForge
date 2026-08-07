/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;

/// Stable semantic tags for tooltip content, in two groups:
///
/// - <b>Vanilla content tags</b> ({@code minecraft:}): semantic only (which lore line, which enchantment, which
///   data component) &mdash; never structural. Factory methods return ready {@link TooltipTag}s for use in
///   {@code remove}/{@code replace}/{@code addBefore}/{@code addAfter} and in {@code output.add(x).tag(...)}.
///   v1 populates: {@code item_name}, {@code lore}+{@code loreLine}, {@code enchantments}, {@code attributes}
///   (group-only), {@code damage}, {@code itemId}, plus the auto {@code component(type)} and source tags. Per-key
///   enchantment and per-modifier attribute tags exist as factories but are not yet populated by the vanilla
///   taggers (they require deeper emission context; tracked as follow-up).
/// - <b>Cross-mod negotiated channels</b> ({@code c:}): channels shared by mods that agree on a meaning. The
///   canonical example is {@link #MOD_NAME}: multiple mods may emit a "Mod Name" line, all tagged as a candidate,
///   and the {@link TooltipResolver#chooseOne() chooseOne} resolver collapses them to a single survivor.
///
/// All channels here are declared at priority 0 with no constraints &mdash; the same standing as any mod's own
/// declaration of the same channel.
public final class TooltipTags {
    /// Value = the claimant mod id; resolved by {@link TooltipResolver#chooseOne()}.
    public static final TooltipTag<TooltipNode.Entry, String> MOD_NAME =
            TooltipTag.negotiated(Identifier.fromNamespaceAndPath("c", "mod_name"), TooltipNode.Entry.class, TooltipResolver.chooseOne());

    private static final TooltipTag<TooltipNode.Entry, Void> ITEM_NAME = TooltipTag.plain(id("item_name"), TooltipNode.Entry.class);
    private static final TooltipTag<TooltipNode.Group, Void> LORE = TooltipTag.plain(id("lore"), TooltipNode.Group.class);
    private static final TooltipTag<TooltipNode.Entry, Integer> LORE_LINE = TooltipTag.plain(id("lore_line"), TooltipNode.Entry.class);
    private static final TooltipTag<TooltipNode.Group, Void> ENCHANTMENTS = TooltipTag.plain(id("enchantments"), TooltipNode.Group.class);
    private static final TooltipTag<TooltipNode.Entry, Identifier> ENCHANTMENT = TooltipTag.plain(id("enchantment"), TooltipNode.Entry.class);
    private static final TooltipTag<TooltipNode.Group, Void> ATTRIBUTES = TooltipTag.plain(id("attributes"), TooltipNode.Group.class);
    private static final TooltipTag<TooltipNode.Entry, Void> DAMAGE = TooltipTag.plain(id("damage"), TooltipNode.Entry.class);
    private static final TooltipTag<TooltipNode.Entry, Void> ITEM_ID = TooltipTag.plain(id("item_id"), TooltipNode.Entry.class);
    private static final TooltipTag<TooltipNode, DataComponentType<?>> COMPONENT = TooltipTag.plain(id("component"), TooltipNode.class);
    private static final TooltipTag<TooltipNode.Group, Identifier> APPENDER = TooltipTag.plain(id("appender"), TooltipNode.Group.class);

    /// Attach the {@code mod_name} negotiated channel to a mod-name entry, value = the claimant mod id.
    public static TooltipTag<TooltipNode.Entry, String> modName(String claimantModId) {
        return MOD_NAME.tag(claimantModId);
    }

    public static TooltipTag<TooltipNode.Entry, Void> itemName() {
        return ITEM_NAME;
    }

    public static TooltipTag<TooltipNode.Group, Void> lore() {
        return LORE;
    }

    public static TooltipTag<TooltipNode.Entry, Integer> loreLine(int index) {
        return LORE_LINE.tag(index);
    }

    public static TooltipTag<TooltipNode.Group, Void> enchantments() {
        return ENCHANTMENTS;
    }

    public static TooltipTag<TooltipNode.Entry, Identifier> enchantment(Identifier enchantmentId) {
        return ENCHANTMENT.tag(enchantmentId);
    }

    public static TooltipTag<TooltipNode.Group, Void> attributes() {
        return ATTRIBUTES;
    }

    public static TooltipTag<TooltipNode.Entry, Void> damage() {
        return DAMAGE;
    }

    public static TooltipTag<TooltipNode.Entry, Void> itemId() {
        return ITEM_ID;
    }

    /// Auto-applied to every data-component appender's source group.
    public static TooltipTag<TooltipNode, DataComponentType<?>> component(DataComponentType<?> type) {
        return COMPONENT.tag(type);
    }

    /// Whole source-group identity by appender id.
    public static TooltipTag<TooltipNode.Group, Identifier> appender(Identifier appenderId) {
        return APPENDER.tag(appenderId);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("minecraft", path);
    }

    private TooltipTags() {}
}
