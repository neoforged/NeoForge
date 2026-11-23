/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class CreativeModeTabPageManager {
    private static final int TABS_PER_ROW = 5;
    private static final int TABS_PER_PAGE = TABS_PER_ROW * 2;
    private static final int NAV_BTN_Y_OFF = 50;
    private static final int NAV_BTN_SIZE = 20;

    private final CreativeModeInventoryScreen owner;
    private final Runnable rebuildWidgets;
    private final List<List<CreativeModeTab>> pages = new ArrayList<>();
    private final Button buttonDecrement;
    private final Button buttonIncrement;
    private List<CreativeModeTab> currentPage = List.of();
    private int currentPageIdx = 0;

    public CreativeModeTabPageManager(CreativeModeInventoryScreen owner, Runnable rebuildWidgets) {
        this.owner = owner;
        this.rebuildWidgets = rebuildWidgets;
        this.buttonDecrement = Button.builder(Component.literal("<"), this::decrementPage).size(NAV_BTN_SIZE, NAV_BTN_SIZE).build();
        this.buttonIncrement = Button.builder(Component.literal(">"), this::incrementPage).size(NAV_BTN_SIZE, NAV_BTN_SIZE).build();
    }

    public void update(CreativeModeTab selectedTab, boolean initial) {
        List<List<CreativeModeTab>> newPages = new ArrayList<>();
        List<CreativeModeTab> page = new ReferenceArrayList<>(TABS_PER_PAGE);
        int selectedPageIndex = 0;
        for (CreativeModeTab tab : CreativeModeTabRegistry.getSortedCreativeModeTabs()) {
            if (!tab.shouldDisplay()) {
                continue;
            }
            if (tab == selectedTab) {
                selectedPageIndex = newPages.size();
            }
            page.add(tab);
            if (page.size() == TABS_PER_PAGE) {
                newPages.add(page);
                page = new ReferenceArrayList<>(TABS_PER_PAGE);
            }
        }
        if (!page.isEmpty() || newPages.isEmpty()) {
            newPages.add(page);
        }
        if (!newPages.equals(pages)) {
            pages.clear();
            pages.addAll(newPages);
            setPage(selectedPageIndex, !initial);
        }
    }

    public boolean hasMultiplePages() {
        return pages.size() > 1;
    }

    public Iterable<CreativeModeTab> getCurrentTabs() {
        return Iterables.filter(Iterables.concat(currentPage, CreativeModeTabRegistry.getDefaultTabs()), CreativeModeTab::shouldDisplay);
    }

    public boolean isTop(CreativeModeTab tab) {
        int idx = currentPage.indexOf(tab);
        if (idx != -1) {
            return idx < TABS_PER_ROW;
        }
        return tab.row() == CreativeModeTab.Row.TOP;
    }

    public int getColumn(CreativeModeTab tab) {
        int idx = currentPage.indexOf(tab);
        if (idx != -1) {
            return idx % TABS_PER_ROW;
        }
        return tab.column();
    }

    public void addNavButtons(Consumer<AbstractWidget> widgetAppender) {
        if (!hasMultiplePages()) {
            return;
        }

        buttonDecrement.setPosition(owner.getLeftPos(), owner.getTopPos() - NAV_BTN_Y_OFF);
        buttonIncrement.setPosition(owner.getLeftPos() + owner.getImageWidth() - NAV_BTN_SIZE, owner.getTopPos() - NAV_BTN_Y_OFF);
        widgetAppender.accept(buttonDecrement);
        widgetAppender.accept(buttonIncrement);
    }

    public Component getPageIndicator() {
        return Component.literal(String.format("%d / %d", currentPageIdx + 1, pages.size()));
    }

    private void decrementPage(Button btn) {
        if (currentPageIdx > 0) {
            setPage(currentPageIdx - 1, true);
        }
    }

    private void incrementPage(Button btn) {
        if (currentPageIdx < pages.size() - 1) {
            setPage(currentPageIdx + 1, true);
        }
    }

    private void setPage(int page, boolean reInit) {
        currentPageIdx = page;
        currentPage = pages.get(currentPageIdx);
        buttonDecrement.active = page > 0;
        buttonIncrement.active = page < pages.size() - 1;

        if (reInit) {
            GuiEventListener focused = owner.getFocused();
            rebuildWidgets.run();
            // Widget re-init resets focus -> copy previous focus when keyboard was used to ensure
            // the user can continue switching pages without manually re-focusing the nav button
            if (Minecraft.getInstance().getLastInputType().isKeyboard()) {
                owner.setFocused(focused);
            }
        }
    }
}
