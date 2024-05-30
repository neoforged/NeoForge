/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.i18n.FMLTranslations;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class LoadingErrorScreen extends ErrorScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Path modsDir;
    private final Path logFile;
    private final List<FormattedIssue> modLoadErrors;
    private final List<FormattedIssue> modLoadWarnings;
    @Nullable
    private final Path dumpedLocation;
    private LoadingEntryList entryList;
    private Component errorHeader;
    private Component warningHeader;

    public LoadingErrorScreen(List<ModLoadingIssue> issues, @Nullable File dumpedLocation) {
        super(Component.literal("Loading Error"), null);
        this.modLoadWarnings = issues.stream()
                .filter(issue -> issue.severity() == ModLoadingIssue.Severity.WARNING)
                .map(FormattedIssue::of)
                .toList();
        this.modLoadErrors = issues.stream()
                .filter(issue -> issue.severity() == ModLoadingIssue.Severity.ERROR)
                .map(FormattedIssue::of)
                .toList();
        this.modsDir = FMLPaths.MODSDIR.get();
        this.logFile = FMLPaths.GAMEDIR.get().resolve(Paths.get("logs", "latest.log"));
        this.dumpedLocation = dumpedLocation != null ? dumpedLocation.toPath() : null;
    }

    @Override
    public void init() {
        super.init();
        this.clearWidgets();

        this.errorHeader = Component.literal(ChatFormatting.RED + FMLTranslations.parseMessage("fml.loadingerrorscreen.errorheader", this.modLoadErrors.size()) + ChatFormatting.RESET);
        this.warningHeader = Component.literal(ChatFormatting.YELLOW + FMLTranslations.parseMessage("fml.loadingerrorscreen.warningheader", this.modLoadWarnings.size()) + ChatFormatting.RESET);

        int yOffset = 46;
        this.addRenderableWidget(new ExtendedButton(50, this.height - yOffset, this.width / 2 - 55, 20, Component.literal(FMLTranslations.parseMessage("fml.button.open.mods.folder")), b -> Util.getPlatform().openFile(modsDir.toFile())));
        this.addRenderableWidget(new ExtendedButton(this.width / 2 + 5, this.height - yOffset, this.width / 2 - 55, 20, Component.literal(FMLTranslations.parseMessage("fml.button.open.log")), b -> Util.getPlatform().openFile(logFile.toFile())));
        if (this.modLoadErrors.isEmpty()) {
            this.addRenderableWidget(new ExtendedButton(50, this.height - 24, this.width / 2 - 55, 20, Component.literal(FMLTranslations.parseMessage("fml.button.continue.launch")), b -> {
                this.minecraft.setScreen(null);
            }));
        } else {
            this.addRenderableWidget(new ExtendedButton(50, this.height - 24, this.width / 2 - 55, 20, Component.literal(FMLTranslations.parseMessage("fml.button.open.crashreport")), b -> Util.getPlatform().openFile(dumpedLocation.toFile())));
        }
        this.addRenderableWidget(new ExtendedButton(this.width / 2 + 5, this.height - 24, this.width / 2 - 55, 20, Component.translatable("menu.quit"), b -> this.minecraft.stop()));

        this.entryList = new LoadingEntryList(this, this.modLoadErrors, this.modLoadWarnings);
        this.addWidget(this.entryList);
        this.setFocused(this.entryList);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.entryList.render(guiGraphics, mouseX, mouseY, partialTick);
        drawMultiLineCenteredString(guiGraphics, font, this.modLoadErrors.isEmpty() ? warningHeader : errorHeader, this.width / 2, 10);
        this.renderables.forEach(button -> button.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    private void drawMultiLineCenteredString(GuiGraphics guiGraphics, Font fr, Component str, int x, int y) {
        for (FormattedCharSequence s : fr.split(str, this.width)) {
            guiGraphics.drawString(fr, s, (float) (x - fr.width(s) / 2.0), (float) y, 0xFFFFFF, true);
            y += fr.lineHeight;
        }
    }

    public static class LoadingEntryList extends ObjectSelectionList<LoadingEntryList.LoadingMessageEntry> {
        LoadingEntryList(final LoadingErrorScreen parent, final List<FormattedIssue> errors, final List<FormattedIssue> warnings) {
            super(Objects.requireNonNull(parent.minecraft), parent.width, parent.height - 85, 35,
                    Math.max(
                            errors.stream().mapToInt(error -> parent.font.split(error.text, parent.width - 20).size()).max().orElse(0),
                            warnings.stream().mapToInt(warning -> parent.font.split(warning.text, parent.width - 20).size()).max().orElse(0)) * parent.minecraft.font.lineHeight + 8);
            boolean both = !errors.isEmpty() && !warnings.isEmpty();
            if (both)
                addEntry(new LoadingMessageEntry(parent.errorHeader, true));
            errors.forEach(e -> addEntry(new LoadingMessageEntry(e.text)));
            if (both) {
                addEntry(new LoadingMessageEntry(parent.warningHeader, true));
            }
            warnings.forEach(w -> addEntry(new LoadingMessageEntry(w.text)));
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getRight() - 6;
        }

        @Override
        public int getRowWidth() {
            return this.width - 15;
        }

        public class LoadingMessageEntry extends ObjectSelectionList.Entry<LoadingMessageEntry> {
            private final Component message;
            private final boolean center;

            LoadingMessageEntry(final Component message) {
                this(message, false);
            }

            LoadingMessageEntry(final Component message, final boolean center) {
                this.message = Objects.requireNonNull(message);
                this.center = center;
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", message);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int entryIdx, int top, int left, final int entryWidth, final int entryHeight, final int mouseX, final int mouseY, final boolean p_194999_5_, final float partialTick) {
                Font font = Minecraft.getInstance().font;
                final List<FormattedCharSequence> strings = font.split(message, LoadingEntryList.this.width - 20);
                int y = top + 2;
                for (FormattedCharSequence string : strings) {
                    if (center)
                        guiGraphics.drawString(font, string, left + (width - font.width(string)) / 2F, (float) y, 0xFFFFFF, false);
                    else
                        guiGraphics.drawString(font, string, left + 5, y, 0xFFFFFF, false);
                    y += font.lineHeight;
                }
            }
        }
    }

    private record FormattedIssue(Component text, ModLoadingIssue issue) {
        public static FormattedIssue of(ModLoadingIssue issue) {
            return new FormattedIssue(
                    Component.literal(FMLTranslations.translateIssue(issue)),
                    issue);
        }
    }
}
