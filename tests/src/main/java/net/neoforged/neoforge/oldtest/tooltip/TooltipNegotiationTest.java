/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.tooltip.TooltipTags;
import net.neoforged.neoforge.event.TooltipNegotiationEvent;

/// In-game test for the tooltip negotiation pipeline (see {@code TooltipPipeline} and
/// {@code TooltipNegotiationEvent}). Registers four listeners on the game bus to simulate four independent mods
/// negotiating over the same tooltip:
///
/// - <b>provider_a</b> inserts a high-priority line directly under the item name and an unanchored footer that
///   reports the line count it read from the snapshot (read-side API);
/// - <b>provider_b</b> inserts a normal-priority line under the item name. Because both adds anchor on
///   {@code item_name}, they stack in the deterministic cross-listener order: the priority-1 line of provider_a
///   sits closer to the anchor regardless of listener registration order;
/// - <b>provider_c</b> renames apples via an exact-match {@code replace} on {@code item_name};
/// - <b>provider_d</b> suppresses the mod-name line on sticks by removing the {@code c:mod_name} channel
///   candidate with value {@code "minecraft"}.
///
/// Manual check: hover a stick &rarr; name, then [A], then [B], vanilla lines, footer at the tail, and <em>no</em>
/// "Minecraft" mod-name line (removed by provider_d). Hover an apple &rarr; the name is replaced with
/// "Negotiated Apple". Hover any other item (e.g. a diamond sword) &rarr; a blue-italic "Minecraft" line sits at
/// the tail (or that item's own mod name). The relative order of [A]/[B] must stay stable across restarts
/// (listener-order independence).
@Mod(TooltipNegotiationTest.ID)
public class TooltipNegotiationTest {
    static final boolean ENABLED = true;
    static final String ID = "tooltip_negotiation_test";

    public TooltipNegotiationTest() {
        if (!ENABLED) {
            return;
        }
        // Simulates mod A: priority-1 line under the item name + a footer built from snapshot reads.
        NeoForge.EVENT_BUS.addListener(TooltipNegotiationEvent.class, event -> {
            if (!isTestItem(event.getItemStack())) {
                return;
            }
            var tooltip = event.tooltip("provider_a");
            tooltip.add(Component.literal("[A] High priority line").withStyle(ChatFormatting.GOLD))
                    .after(TooltipTags.itemName())
                    .priority(1);
            tooltip.add(Component.literal("[A] Tooltip lines seen: " + event.snapshot().flatten().size()).withStyle(ChatFormatting.DARK_GRAY));
        });
        // Simulates mod B: priority-0 line under the item name; stacks after provider_a's line deterministically.
        NeoForge.EVENT_BUS.addListener(TooltipNegotiationEvent.class, event -> {
            if (!isTestItem(event.getItemStack())) {
                return;
            }
            event.tooltip("provider_b").add(Component.literal("[B] Normal priority line").withStyle(ChatFormatting.AQUA))
                    .after(TooltipTags.itemName());
        });
        // Simulates mod C: renames apples via exact-match replace on the item name.
        NeoForge.EVENT_BUS.addListener(TooltipNegotiationEvent.class, event -> {
            if (!event.getItemStack().is(Items.APPLE)) {
                return;
            }
            event.tooltip("provider_c").replace(TooltipTags.itemName(), Component.literal("Negotiated Apple").withStyle(ChatFormatting.GREEN));
        });
        // Simulates mod D: suppresses the mod-name line on sticks via the negotiated channel.
        NeoForge.EVENT_BUS.addListener(TooltipNegotiationEvent.class, event -> {
            if (!event.getItemStack().is(Items.STICK)) {
                return;
            }
            event.tooltip("provider_d").remove(TooltipTags.modName("minecraft"));
        });
    }

    private static boolean isTestItem(ItemStack stack) {
        return stack.is(Items.STICK) || stack.is(Items.APPLE);
    }
}
