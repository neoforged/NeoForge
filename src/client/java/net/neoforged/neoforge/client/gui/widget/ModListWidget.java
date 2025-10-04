/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.VersionChecker;
import net.neoforged.fml.i18n.MavenVersionTranslator;
import net.neoforged.neoforge.client.gui.ModListScreen;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforgespi.language.IModInfo;

public class ModListWidget extends ObjectSelectionList<ModListWidget.ModEntry> {
    private static String stripControlCodes(String value) {
        return net.minecraft.util.StringUtil.stripColor(value);
    }

    private static final ResourceLocation VERSION_CHECK_ICONS = ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "textures/gui/version_check_icons.png");
    private final int listWidth;

    private ModListScreen parent;

    public ModListWidget(ModListScreen parent, int listWidth, int top, int bottom) {
        super(parent.getMinecraftInstance(), listWidth, bottom - top, top, parent.getFontRenderer().lineHeight * 2 + 8);
        this.parent = parent;
        this.listWidth = listWidth;
        //this.setRenderBackground(false); // TODO: Porting 1.20.5 still needed?
        this.refreshList();
    }

    @Override
    protected int scrollBarX() {
        return this.listWidth;
    }

    @Override
    public int getRowWidth() {
        return this.listWidth;
    }

    public void refreshList() {
        this.clearEntries();
        parent.buildModList(this::addEntry, mod -> new ModEntry(mod, this.parent));
    }

    public class ModEntry extends ObjectSelectionList.Entry<ModEntry> {
        private final ModContainer container;
        private final ModListScreen parent;

        ModEntry(ModContainer info, ModListScreen parent) {
            this.container = info;
            this.parent = parent;
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", container.getModInfo().getDisplayName());
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            Component name = Component.literal(stripControlCodes(container.getModInfo().getDisplayName()));
            Component version = Component.literal(stripControlCodes(MavenVersionTranslator.artifactVersionToString(container.getModInfo().getVersion())));
            VersionChecker.CheckResult vercheck = VersionChecker.getResult(container.getModInfo());
            int left = getContentX();
            int top = getContentY();
            Font font = this.parent.getFontRenderer();
            guiGraphics.drawString(font, Language.getInstance().getVisualOrder(FormattedText.composite(font.substrByWidth(name, listWidth))), left + 3, top + 2, 0xFFFFFFFF, false);
            guiGraphics.drawString(font, Language.getInstance().getVisualOrder(FormattedText.composite(font.substrByWidth(version, listWidth))), left + 3, top + 2 + font.lineHeight, 0xFFCCCCCC, false);
            if (vercheck.status().shouldDraw()) {
                //TODO: Consider adding more icons for visualization
                int entryHeight = getContentHeight();
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, VERSION_CHECK_ICONS, getX() + width - 12, top + entryHeight / 4, vercheck.status().getSheetOffset() * 8, (vercheck.status().isAnimated() && ((System.currentTimeMillis() / 800 & 1)) == 1) ? 8 : 0, 8, 8, 64, 16);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            // clicking on a selected item a second time unselects it
            parent.setSelected(isFocused() ? null : this);
            ModListWidget.this.setSelected(isFocused() ? null : this);
            return false;
        }

        @Override
        public void setFocused(boolean focused) {
            // ignore focus loss so the item stays selected when tabbing to the config button
            if (focused) {
                parent.setSelected(this);
                ModListWidget.this.setSelected(this);
            }
        }

        @Override
        public boolean isFocused() {
            return ModListWidget.this.getSelected() == this;
        }

        public IModInfo getInfo() {
            return container.getModInfo();
        }

        public ModContainer getContainer() {
            return container;
        }
    }
}
