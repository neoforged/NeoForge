/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMaps;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nullable;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class RegistrySnapshot {
    private static final Comparator<ResourceLocation> SORTER = ResourceLocation::compareNamespaced;
    // Use a sorted map with the ID as the key.
    // We need the entries to be sorted by increasing order for client-side application of the snapshot to work.
    private final Int2ObjectSortedMap<ResourceLocation> ids = new Int2ObjectRBTreeMap<>();
    private final Int2ObjectSortedMap<ResourceLocation> idsView = Int2ObjectSortedMaps.unmodifiable(this.ids);
    private final Map<ResourceLocation, ResourceLocation> aliases = new TreeMap<>(SORTER);
    private final Map<ResourceLocation, ResourceLocation> aliasesView = Collections.unmodifiableMap(this.aliases);
    @Nullable
    private final Registry<?> fullBackup;
    @Nullable
    private byte[] binary = null;

    /**
     * Creates a blank snapshot to populate.
     */
    private RegistrySnapshot() {
        this.fullBackup = null;
    }

    /**
     * Creates a registry snapshot based on the given registry.
     *
     * @param registry the registry to snapshot.
     * @param full     if {@code true}, all entries will be stored in this snapshot.
     *                 These entries are never saved to disk nor sent to the client.
     * @param <T>      the registry type
     */
    public <T> RegistrySnapshot(Registry<T> registry, boolean full) {
        registry.keySet().forEach(key -> this.ids.put(registry.getId(key), key));
        this.aliases.putAll(((BaseMappedRegistry<T>) registry).aliases);

        if (full) {
            MappedRegistry<T> backup = new MappedRegistry<>(registry.key(), registry.registryLifecycle());
            for (var entry : registry.entrySet()) {
                ResourceKey<T> key = entry.getKey();
                T value = entry.getValue();
                backup.registerMapping(registry.getId(key), key, value, registry.lifecycle(value));
            }
            backup.freeze();
            this.fullBackup = backup;
        } else {
            this.fullBackup = null;
        }
    }

    /**
     * Creates a registry snapshot from the received buffer.
     * 
     * @param buf the buffer containing the data of the received snapshot.
     */
    public RegistrySnapshot(FriendlyByteBuf buf) {
        this();
        buf.readMap(size -> this.ids, FriendlyByteBuf::readVarInt, FriendlyByteBuf::readResourceLocation);
        buf.readMap(size -> this.aliases, FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readResourceLocation);
    }

    /**
     * Write the registry snapshot to the given buffer and cache the binary data.
     * 
     * @param buf the buffer to write to.
     */
    public synchronized void write(FriendlyByteBuf buf) {
        if (this.binary == null) {
            FriendlyByteBuf pkt = new FriendlyByteBuf(Unpooled.buffer());
            try {
                pkt.writeMap(this.ids, FriendlyByteBuf::writeVarInt, FriendlyByteBuf::writeResourceLocation);
                pkt.writeMap(this.aliases, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeResourceLocation);
                this.binary = new byte[pkt.readableBytes()];
                pkt.readBytes(this.binary);
            } finally {
                pkt.release();
            }
        }
        buf.writeBytes(this.binary);
    }

    public Int2ObjectSortedMap<ResourceLocation> getIds() {
        return this.idsView;
    }

    public Map<ResourceLocation, ResourceLocation> getAliases() {
        return this.aliasesView;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> Registry<T> getFullBackup() {
        return (Registry<T>) this.fullBackup;
    }
}
