/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import com.mojang.blaze3d.vertex.Tesselator;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import net.neoforged.neoforge.common.I18nExtension;
import org.apache.commons.lang3.tuple.Pair;

public class ModMismatchDisconnectedScreen extends Screen {
    private final Component reason;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    private final Screen parent;
    private int textHeight;
    private final Path modsDir;
    private final Path logFile;
    private final int listHeight = 140;
    private final Map<ResourceLocation, Component> mismatchedChannelData;

    public ModMismatchDisconnectedScreen(Screen parentScreen, Component reason, Map<ResourceLocation, Component> mismatchedChannelData) {
        super(Component.translatable("disconnect.lost"));
        this.reason = reason;
        this.parent = parentScreen;
        this.modsDir = FMLPaths.MODSDIR.get();
        this.logFile = FMLPaths.GAMEDIR.get().resolve(Paths.get("logs", "latest.log"));
        this.mismatchedChannelData = mismatchedChannelData;
    }

    @Override
    protected void init() {
        int listLeft = Math.max(8, this.width / 2 - 220);
        int listWidth = Math.min(440, this.width - 16);

        this.message = MultiLineLabel.create(this.font, this.reason, this.width - 50);
        this.textHeight = this.message.getLineCount() * 9;

        int upperButtonHeight = Math.min((this.height + this.listHeight) / 2 + 25, this.height - 50);
        int lowerButtonHeight = Math.min((this.height + this.listHeight) / 2 + 50, this.height - 25);
        this.addRenderableWidget(new MismatchInfoPanel(minecraft, listWidth, listHeight, (this.height - this.listHeight) / 2, listLeft));

        int buttonWidth = Math.min(210, this.width / 2 - 20);
        this.addRenderableWidget(Button.builder(Component.literal(I18nExtension.parseMessage("fml.button.open.file", logFile.getFileName())), button -> Util.getPlatform().openFile(logFile.toFile()))
                .bounds(Math.max(this.width / 4 - buttonWidth / 2, listLeft), upperButtonHeight, buttonWidth, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal(I18nExtension.parseMessage("fml.button.open.mods.folder")), button -> Util.getPlatform().openFile(modsDir.toFile()))
                .bounds(Math.min(this.width * 3 / 4 - buttonWidth / 2, listLeft + listWidth - buttonWidth), upperButtonHeight, buttonWidth, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.toMenu"), button -> this.minecraft.setScreen(this.parent))
                .bounds((this.width - buttonWidth) / 2, lowerButtonHeight, buttonWidth, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, (this.height - this.listHeight) / 2 - this.textHeight - 9 * 4, 0xAAAAAA);
        this.message.renderCentered(guiGraphics, this.width / 2, (this.height - this.listHeight) / 2 - this.textHeight - 9 * 2);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    class MismatchInfoPanel extends ScrollPanel {
        private final List<Pair<FormattedCharSequence, FormattedCharSequence>> lineTable;
        private final int contentSize;
        private final int nameIndent = 10;
        private final int tableWidth = width - border * 2 - 6 - nameIndent;
        private final int nameWidth = tableWidth / 2;
        private final int versionWidth = tableWidth - nameWidth;

        public MismatchInfoPanel(Minecraft client, int width, int height, int top, int left) {
            super(client, width, height, top, left);

            Map<ResourceLocation, Pair<Integer, Component>> collapsedChannelData = collapseChannelData(mismatchedChannelData);
            record Row(MutableComponent name, MutableComponent reason) {}
            //The raw list of the strings in a table row, the components may still be too long for the final table and will be split up later. The first row element may have a style assigned to it that will be used for the whole content row.
            List<Row> rows = new ArrayList<>();
            if (!collapsedChannelData.isEmpty()) {
                //Each table row contains the mod name and the reason for the corresponding channel mismatch.
                rows.add(new Row(Component.translatable("fml.modmismatchscreen.table.channelname"), Component.translatable("fml.modmismatchscreen.table.reason")));
                int i = 0;
                for (Map.Entry<ResourceLocation, Pair<Integer, Component>> channelData : collapsedChannelData.entrySet()) {
                    rows.add(new Row(toChannelNameComponent(channelData.getKey(), channelData.getValue().getLeft(), i % 2 == 0 ? ChatFormatting.GOLD : ChatFormatting.YELLOW), channelData.getValue().getRight().copy()));
                    if (++i == 20 && collapsedChannelData.size() > 20) {
                        //If too many mismatched mod entries are present, append a line referencing how to see the full list and stop rendering any more entries
                        rows.add(new Row(Component.literal(""), Component.translatable("fml.modmismatchscreen.additional", collapsedChannelData.size() - i).withStyle(ChatFormatting.ITALIC)));
                        break;
                    }
                }
                rows.add(new Row(Component.literal(""), Component.literal(""))); //Add one line of padding.
            }

            this.lineTable = rows.stream().flatMap(p -> splitLineToWidth(p.name(), p.reason()).stream()).collect(Collectors.toList());
            this.contentSize = lineTable.size();
        }

        /**
         * Collapses quasi-duplicate channel mismatch entries into single list entries to reduce repetition of entries in the final list.
         * Quasi-duplicate channel mismatch entries share the same channel namespace and mismatch reasons, and it is thus very likely that they are caused
         * by one and the same mod (that has registered all of these channels) missing/mismatching between client and server.
         *
         * @param mismatchedChannelData The raw mismatched channel data received from the server, which might contain quasi-duplicate entries
         * @return A map containing deduplicated channel mismatch entries. For each quasi-duplicate group, only the first encountered channel id is kept,
         * and all other quasi-duplicate channels then increment the associated repetition count that is mapped to that first channel id.
         * Finally, the (unchanged) mismatch reason (which is the same for all quasi-duplicate entries) also gets mapped to the channel id.
         */
        private Map<ResourceLocation, Pair<Integer, Component>> collapseChannelData(Map<ResourceLocation, Component> mismatchedChannelData) {
            Map<ResourceLocation, Pair<Integer, Component>> repetitions = new LinkedHashMap<>();
            List<ResourceLocation> sortedChannels = mismatchedChannelData.keySet().stream().sorted(Comparator.comparing(ResourceLocation::toString)).toList();
            for (ResourceLocation channel : sortedChannels) {
                Component channelMismatchReason = mismatchedChannelData.get(channel);
                List<ResourceLocation> namespaceChannels = repetitions.keySet().stream().filter(r -> r.getNamespace().equals(channel.getNamespace())).toList();
                boolean matched = false;
                if (!namespaceChannels.isEmpty()) {
                    for (ResourceLocation potentialRepetitionChannel : namespaceChannels) {
                        Pair<Integer, Component> repetitionData = repetitions.get(potentialRepetitionChannel);

                        if (repetitionData.getRight().equals(channelMismatchReason)) {
                            repetitions.put(potentialRepetitionChannel, Pair.of(repetitionData.getLeft() + 1, repetitionData.getRight()));
                            matched = true;
                        }
                    }
                }
                if (!matched)
                    repetitions.put(channel, Pair.of(1, channelMismatchReason));
            }

            return repetitions;
        }

        /**
         * Splits the raw mod name and mismatch reason strings, making them use multiple lines if needed, to fit within the table dimensions.
         * The style assigned to the name element is then applied to the entire content row.
         *
         * @param name   The first element of the content row, usually representing a table section header or the name of a mod entry
         * @param reason The second element of the content row, usually representing the reason why the mod is mismatched
         * @return A list of table rows consisting of 2 elements each which consist of the same content as was given by the parameters, but split up to fit within the table dimensions.
         */
        private List<Pair<FormattedCharSequence, FormattedCharSequence>> splitLineToWidth(MutableComponent name, MutableComponent reason) {
            Style style = name.getStyle();
            List<FormattedCharSequence> nameLines = font.split(name, nameWidth - 4);
            List<FormattedCharSequence> reasonLines = font.split(reason.setStyle(style), versionWidth - 4);
            List<Pair<FormattedCharSequence, FormattedCharSequence>> splitLines = new ArrayList<>();

            int rowsOccupied = Math.max(nameLines.size(), reasonLines.size());
            for (int i = 0; i < rowsOccupied; i++) {
                splitLines.add(Pair.of(i < nameLines.size() ? nameLines.get(i) : FormattedCharSequence.EMPTY, i < reasonLines.size() ? reasonLines.get(i) : FormattedCharSequence.EMPTY));
            }
            return splitLines;
        }

        /**
         * Uses the given channel id to return a component with the name of the mod that likely owns the channel. If no such mod is found, the namespace of the channel id is used instead.
         * The style assigned to the returned component contains the color of the entry, a hover event containing the channel id, and an optional click event which, if present, opens the homepage of the mod.
         *
         * @param id The id of the mismatched channel. Used to query the name of the mod that has likely registered the channel in order to use and display its name and homepage URL.
         * @param repetitionCount How many other channels of the same mod failed negotiation with the same error message. Displayed in the hover tooltip.
         * @param color Defines the color of the returned channel name component.
         * @return A component with the mod name (if available) as the main text component, and an assigned color which will be used for the whole content row.
         */
        private MutableComponent toChannelNameComponent(ResourceLocation id, int repetitionCount, ChatFormatting color) {
            String modId = id.getNamespace();
            Optional<? extends ModContainer> mod = ModList.get().getModContainerById(modId);
            String name = mod.map(m -> m.getModInfo().getDisplayName()).orElse(modId);
            String url = mod.flatMap(container -> container.getModInfo().getModURL())
                    .map(URL::toString)
                    .orElse("");
            MutableComponent result = Component.literal(name).withStyle(color);
            MutableComponent hoverText = Component.literal(id.toString());
            if (repetitionCount > 1)
                hoverText.append(Component.literal(" [+%s more]".formatted(repetitionCount)).withStyle(ChatFormatting.GRAY));
            if (!url.isEmpty()) {
                hoverText.append(Component.literal("\n").append(Component.translatable("fml.modmismatchscreen.table.visit.mod_page", id.toString())));
                result = result.withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
            }

            result = result.withStyle(s -> s.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, hoverText)));
            return result;
        }

        @Override
        protected int getContentHeight() {
            int height = contentSize * (font.lineHeight + 3);

            if (height < bottom - top - 4)
                height = bottom - top - 4;

            return height;
        }

        @Override
        protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
            int i = 0;

            for (Pair<FormattedCharSequence, FormattedCharSequence> line : lineTable) {
                FormattedCharSequence name = line.getLeft();
                FormattedCharSequence reasons = line.getRight();
                //Since font#draw does not respect the color of the given component, we have to read it out here and then use it as the last parameter
                int color = Optional.ofNullable(font.getSplitter().componentStyleAtWidth(name, 0)).map(Style::getColor).map(TextColor::getValue).orElse(0xFFFFFF);
                //Only indent the given name if a version string is present. This makes it easier to distinguish table section headers and mod entries
                int nameLeft = left + border + (reasons == null ? 0 : nameIndent);
                guiGraphics.drawString(font, name, nameLeft, relativeY + i * 12, color, false);
                if (reasons != null) {
                    guiGraphics.drawString(font, reasons, left + border + nameIndent + nameWidth, relativeY + i * 12, color, false);
                }

                i++;
            }
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
            Style style = getComponentStyleAt(mouseX, mouseY);
            if (style != null && style.getHoverEvent() != null) {
                guiGraphics.renderComponentHoverEffect(font, style, mouseX, mouseY);
            }
        }

        public Style getComponentStyleAt(double x, double y) {
            if (this.isMouseOver(x, y)) {
                double relativeY = y - this.top + this.scrollDistance - border;
                int slotIndex = (int) (relativeY + (border / 2)) / 12;
                if (slotIndex < contentSize) {
                    //The relative x needs to take the potentially missing indent of the row into account. It does that by checking if the line has a version associated to it
                    double relativeX = x - left - border - (lineTable.get(slotIndex).getRight() == null ? 0 : nameIndent);
                    if (relativeX >= 0)
                        return font.getSplitter().componentStyleAtWidth(lineTable.get(slotIndex).getLeft(), (int) relativeX);
                }
            }

            return null;
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            Style style = getComponentStyleAt(mouseX, mouseY);
            if (style != null) {
                handleComponentClicked(style);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput output) {}
    }
}
