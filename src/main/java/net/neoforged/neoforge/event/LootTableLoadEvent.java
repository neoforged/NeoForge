/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a {@link LootTable} is loaded from JSON.
 * Can be used to modify the loot table, cancel loading it, or outright replace it.
 * This event is currently fired for all loot tables coming from vanilla, mods, and user datapacks.
 * This event is fired whenever server resources are loaded or reloaded.
 *
 * <p>This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 * If the event is cancelled, the loot table will be made empty.</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
 */
public class LootTableLoadEvent extends Event implements ICancellableEvent {
    private final HolderLookup.Provider registries;
    private final ResourceLocation name;
    private LootTable table;

    @Nullable
    private ResourceKey<LootTable> key;

    @ApiStatus.Internal
    public LootTableLoadEvent(HolderLookup.Provider registries, ResourceLocation name, LootTable table) {
        this.registries = registries;
        this.name = name;
        this.table = table;
    }

    /**
     * {@return a lookup provider that can be used to access registries}
     */
    public HolderLookup.Provider getRegistries() {
        return this.registries;
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public ResourceKey<LootTable> getKey() {
        if (this.key == null) {
            this.key = ResourceKey.create(Registries.LOOT_TABLE, name);
        }
        return this.key;
    }

    public LootTable getTable() {
        return this.table;
    }

    public void setTable(LootTable table) {
        Objects.requireNonNull(table);
        this.table = table;
    }
}
