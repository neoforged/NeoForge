/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMaps;
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
    private final Object2IntSortedMap<ResourceLocation> ids = new Object2IntRBTreeMap<>(SORTER);
    private final Object2IntSortedMap<ResourceLocation> idsView = Object2IntSortedMaps.unmodifiable(this.ids);
    private final Map<ResourceLocation, ResourceLocation> aliases = new TreeMap<>(SORTER);
    private final Map<ResourceLocation, ResourceLocation> aliasesView = Collections.unmodifiableMap(this.aliases);
    @Nullable
    private final Registry<?> fullBackup;
    @Nullable
    private FriendlyByteBuf binary = null;

    /**
     * Creates a blank snapshot to populate.
     */
    private RegistrySnapshot() {
        this.fullBackup = null;
    }

    /**
     * Creates a registry snapshot based on the given registry.
     *
     * @param registry the registry to snapshot
     * @param full     if {@code true}, all entries will be stored in this snapshot.
     *                 These entries are never saved to disk nor sent to the client.
     * @param <T>      the registry type
     */
    public <T> RegistrySnapshot(Registry<T> registry, boolean full) {
        registry.keySet().forEach(key -> this.ids.put(key, registry.getId(key)));
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

    public Object2IntSortedMap<ResourceLocation> getIds() {
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

    public synchronized FriendlyByteBuf getPacketData() {
        if (this.binary == null) {
            FriendlyByteBuf pkt = new FriendlyByteBuf(Unpooled.buffer());

            pkt.writeVarInt(this.ids.size());
            this.ids.forEach((k, v) -> {
                pkt.writeResourceLocation(k);
                pkt.writeVarInt(v);
            });

            pkt.writeVarInt(this.aliases.size());
            this.aliases.forEach((k, v) -> {
                pkt.writeResourceLocation(k);
                pkt.writeResourceLocation(v);
            });

            this.binary = pkt;
        }

        return new FriendlyByteBuf(this.binary.slice());
    }

    public static RegistrySnapshot read(FriendlyByteBuf buf) {
        if (buf == null)
            return new RegistrySnapshot();

        RegistrySnapshot ret = new RegistrySnapshot();

        int len = buf.readVarInt();
        for (int x = 0; x < len; x++)
            ret.ids.put(buf.readResourceLocation(), buf.readVarInt());

        len = buf.readVarInt();
        for (int x = 0; x < len; x++)
            ret.aliases.put(buf.readResourceLocation(), buf.readResourceLocation());

        return ret;
    }
}
