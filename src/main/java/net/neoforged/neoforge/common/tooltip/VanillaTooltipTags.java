/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;

/// Stable semantic tags for vanilla tooltip content. Tags are <em>semantic only</em> (which lore line, which
/// enchantment, which data component) &mdash; never structural. Factory methods return ready {@link TooltipTag}s
/// for use in {@code remove}/{@code replace}/{@code addBefore}/{@code addAfter} and in {@code output.add(x).tag(...)}.
///
/// v1 populates: {@code item_name}, {@code lore}+{@code loreLine}, {@code enchantments}, {@code attributes}
/// (group-only), {@code damage}, {@code itemId}, plus the auto {@code component(type)} and source tags. Per-key
/// enchantment and per-modifier attribute tags exist as factories but are not yet populated by the vanilla taggers
/// (they require deeper emission context; tracked as follow-up).
public final class VanillaTooltipTags {
    private static final TooltipTagType.Plain<TooltipEntry, Void> ITEM_NAME = TooltipTagType.Plain.create(id("item_name"), TooltipEntry.class);
    private static final TooltipTagType.Plain<TooltipGroup, Void> LORE = TooltipTagType.Plain.create(id("lore"), TooltipGroup.class);
    private static final TooltipTagType.Plain<TooltipEntry, Integer> LORE_LINE = TooltipTagType.Plain.create(id("lore_line"), TooltipEntry.class);
    private static final TooltipTagType.Plain<TooltipGroup, Void> ENCHANTMENTS = TooltipTagType.Plain.create(id("enchantments"), TooltipGroup.class);
    private static final TooltipTagType.Plain<TooltipEntry, Identifier> ENCHANTMENT = TooltipTagType.Plain.create(id("enchantment"), TooltipEntry.class);
    private static final TooltipTagType.Plain<TooltipGroup, Void> ATTRIBUTES = TooltipTagType.Plain.create(id("attributes"), TooltipGroup.class);
    private static final TooltipTagType.Plain<TooltipEntry, Void> DAMAGE = TooltipTagType.Plain.create(id("damage"), TooltipEntry.class);
    private static final TooltipTagType.Plain<TooltipEntry, Void> ITEM_ID = TooltipTagType.Plain.create(id("item_id"), TooltipEntry.class);
    private static final TooltipTagType.Plain<TooltipNode, DataComponentType<?>> COMPONENT = TooltipTagType.Plain.create(id("component"), TooltipNode.class);
    private static final TooltipTagType.Plain<TooltipGroup, Identifier> APPENDER = TooltipTagType.Plain.create(id("appender"), TooltipGroup.class);

    static {
        TooltipTagType.register(ITEM_NAME);
        TooltipTagType.register(LORE);
        TooltipTagType.register(LORE_LINE);
        TooltipTagType.register(ENCHANTMENTS);
        TooltipTagType.register(ENCHANTMENT);
        TooltipTagType.register(ATTRIBUTES);
        TooltipTagType.register(DAMAGE);
        TooltipTagType.register(ITEM_ID);
        TooltipTagType.register(COMPONENT);
        TooltipTagType.register(APPENDER);
    }

    public static TooltipTag<TooltipEntry, Void> itemName() {
        return ITEM_NAME.tag(null);
    }

    public static TooltipTag<TooltipGroup, Void> lore() {
        return LORE.tag(null);
    }

    public static TooltipTag<TooltipEntry, Integer> loreLine(int index) {
        return LORE_LINE.tag(index);
    }

    public static TooltipTag<TooltipGroup, Void> enchantments() {
        return ENCHANTMENTS.tag(null);
    }

    public static TooltipTag<TooltipEntry, Identifier> enchantment(Identifier enchantmentId) {
        return ENCHANTMENT.tag(enchantmentId);
    }

    public static TooltipTag<TooltipGroup, Void> attributes() {
        return ATTRIBUTES.tag(null);
    }

    public static TooltipTag<TooltipEntry, Void> damage() {
        return DAMAGE.tag(null);
    }

    public static TooltipTag<TooltipEntry, Void> itemId() {
        return ITEM_ID.tag(null);
    }

    /// Auto-applied to every data-component appender's source group.
    public static TooltipTag<TooltipNode, DataComponentType<?>> component(DataComponentType<?> type) {
        return COMPONENT.tag(type);
    }

    /// Whole source-group identity by appender id.
    public static TooltipTag<TooltipGroup, Identifier> appender(Identifier appenderId) {
        return APPENDER.tag(appenderId);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("minecraft", path);
    }

    private VanillaTooltipTags() {}
}
