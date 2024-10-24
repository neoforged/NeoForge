/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/** This is a copy-paste of {@link ModMismatchDisconnectedScreen} */
public class RegistryConfigMismatchScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Component reason;
    private final int listHeight = 140;
    private final Map<ResourceLocation, Component> mismatchedChannelData;
    private final Runnable replaceAction;
    private final Screen parent;

    private MultiLineLabel message = MultiLineLabel.EMPTY;
    private int textHeight;

    public RegistryConfigMismatchScreen(Screen parent, Component reason, @Nullable Runnable replaceAction, Map<ResourceLocation, Component> mismatchedChannelData) {
        super(Component.translatable("disconnect.lost"));
        this.reason = reason;
        this.parent = parent;
        this.mismatchedChannelData = mismatchedChannelData;
        mismatchedChannelData.forEach((id, r) -> LOGGER.warn("Registry Config [{}] failed to connect: {}", id, r.getString()));
        this.replaceAction = replaceAction;
    }

    @Override
    protected void init() {
        int lstX = Math.max(8, width / 2 - 220);
        int lstW = Math.min(440, width - 16);

        message = MultiLineLabel.create(font, reason, width - 50);
        textHeight = message.getLineCount() * 9;

        addRenderableWidget(new MismatchInfoPanel(minecraft, lstW, listHeight, (height - listHeight) / 2, lstX));

        int btnY = Math.min((height + listHeight) / 2 + 50, height - 25);
        int btnW = Math.min(210, width / 2 - 20);

        Button rep = addRenderableWidget(Button.builder(Component.translatable("fml.registryconfigmismatchscreen.overwrite"),
                button -> Optional.ofNullable(replaceAction).ifPresent(Runnable::run))
                .bounds(Math.min(width * 3 / 4 - btnW / 2, lstX),
                        btnY, btnW, 20)
                .build());
        rep.active = replaceAction != null;
        addRenderableWidget(Button.builder(Component.translatable("gui.toMenu"),
                button -> minecraft.setScreen(parent))
                .bounds(Math.min(width * 3 / 4 - btnW / 2, lstX + lstW - btnW),
                        btnY, btnW, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(font, title, width / 2, (height - listHeight) / 2 - textHeight - 9 * 4, 0xAAAAAA);
        message.renderCentered(guiGraphics, width / 2, (height - listHeight) / 2 - textHeight - 9 * 2);
    }

    class MismatchInfoPanel extends ScrollPanel {
        private final int nameIndent = 10;
        private final int tableWidth = width - border * 2 - 6 - nameIndent;
        private final int nameWidth = tableWidth / 2;
        private final int versionWidth = tableWidth - nameWidth;
        private List<Pair<FormattedCharSequence, FormattedCharSequence>> lineTable;
        private int contentSize;

        public MismatchInfoPanel(Minecraft client, int width, int height, int top, int left) {
            super(client, width, height, top, left);
            updateListContent();
        }

        private void updateListContent() {
            record Row(MutableComponent name, MutableComponent reason) {}
            //The raw list of the strings in a table row, the components may still be too long for the final table and will be split up later. The first row element may have a style assigned to it that will be used for the whole content row.
            List<Row> rows = new ArrayList<>();
            if (!mismatchedChannelData.isEmpty()) {
                //Each table row contains the channel id(s) and the reason for the corresponding channel mismatch.
                rows.add(new Row(
                        Component.translatable("fml.registryconfigmismatchscreen.table.type"),
                        Component.translatable("fml.registryconfigmismatchscreen.table.reason")));
                int i = 0;
                for (var channelData : mismatchedChannelData.entrySet()) {
                    rows.add(new Row(
                            Component.literal(channelData.getKey().toString()).withStyle(i % 2 == 0 ? ChatFormatting.GOLD : ChatFormatting.YELLOW),
                            channelData.getValue().copy()));
                    i++;
                }
                rows.add(new Row(Component.literal(""), Component.literal(""))); //Add one line of padding.
            }

            lineTable = rows.stream().flatMap(p -> splitLineToWidth(p.name(), p.reason()).stream()).collect(Collectors.toList());
            contentSize = lineTable.size();
            scrollDistance = 0;
        }

        // Start copying ModMismatchDisconnectedScreen$MismatchInfoPanel

        /**
         * Splits the raw channel namespace and mismatch reason strings, making them use multiple lines if needed, to fit within the table dimensions.
         * The style assigned to the name element is then applied to the entire content row.
         *
         * @param name   The first element of the content row, usually representing a table section header or a channel name entry
         * @param reason The second element of the content row, usually representing the reason why the channel is mismatched
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
            if (isMouseOver(x, y)) {
                double relativeY = y - top + scrollDistance - border;
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
