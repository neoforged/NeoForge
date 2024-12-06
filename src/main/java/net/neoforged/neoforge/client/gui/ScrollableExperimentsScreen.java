/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.ExperimentsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class ScrollableExperimentsScreen extends ExperimentsScreen {
    private static final int DEFAULT_LIST_HEIGHT = 121;
    private static final int ROW_PADDING = 4;
    private static final int LIST_PADDING = ROW_PADDING + 34;
    private static final int ENTRY_HEIGHT = 42;

    @Nullable
    private ExperimentSelectionList selectionList;
    @Nullable
    private LinearLayout listLayout;

    public ScrollableExperimentsScreen(Screen parent, PackRepository packRepository, Consumer<PackRepository> output) {
        super(parent, packRepository, output);
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(TITLE, this.font);

        LinearLayout contentLayout = this.layout.addToContents(LinearLayout.vertical(), LayoutSettings::alignVerticallyTop);
        contentLayout.addChild(
                new MultiLineTextWidget(INFO, this.font).setMaxWidth(MAIN_CONTENT_WIDTH),
                settings -> settings.paddingBottom(15).alignHorizontallyCenter());

        this.listLayout = contentLayout.addChild(LinearLayout.vertical());
        this.selectionList = new ExperimentSelectionList(this.minecraft);
        this.packs.forEach((pack, selected) -> selectionList.addEntry(new ExperimentSelectionList.ExperimentEntry(
                getHumanReadableTitle(pack),
                () -> this.packs.getBoolean(pack),
                flag -> this.packs.put(pack, flag.booleanValue()),
                pack.getDescription())));
        this.listLayout.addChild(selectionList);

        LinearLayout footerLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footerLayout.addChild(Button.builder(CommonComponents.GUI_DONE, btn -> this.onDone()).build());
        footerLayout.addChild(Button.builder(CommonComponents.GUI_CANCEL, btn -> this.onClose()).build());

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        if (this.selectionList != null) {
            // Reset list height to empirical default because layouts can't squish elements to fit...
            this.selectionList.setHeight(DEFAULT_LIST_HEIGHT);
        }
        super.repositionElements();
        if (this.selectionList != null && this.listLayout != null) {
            this.selectionList.setHeight(this.layout.getContentHeight() - this.listLayout.getY());
            this.selectionList.setPosition(this.listLayout.getX(), this.listLayout.getY());
            this.selectionList.refreshScrollAmount();
        }
    }

    private static class ExperimentSelectionList extends ContainerObjectSelectionList<ExperimentSelectionList.ExperimentEntry> {
        public ExperimentSelectionList(Minecraft mc) {
            super(mc, ExperimentsScreen.MAIN_CONTENT_WIDTH + LIST_PADDING, DEFAULT_LIST_HEIGHT, 0, ENTRY_HEIGHT);
        }

        @Override
        public int getRowWidth() {
            return ExperimentsScreen.MAIN_CONTENT_WIDTH + ROW_PADDING;
        }

        @Override
        protected int addEntry(ExperimentEntry entry) {
            return super.addEntry(entry);
        }

        private static class ExperimentEntry extends ContainerObjectSelectionList.Entry<ExperimentEntry> {
            private static final int BUTTON_WIDTH = 44;
            private static final int TITLE_Y = 6;
            private static final int DESCRIPTION_Y = 20;

            private final StringWidget titleWidget;
            private final MultiLineTextWidget descriptionWidget;
            private final CycleButton<Boolean> button;
            private final List<AbstractWidget> children;

            public ExperimentEntry(Component title, BooleanSupplier selectedSupplier, Consumer<Boolean> selectedSetter, Component description) {
                this.titleWidget = new StringWidget(title, Minecraft.getInstance().font).alignLeft();
                this.descriptionWidget = new MultiLineTextWidget(description.copy().withStyle(ChatFormatting.GRAY), Minecraft.getInstance().font)
                        .setMaxRows(2);
                this.button = CycleButton.onOffBuilder(selectedSupplier.getAsBoolean())
                        .displayOnlyValue()
                        .withCustomNarration(btn -> CommonComponents.joinForNarration(title, btn.createDefaultNarrationMessage(), description))
                        .create(0, 0, BUTTON_WIDTH, Button.DEFAULT_HEIGHT, Component.empty(), (btn, val) -> selectedSetter.accept(val));
                this.children = List.of(titleWidget, descriptionWidget, this.button);
            }

            @Override
            public void render(GuiGraphics graphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTick) {
                this.titleWidget.setPosition(left, top + TITLE_Y);
                this.descriptionWidget.setPosition(left, top + DESCRIPTION_Y);
                this.descriptionWidget.setMaxWidth(entryWidth - this.button.getWidth());
                this.button.setPosition(left + entryWidth - this.button.getWidth() - ROW_PADDING, top);

                this.titleWidget.render(graphics, mouseX, mouseY, partialTick);
                this.descriptionWidget.render(graphics, mouseX, mouseY, partialTick);
                this.button.render(graphics, mouseX, mouseY, partialTick);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return this.children;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return this.children;
            }
        }
    }
}
