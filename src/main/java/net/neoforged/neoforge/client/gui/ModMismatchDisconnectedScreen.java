/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import net.neoforged.neoforge.common.I18nExtension;
import org.apache.commons.lang3.tuple.Pair;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModMismatchDisconnectedScreen extends Screen {
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    private final Screen parent;
    private int textHeight;
    private final Path modsDir;
    private final Path logFile;
    private final int listHeight = 140;
    private final Map<ResourceLocation, Component> mismatchedChannelData;
    
    public ModMismatchDisconnectedScreen(Screen parentScreen, Component title, Map<ResourceLocation, Component> mismatchedChannelData) {
        super(title);
        this.parent = parentScreen;
        this.modsDir = FMLPaths.MODSDIR.get();
        this.logFile = FMLPaths.GAMEDIR.get().resolve(Paths.get("logs", "latest.log"));
        this.mismatchedChannelData = mismatchedChannelData;
    }
    
    @Override
    protected void init() {
        //this.message = MultiLineLabel.create(this.font, this.reason, this.width - 50);
        this.textHeight = this.message.getLineCount() * 9;
        
        int listLeft = Math.max(8, this.width / 2 - 220);
        int listWidth = Math.min(440, this.width - 16);
        int upperButtonHeight = Math.min((this.height + this.listHeight + this.textHeight) / 2 + 10, this.height - 50);
        int lowerButtonHeight = Math.min((this.height + this.listHeight + this.textHeight) / 2 + 35, this.height - 25);
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
        int textYOffset = 18;
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, (this.height - this.listHeight - this.textHeight) / 2 - textYOffset - 9 * 2, 0xAAAAAA);
        this.message.renderCentered(guiGraphics, this.width / 2, (this.height - this.listHeight - this.textHeight) / 2 - textYOffset);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
    
    class MismatchInfoPanel extends ScrollPanel {
        private final List<Pair<FormattedCharSequence, FormattedCharSequence>> lineTable;
        private final int contentSize;
        private final int nameIndent = 10;
        private final int tableWidth = width - border * 2 - 6 - nameIndent;
        private final int nameWidth = tableWidth * 3 / 5;
        private final int versionWidth = (tableWidth - nameWidth) / 2;
        
        public MismatchInfoPanel(Minecraft client, int width, int height, int top, int left) {
            super(client, width, height, top, left);
            
            //The raw list of the strings in a table row, the components may still be too long for the final table and will be split up later. The first row element may have a style assigned to it that will be used for the whole content row.
            record Row(MutableComponent name, MutableComponent reason) {
            }
            List<Row> rows = new ArrayList<>();
            if (!mismatchedChannelData.isEmpty()) {
                //This table section contains the mod name and both mod versions of each mod that has a mismatching client and server version
                rows.add(new Row(Component.translatable("fml.modmismatchscreen.table.channelname"), Component.translatable("fml.modmismatchscreen.table.reason")));
                int i = 0;
                for (Map.Entry<ResourceLocation, Component> modData : mismatchedChannelData.entrySet()) {
                    rows.add(new Row(toModNameComponent(modData.getKey()), modData.getValue().copy()));
                    if (++i >= 10) {
                        //If too many mismatched mod entries are present, append a line referencing how to see the full list and stop rendering any more entries
                        rows.add(new Row(Component.literal(""), Component.translatable("fml.modmismatchscreen.additional", mismatchedChannelData.size() - i).withStyle(ChatFormatting.ITALIC)));
                        break;
                    }
                }
                rows.add(new Row(Component.literal(""), Component.literal(""))); //Add one line of padding.
            }
            
            this.lineTable = rows.stream().flatMap(p -> splitLineToWidth(p.name(), p.reason()).stream()).collect(Collectors.toList());
            this.contentSize = lineTable.size();
        }
        
        /**
         * Splits the raw name and version strings, making them use multiple lines if needed, to fit within the table dimensions.
         * The style assigned to the name element is then applied to the entire content row.
         *
         * @param name     The first element of the content row, usually representing a table section header or the name of a mod entry
         * @param reason The second element of the content row, usually representing  the reason why the mod is mismatched
         * @return A list of table rows consisting of 2 elements each which consist of the same content as was given by the parameters, but split up to fit within the table dimensions.
         */
        private List<Pair<FormattedCharSequence, FormattedCharSequence>> splitLineToWidth(MutableComponent name, MutableComponent reason) {
            Style style = name.getStyle();
            int versionColumns = 1;
            int adaptedNameWidth = nameWidth + versionWidth * (2 - versionColumns) - 4; //the name width may be expanded when the version column string is missing
            List<FormattedCharSequence> nameLines = font.split(name, adaptedNameWidth);
            List<FormattedCharSequence> reasonLines = font.split(reason.setStyle(style), versionWidth - 4);
            List<Pair<FormattedCharSequence, FormattedCharSequence>> splitLines = new ArrayList<>();
            
            int rowsOccupied = Math.max(nameLines.size(), reasonLines.size());
            for (int i = 0; i < rowsOccupied; i++) {
                splitLines.add(Pair.of(i < nameLines.size() ? nameLines.get(i) : FormattedCharSequence.EMPTY, i < reasonLines.size() ? reasonLines.get(i) : FormattedCharSequence.EMPTY));
            }
            return splitLines;
        }
        
        /**
         * Adds a style information to the given mod name string. The style assigned to the returned component contains the color of the mod name,
         * a hover event containing the given id, and an optional click event, which opens the homepage of mod, if present.
         *
         * @param id An id that gets displayed in the hover event. Depending on the origin it may only consist of a namespace (the mod id) or a namespace + path (a channel id associated with the mod).
         * @return A component with the mod name as the main text component, and an assigned style which will be used for the whole content row.
         */
        private MutableComponent toModNameComponent(ResourceLocation id) {
            String modId = id.getNamespace();
            String modName = ModList.get().getModContainerById(modId)
                                     .map(container -> container.getModInfo().getDisplayName())
                                     .orElse(modId);
            
            String url = ModList.get().getModContainerById(modId)
                                 .flatMap(container -> container.getModInfo().getModURL())
                                 .map(URL::toString)
                                 .orElse("");
            MutableComponent result = Component.literal(modName).withStyle(ChatFormatting.YELLOW);
            if (!url.isEmpty()) {
                result = result.withStyle(s -> s.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, Component.translatable("fml.modmismatchscreen.table.visit.mod_page", id.toString()))))
                                 .withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
            }
            
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
                FormattedCharSequence reaons = line.getRight();
                //Since font#draw does not respect the color of the given component, we have to read it out here and then use it as the last parameter
                int color = Optional.ofNullable(font.getSplitter().componentStyleAtWidth(name, 0)).map(Style::getColor).map(TextColor::getValue).orElse(0xFFFFFF);
                //Only indent the given name if a version string is present. This makes it easier to distinguish table section headers and mod entries
                int nameLeft = left + border + (reaons == null ? 0 : nameIndent);
                guiGraphics.drawString(font, name, nameLeft, relativeY + i * 12, color, false);
                if (reaons != null) {
                    guiGraphics.drawString(font, reaons, left + border + nameIndent + nameWidth, relativeY + i * 12, color, false);
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
        public void updateNarration(NarrationElementOutput output) {
        }
    }
}
