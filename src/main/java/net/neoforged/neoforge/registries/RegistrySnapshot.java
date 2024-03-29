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
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class RegistrySnapshot {
    private static final Comparator<ResourceLocation> SORTER = ResourceLocation::compareNamespaced;
    public static final StreamCodec<FriendlyByteBuf, RegistrySnapshot> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RegistrySnapshot decode(FriendlyByteBuf buf) {
            RegistrySnapshot snapshot = new RegistrySnapshot();
            buf.readMap(size -> snapshot.ids, FriendlyByteBuf::readVarInt, FriendlyByteBuf::readResourceLocation);
            buf.readMap(size -> snapshot.aliases, FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readResourceLocation);
            return snapshot;
        }

        @Override
        public synchronized void encode(FriendlyByteBuf buf, RegistrySnapshot snapshot) {
            if (snapshot.binary == null) {
                FriendlyByteBuf pkt = new FriendlyByteBuf(Unpooled.buffer());
                try {
                    pkt.writeMap(snapshot.ids, FriendlyByteBuf::writeVarInt, FriendlyByteBuf::writeResourceLocation);
                    pkt.writeMap(snapshot.aliases, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeResourceLocation);
                    snapshot.binary = new byte[pkt.readableBytes()];
                    pkt.readBytes(snapshot.binary);
                } finally {
                    pkt.release();
                }
            }
            buf.writeBytes(snapshot.binary);
        }
    };

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
                backup.register(registry.getId(key), key, value, registry.registrationInfo(key).orElse(RegistrationInfo.BUILT_IN));
            }
            backup.freeze();
            this.fullBackup = backup;
        } else {
            this.fullBackup = null;
        }
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
