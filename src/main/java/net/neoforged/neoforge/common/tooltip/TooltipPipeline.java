/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.tooltip.ItemTooltipHandler.Section;
import net.neoforged.neoforge.event.TooltipNegotiationEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/// Lifecycle bridge between the flat tooltip line list produced by {@code ItemStack#getTooltipLines} /
/// {@code FluidStack#getTooltipLines} and the structured negotiation pipeline: wrap the lines into a
/// {@link TooltipDocument} (one source group per recorded emission section, see {@link Section}), fire
/// {@link TooltipNegotiationEvent}, arbitrate the collected intents, and flatten the result back into the same
/// mutable list (so the legacy {@code ItemTooltipEvent} / {@code FluidTooltipEvent} fired afterwards sees the
/// negotiated lines).
///
/// ## Vanilla content tags
///
/// Section keys recorded during emission drive the vanilla tags: the styled hover name (never part of a
/// section) is tagged {@link TooltipTags#itemName()}; the {@code appendHoverText} phase is tagged
/// {@link TooltipTags#lore()} with per-line {@link TooltipTags#loreLine(int)}; each component appender section
/// is tagged {@link TooltipTags#component(DataComponentType)}, with {@code ENCHANTMENTS}/{@code
/// ATTRIBUTE_MODIFIERS}/{@code DAMAGE} additionally tagged {@link TooltipTags#enchantments()} /
/// {@link TooltipTags#attributes()} / {@link TooltipTags#damage()}; the vanilla tail's registry-id line is
/// tagged {@link TooltipTags#itemId()} in advanced mode. Lines from mod location appenders (null-key sections)
/// and any line outside a recorded section get an un-tagged {@code "minecraft"} group.
///
/// ## Mod-name line
///
/// Like JEI, a line naming the mod the item or fluid comes from (JEI-styled: blue italic) is appended at the
/// tail of every non-empty tooltip. The line is emitted as a candidate on the {@code c:mod_name} negotiated
/// channel ({@link TooltipTags#modName}) so listeners can {@code prefer}/{@code remove}/{@code replace} it, and
/// so other mods' tagged mod-name lines collapse with it via the channel resolver. Deference to un-tagged
/// third-party renderers (today's JEI/Jade, which emit their line as plain text) is heuristic: if any existing
/// line already renders the same display name, ours is not emitted.
@ApiStatus.Internal
public final class TooltipPipeline {
    private static final Logger LOGGER = LogUtils.getLogger();

    private TooltipPipeline() {}

    /// Negotiate the flat tooltip {@code lines} of an item stack, mutating the list in place.
    public static void negotiateItemTooltip(ItemStack stack, @Nullable Player player, List<Component> lines, TooltipFlag flag, Item.TooltipContext context, TooltipDisplay display, List<Section> sections) {
        if (lines.isEmpty()) {
            return;
        }
        TooltipDocument document = buildDocument(lines, sections, flag);
        if (!stack.isEmpty()) {
            addModNameCandidate(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace(), lines, document);
        }
        postAndApply(document, lines, snapshot -> new TooltipNegotiationEvent(snapshot, stack, context, display, player, flag));
    }

    /// Negotiate the flat tooltip {@code lines} of a fluid stack, mutating the list in place.
    public static void negotiateFluidTooltip(FluidStack stack, @Nullable Player player, List<Component> lines, TooltipFlag flag, Item.TooltipContext context, TooltipDisplay display, List<Section> sections) {
        if (lines.isEmpty()) {
            return;
        }
        TooltipDocument document = buildDocument(lines, sections, flag);
        if (!stack.isEmpty()) {
            addModNameCandidate(BuiltInRegistries.FLUID.getKey(stack.getFluid()).getNamespace(), lines, document);
        }
        postAndApply(document, lines, snapshot -> new TooltipNegotiationEvent(snapshot, stack, context, display, player, flag));
    }

