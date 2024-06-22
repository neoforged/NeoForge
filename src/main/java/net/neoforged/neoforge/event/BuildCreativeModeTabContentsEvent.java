/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.common.util.InsertableLinkedOpenCustomHashSet;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when the contents of a specific creative mode tab are being populated in {@link CreativeModeTab#buildContents(CreativeModeTab.ItemDisplayParameters)}.
 * <p>
 * This event may be fired multiple times if the operator status of the local player or enabled feature flags changes.
 * <p>
 * In vanilla, this is only fired on the logical client, but mods may request creative mode tab contents on the server.
 */
public final class BuildCreativeModeTabContentsEvent extends Event implements IModBusEvent, CreativeModeTab.Output {
    private final CreativeModeTab tab;
    private final CreativeModeTab.ItemDisplayParameters parameters;
    private final InsertableLinkedOpenCustomHashSet<ItemStack> parentEntries;
    private final InsertableLinkedOpenCustomHashSet<ItemStack> searchEntries;
    private final ResourceKey<CreativeModeTab> tabKey;

    @ApiStatus.Internal
    public BuildCreativeModeTabContentsEvent(CreativeModeTab tab, ResourceKey<CreativeModeTab> tabKey, CreativeModeTab.ItemDisplayParameters parameters, InsertableLinkedOpenCustomHashSet<ItemStack> parentEntries, InsertableLinkedOpenCustomHashSet<ItemStack> searchEntries) {
        this.tab = tab;
        this.tabKey = tabKey;
        this.parameters = parameters;
        this.parentEntries = parentEntries;
        this.searchEntries = searchEntries;
    }

    /**
     * {@return the creative mode tab currently populating its contents}
     */
    public CreativeModeTab getTab() {
        return this.tab;
    }

    /**
     * {@return the key of the creative mode tab currently populating its contents}
     */
    public ResourceKey<CreativeModeTab> getTabKey() {
        return this.tabKey;
    }

    public FeatureFlagSet getFlags() {
        return this.parameters.enabledFeatures();
    }

    public CreativeModeTab.ItemDisplayParameters getParameters() {
        return parameters;
    }

    public boolean hasPermissions() {
        return this.parameters.hasPermissions();
    }

    /**
     * The current immutable list of the parent tab entries in the order to be added to the Creative Menu.
     * Purely for querying to see what in it. Please use the other event methods for modifications.
     */
    public List<ItemStack> getParentEntries() {
        return this.parentEntries.stream().toList();
    }

    /**
     * The current immutable list of the search tab entries in the order to be added to the Creative Menu.
     * Purely for querying to see what in it. Please use the other event methods for modifications.
     */
    public List<ItemStack> getSearchEntries() {
        return this.searchEntries.stream().toList();
    }

    /**
     * Inserts the new stack at the end of the given tab at this point in time.
     * 
     * @exception IllegalArgumentException if the new itemstack's count is not 1.
     */
    @Override
    public void accept(ItemStack newStack, CreativeModeTab.TabVisibility visibility) {
        if (newStack.getCount() != 1)
            throw new IllegalArgumentException("The stack count must be 1");

        if (isParentTab(visibility)) {
            parentEntries.add(newStack);
        }

        if (isSearchTab(visibility)) {
            searchEntries.add(newStack);
        }
    }

    /**
     * Inserts the new entry after the specified existing entry.
     * 
     * @exception UnsupportedOperationException if the existing entry is not found in the tab's lists.
     * @exception IllegalArgumentException      if the new itemstack's count is not 1.
     */
    public void putAfter(ItemStack existingEntry, ItemStack newEntry, CreativeModeTab.TabVisibility visibility) {
        if (newEntry.getCount() != 1)
            throw new IllegalArgumentException("The stack count must be 1");

        if (isParentTab(visibility)) {
            parentEntries.addBefore(existingEntry, newEntry);
        }

        if (isSearchTab(visibility)) {
            searchEntries.addBefore(existingEntry, newEntry);
        }
    }

    /**
     * Inserts the new entry before the specified existing entry.
     * 
     * @exception UnsupportedOperationException if the existing entry is not found in the tab's lists.
     * @exception IllegalArgumentException      if the new itemstack's count is not 1.
     */
    public void putBefore(ItemStack existingEntry, ItemStack newEntry, CreativeModeTab.TabVisibility visibility) {
        if (newEntry.getCount() != 1)
            throw new IllegalArgumentException("The stack count must be 1");

        if (isParentTab(visibility)) {
            parentEntries.addAfter(existingEntry, newEntry);
        }

        if (isSearchTab(visibility)) {
            searchEntries.addAfter(existingEntry, newEntry);
        }
    }

    /**
     * Inserts the new entry in the front of the tab's content.
     * 
     * @exception IllegalArgumentException if the new itemstack's count is not 1.
     */
    public void putFirst(ItemStack newEntry, CreativeModeTab.TabVisibility visibility) {
        if (newEntry.getCount() != 1)
            throw new IllegalArgumentException("The stack count must be 1");

        if (isParentTab(visibility)) {
            parentEntries.addFirst(newEntry);
        }

        if (isSearchTab(visibility)) {
            searchEntries.addFirst(newEntry);
        }
    }

    /**
     * Removes an entry from the tab's content.
     */
    public void remove(ItemStack existingEntry, CreativeModeTab.TabVisibility visibility) {
        if (isParentTab(visibility)) {
            parentEntries.remove(existingEntry);
        }

        if (isSearchTab(visibility)) {
            searchEntries.remove(existingEntry);
        }
    }

    static boolean isParentTab(CreativeModeTab.TabVisibility visibility) {
        return visibility == CreativeModeTab.TabVisibility.PARENT_TAB_ONLY || visibility == CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
    }

    static boolean isSearchTab(CreativeModeTab.TabVisibility visibility) {
        return visibility == CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY || visibility == CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
    }
}
