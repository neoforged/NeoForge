/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.logging.LogUtils;
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
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import net.neoforged.neoforge.common.I18nExtension;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

public class ModMismatchDisconnectedScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Component reason;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    private MismatchInfoPanel scrollList;
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
        this.mismatchedChannelData.replaceAll((id, r) -> { //Enhance the reason components provided by the server with the info of which mod owns the given channel, if such a mod can be found on the client
            String owningModId = NetworkRegistry.getInstance().getOwningModId(id);
            Optional<String> modDisplayName = ModList.get().getModContainerById(owningModId).map(mod -> mod.getModInfo().getDisplayName());
            return modDisplayName.isPresent() && !(r.getContents() instanceof TranslatableContents c && c.getKey().equals("neoforge.network.negotiation.failure.mod")) ? Component.translatable("neoforge.network.negotiation.failure.mod", modDisplayName.get(), r) : r;
        });
        this.mismatchedChannelData.forEach((id, r) -> LOGGER.warn("Channel [{}] failed to connect: {}", id, r.getString()));
    }

    @Override
    protected void init() {
        int listLeft = Math.max(8, this.width / 2 - 220);
        int listWidth = Math.min(440, this.width - 16);

        this.message = MultiLineLabel.create(this.font, this.reason, this.width - 50);
        this.textHeight = this.message.getLineCount() * 9;

        int upperButtonHeight = Math.min((this.height + this.listHeight) / 2 + 25, this.height - 50);
        int lowerButtonHeight = Math.min((this.height + this.listHeight) / 2 + 50, this.height - 25);
        this.addRenderableWidget(this.scrollList = new MismatchInfoPanel(minecraft, listWidth, listHeight, (this.height - this.listHeight) / 2, listLeft));

        int buttonWidth = Math.min(210, this.width / 2 - 20);
        this.addRenderableWidget(CycleButton.onOffBuilder(true)
                .create(Math.max(this.width / 4 - buttonWidth / 2, listLeft), upperButtonHeight, buttonWidth, 20, Component.translatable("fml.modmismatchscreen.simplifiedview"), (b, v) -> scrollList.toggleSimplifiedView()));
        this.addRenderableWidget(Button.builder(Component.literal(I18nExtension.parseMessage("fml.button.open.file", logFile.getFileName())), button -> Util.getPlatform().openFile(logFile.toFile()))
                .bounds(Math.min(this.width * 3 / 4 - buttonWidth / 2, listLeft + listWidth - buttonWidth), upperButtonHeight, buttonWidth, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal(I18nExtension.parseMessage("fml.button.open.mods.folder")), button -> Util.getPlatform().openFile(modsDir.toFile()))
                .bounds(Math.max(this.width / 4 - buttonWidth / 2, listLeft), lowerButtonHeight, buttonWidth, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.toMenu"), button -> this.minecraft.setScreen(this.parent))
                .bounds(Math.min(this.width * 3 / 4 - buttonWidth / 2, listLeft + listWidth - buttonWidth), lowerButtonHeight, buttonWidth, 20)
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
        private final int nameIndent = 10;
        private final int tableWidth = width - border * 2 - 6 - nameIndent;
        private final int nameWidth = tableWidth / 2;
        private final int versionWidth = tableWidth - nameWidth;
        private List<Pair<FormattedCharSequence, FormattedCharSequence>> lineTable;
        private int contentSize;
        private boolean oneChannelPerEntry = true;

        public MismatchInfoPanel(Minecraft client, int width, int height, int top, int left) {
            super(client, width, height, top, left);
            updateListContent();
        }

        private void updateListContent() {
            Map<List<ResourceLocation>, Component> mergedChannelData = sortAndMergeChannelData(mismatchedChannelData);
            record Row(MutableComponent name, MutableComponent reason) {}
            //The raw list of the strings in a table row, the components may still be too long for the final table and will be split up later. The first row element may have a style assigned to it that will be used for the whole content row.
            List<Row> rows = new ArrayList<>();
            if (!mergedChannelData.isEmpty()) {
                //Each table row contains the channel id(s) and the reason for the corresponding channel mismatch.
                rows.add(new Row(Component.translatable("fml.modmismatchscreen.table.channelname"), Component.translatable("fml.modmismatchscreen.table.reason")));
                int i = 0;
                for (Map.Entry<List<ResourceLocation>, Component> channelData : mergedChannelData.entrySet()) {
                    rows.add(new Row(toChannelComponent(channelData.getKey(), i % 2 == 0 ? ChatFormatting.GOLD : ChatFormatting.YELLOW), channelData.getValue().copy()));
                    if (++i == 30 && mergedChannelData.size() > 30) {
                        //If too many mismatched channel entries are present, append a line referencing how to see the full list and stop rendering any more entries
                        rows.add(new Row(Component.literal(""), Component.translatable("fml.modmismatchscreen.additional", mergedChannelData.size() - i).withStyle(ChatFormatting.ITALIC)));
                        break;
                    }
                }
                rows.add(new Row(Component.literal(""), Component.literal(""))); //Add one line of padding.
            }

            this.lineTable = rows.stream().flatMap(p -> splitLineToWidth(p.name(), p.reason()).stream()).collect(Collectors.toList());
            this.contentSize = lineTable.size();
            this.scrollDistance = 0;
        }

        /**
         * Iterates over the raw channel mismatch data and merges entries with the same reason component into one channel mismatch entry each.
         * Due to the reason component always containing the display name of the mod that owns the associated channel, this step effectively groups channels by their owning mod,
         * so users can see more easily which mods are the culprits of the negotiation failure that caused this screen to appear.
         *
         * @param mismatchedChannelData The raw mismatched channel data received from the server, which might contain entries with duplicate channel mismatch reasons
         * @return A map containing channel mismatch entries with unique reasons. Each channel mismatch entry contains the list of all channels that share the same reason component,
         *         mapped to that reason component.
         */
        private Map<List<ResourceLocation>, Component> sortAndMergeChannelData(Map<ResourceLocation, Component> mismatchedChannelData) {
            Map<Component, List<ResourceLocation>> channelsByReason = new LinkedHashMap<>();
            List<ResourceLocation> sortedChannels = mismatchedChannelData.keySet().stream().sorted(Comparator.comparing(ResourceLocation::toString)).toList();
            for (ResourceLocation channel : sortedChannels) {
                Component channelMismatchReason = mismatchedChannelData.get(channel);
                if (channelsByReason.containsKey(channelMismatchReason))
                    channelsByReason.get(channelMismatchReason).add(channel);
                else
                    channelsByReason.put(channelMismatchReason, Lists.newArrayList(channel));
            }

            Map<List<ResourceLocation>, Component> channelMismatchEntries = new LinkedHashMap<>();
            List<Component> sortedChannelEntries = channelsByReason.entrySet().stream().sorted(Comparator.comparing(entry -> entry.getValue().get(0).toString())).map(Map.Entry::getKey).toList();
            for (Component mismatchReason : sortedChannelEntries) {
                channelMismatchEntries.put(channelsByReason.get(mismatchReason), mismatchReason);
            }

            return channelMismatchEntries;
        }

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

        /**
         * Formats the given list of channel ids to a component which, depending on the current display mode of the list, will list either the first or all channel ids.
         * If only one channel id is shown, the amount of channels that have the same reason component will also be displayed next to the channel id.
         * The component is colored in the given color, which will be used for the whole content row.
         *
         * @param ids   The list of channel ids to be formatted to the component. Depending on the current list mode, either the full list or the first entry will be used for the component text.
         * @param color Defines the color of the returned component.
         * @return A component with one or all entries of the channel id list as the main text component, and an assigned color which will be used for the whole content row.
         */
        private MutableComponent toChannelComponent(List<ResourceLocation> ids, ChatFormatting color) {
            MutableComponent namespaceComponent;
            if (oneChannelPerEntry) {
                namespaceComponent = Component.literal(ids.get(0).toString()).withStyle(color);

                if (ids.size() > 1)
                    namespaceComponent.append(Component.literal("\n[+%s more]".formatted(ids.size() - 1)).withStyle(ChatFormatting.DARK_GRAY));
            } else
                namespaceComponent = ComponentUtils.formatList(ids, ComponentUtils.DEFAULT_SEPARATOR, r -> Component.literal(r.toString())).withStyle(color);

            return namespaceComponent;
        }

        public void toggleSimplifiedView() {
            this.oneChannelPerEntry = !this.oneChannelPerEntry;
            updateListContent();
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
