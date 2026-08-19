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
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public class RegistrySnapshot {
    private static final Comparator<Identifier> SORTER = Identifier::compareNamespaced;
    private static final StreamCodec<FriendlyByteBuf, RegistrySnapshot> UNCACHED_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(_ -> new Int2ObjectRBTreeMap<>(), ByteBufCodecs.VAR_INT, Identifier.STREAM_CODEC),
            registrySnapshot -> registrySnapshot.ids,
            ByteBufCodecs.map(_ -> new TreeMap<>(SORTER), Identifier.STREAM_CODEC, Identifier.STREAM_CODEC),
            registrySnapshot -> registrySnapshot.aliases,
            RegistrySnapshot::new);
    public static final StreamCodec<FriendlyByteBuf, RegistrySnapshot> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RegistrySnapshot decode(FriendlyByteBuf buf) {
            return UNCACHED_STREAM_CODEC.decode(buf);
        }

        @Override
        public synchronized void encode(FriendlyByteBuf buf, RegistrySnapshot snapshot) {
            if (snapshot.binary == null) {
                FriendlyByteBuf pkt = new FriendlyByteBuf(Unpooled.buffer());
                try {
                    UNCACHED_STREAM_CODEC.encode(pkt, snapshot);
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
    private final Int2ObjectSortedMap<Identifier> ids;
    private final Int2ObjectSortedMap<Identifier> idsView = Int2ObjectSortedMaps.unmodifiable(this.ids);
    private final Map<Identifier, Identifier> aliases;
    private final Map<Identifier, Identifier> aliasesView = Collections.unmodifiableMap(this.aliases);
    @Nullable
    private final Registry<?> fullBackup;
    private byte @Nullable [] binary = null;

    /// Creates a snapshot from the given data recieved over the network.
    private RegistrySnapshot(Int2ObjectSortedMap<Identifier> ids, Map<Identifier, Identifier> aliases) {
        this.ids = ids;
        this.aliases = aliases;
        this.fullBackup = null;
        super();
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
        this.ids = new Int2ObjectRBTreeMap<>();
        this.aliases = new TreeMap<>(SORTER);
        super();

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

    public Int2ObjectSortedMap<Identifier> getIds() {
        return this.idsView;
    }

    public Map<Identifier, Identifier> getAliases() {
        return this.aliasesView;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> Registry<T> getFullBackup() {
        return (Registry<T>) this.fullBackup;
    }
}
