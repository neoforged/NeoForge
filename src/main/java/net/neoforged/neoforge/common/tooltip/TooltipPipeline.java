/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import com.mojang.logging.LogUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TooltipNegotiationEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/// Lifecycle bridge between the flat tooltip line list produced by {@code ItemStack#getTooltipLines} and the
/// structured negotiation pipeline: wrap the lines into a {@link TooltipDocument}, fire
/// {@link TooltipNegotiationEvent}, arbitrate the collected intents, and flatten the result back into the same
/// mutable list (so the legacy {@code ItemTooltipEvent} fired afterwards sees the negotiated lines).
///
/// Until the appender side is wired to emit structured output directly, every line becomes its own source group
/// owned by {@code "minecraft"} and only the first line (the styled hover name) carries a semantic tag
/// ({@link TooltipTags#itemName()}); lines contributed by mod appenders are indistinguishable from vanilla ones.
@ApiStatus.Internal
public final class TooltipPipeline {
    private static final Logger LOGGER = LogUtils.getLogger();

    private TooltipPipeline() {}

    /// Negotiate the flat tooltip {@code lines} of an item stack, mutating the list in place.
    public static void negotiateItemTooltip(ItemStack stack, @Nullable Player player, List<Component> lines, TooltipFlag flag, Item.TooltipContext context, TooltipDisplay display) {
        if (lines.isEmpty()) {
            return;
        }

        TooltipDocument document = new TooltipDocument();
        for (int i = 0; i < lines.size(); i++) {
            var output = document.newOutput("minecraft");
            var taggable = output.add(lines.get(i));
            if (i == 0) {
                taggable.tag(TooltipTags.itemName());
            }
            document.addSourceGroup(output);
        }
        var snapshot = document.freeze();

        TooltipNegotiationEvent event = new TooltipNegotiationEvent(snapshot, stack, context, display, player, flag);
        NeoForge.EVENT_BUS.post(event);
        // Flush intents of bare listeners that were registered without the provider-stamping helper.
        event.commitCurrent();

        List<TooltipIntent> intents = event.collectedIntents();
        if (intents.isEmpty() && snapshot.negotiatedTagIds().isEmpty()) {
            return; // nothing to resolve: leave the lines untouched
        }

        List<TooltipNode.Group> resolved = TooltipArbitrator.resolve(snapshot, intents, message -> LOGGER.debug("Tooltip negotiation: {}", message));
        lines.clear();
        for (TooltipNode.Entry entry : TooltipDocument.Snapshot.of(resolved).flatten()) {
            lines.add(entry.component());
        }
    }
}