    /// Build the document from the flat lines and the recorded sections: line 0 (the styled hover name) and any
    /// line outside a section become single-line groups; every non-empty section becomes one source group with
    /// the vanilla content tags derived from its key.
    private static TooltipDocument buildDocument(List<Component> lines, List<Section> sections, TooltipFlag flag) {
        TooltipDocument document = new TooltipDocument();
        int cursor = 0;
        for (Section section : sections) {
            cursor = addPlainLines(document, lines, cursor, section.from());
            if (section.to() > section.from()) {
                addSectionGroup(document, lines, section, flag);
            }
            cursor = Math.max(cursor, section.to());
        }
        addPlainLines(document, lines, cursor, lines.size());
        return document;
    }

    /// Add the lines in {@code [from, to)} as single-line un-tagged groups (line 0 carries the item-name tag),
    /// returning the new cursor.
    private static int addPlainLines(TooltipDocument document, List<Component> lines, int from, int to) {
        int end = Math.min(to, lines.size());
        for (int i = from; i < end; i++) {
            var output = document.newOutput("minecraft");
            var taggable = output.add(lines.get(i));
            if (i == 0) {
                taggable.tag(TooltipTags.itemName());
            }
            document.addSourceGroup(output);
        }
        return Math.max(from, end);
    }

    private static void addSectionGroup(TooltipDocument document, List<Component> lines, Section section, TooltipFlag flag) {
        var output = document.newOutput("minecraft");
        Object key = section.key();
        if (key instanceof DataComponentType<?> type) {
            output.sourceTag(TooltipTags.component(type));
            if (type == DataComponents.ENCHANTMENTS) {
                output.sourceTag(TooltipTags.enchantments());
            } else if (type == DataComponents.ATTRIBUTE_MODIFIERS) {
                output.sourceTag(TooltipTags.attributes());
            }
        } else if (key == ItemTooltipHandler.Phase.HOVER_TEXT) {
            output.sourceTag(TooltipTags.lore());
        }
        for (int i = section.from(); i < section.to(); i++) {
            var taggable = output.add(lines.get(i));
            if (key == ItemTooltipHandler.Phase.HOVER_TEXT) {
                taggable.tag(TooltipTags.loreLine(i - section.from()));
            } else if (key == DataComponents.DAMAGE) {
                taggable.tag(TooltipTags.damage());
            } else if (key == ItemTooltipHandler.Phase.TAIL && i == section.from() && flag.isAdvanced()) {
                taggable.tag(TooltipTags.itemId());
            }
        }
        document.addSourceGroup(output);
    }

    /// Append the mod-name candidate (tail position, JEI styling) unless another renderer already shows it.
    private static void addModNameCandidate(String modId, List<Component> lines, TooltipDocument document) {
        String displayName = modDisplayName(modId);
        for (Component line : lines) {
            if (line.getString().equals(displayName)) {
                return; // defer: another renderer (e.g. JEI) already shows the mod name
            }
        }
        var output = document.newOutput("neoforge");
        output.add(Component.literal(displayName).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC))
                .tag(TooltipTags.modName(modId));
        document.addSourceGroup(output);
    }

    /// Freeze the document, fire the negotiation event, arbitrate the collected intents, and flatten the
    /// resolved tree back into {@code lines}.
    private static void postAndApply(TooltipDocument document, List<Component> lines, Function<TooltipDocument.Snapshot, TooltipNegotiationEvent> eventFactory) {
        var snapshot = document.freeze();
        TooltipNegotiationEvent event = eventFactory.apply(snapshot);
        NeoForge.EVENT_BUS.post(event);

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

    /// The display name of the mod with the given id, falling back to the raw id when the mod list is
    /// unavailable (early startup, bare junit) or the mod is unknown.
    private static String modDisplayName(String modId) {
        ModList modList = ModList.get();
        if (modList == null) {
            return modId;
        }
        return modList.getModContainerById(modId)
                .map(container -> container.getModInfo().getDisplayName())
                .orElse(modId);
    }
}
