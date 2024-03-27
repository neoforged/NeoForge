/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.flag.FeatureFlagSet;
import org.jetbrains.annotations.Nullable;

public final class InMemoryResourcePack implements PackResources {
    private final Map<PackType, Map<ResourceLocation, Supplier<byte[]>>> data = new EnumMap<>(PackType.class);
    private final Map<String, Supplier<byte[]>> root = new ConcurrentHashMap<>();
    private final Map<String, Object> packMetadata = new ConcurrentHashMap<>();
    private final String id;

    public InMemoryResourcePack(String id) {
        this.id = id;

        for (PackType packType : PackType.values()) {
            this.data.put(packType, new ConcurrentHashMap<>());
        }

        putMeta(PackMetadataSection.TYPE, new PackMetadataSection(Component.literal("A virtual resource pack."), SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA)));
    }

    public static Pack createInMemoryResourcePack(String id, Component title, Component description, boolean required, boolean hidden, boolean fixedPosition, FeatureFlagSet requestedFeatures, List<String> overlays, Pack.Position position, Consumer<InMemoryResourcePack> populator) {
        InMemoryResourcePack pack = new InMemoryResourcePack(id);
        populator.accept(pack);
        return Pack.create(
                pack.packId(),
                title,
                required,
                BuiltInPackSource.fixedResources(pack),
                new Pack.Info(
                        description,
                        PackCompatibility.COMPATIBLE,
                        requestedFeatures,
                        overlays,
                        hidden),
                position,
                fixedPosition,
                PackSource.BUILT_IN);
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... loc) {
        return openResource(this.root, String.join("/", loc));
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation loc) {
        return openResource(this.data.get(type), loc);
    }

    private <T> @Nullable IoSupplier<InputStream> openResource(Map<T, Supplier<byte[]>> map, T key) {
        final Supplier<byte[]> supplier = map.get(key);
        if (supplier == null) {
            return null;
        }
        final byte[] bytes = supplier.get();
        if (bytes == null) {
            return null;
        }
        return () -> new ByteArrayInputStream(bytes);
    }

    @Override
    public void listResources(PackType type, String namespace, String startingPath, ResourceOutput out) {
        this.data.get(type).forEach((key, data) -> {
            if (!key.getNamespace().equals(namespace) || !key.getPath().startsWith(startingPath)) return;
            final byte[] bytes = data.get();
            if (bytes == null) return;
            out.accept(key, () -> new ByteArrayInputStream(bytes));
        });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return this.data.get(type)
                .keySet()
                .stream()
                .map(ResourceLocation::getNamespace)
                .collect(Collectors.toUnmodifiableSet());
    }

    public void putRoot(String path, JsonObject json) {
        final byte[] bytes = fromJson(json);
        putRoot(path, () -> bytes);
    }

    public void putRoot(String path, Supplier<byte[]> data) {
        this.root.put(path, data);
    }

    public <T> void putJsonData(PackType type, ResourceLocation path, Codec<T> codec, T data) {
        putData(type, path, codec, JsonOps.INSTANCE, data);
    }

    public <T> void putData(PackType type, ResourceLocation path, Codec<T> codec, DynamicOps<JsonElement> ops, T data) {
        putData(type, path, Util.getOrThrow(codec.encodeStart(ops, data), IllegalArgumentException::new));
    }

    public void putData(PackType type, ResourceLocation path, JsonElement json) {
        final byte[] bytes = fromJson(json);
        putData(type, path, () -> bytes);
    }

    public void putData(PackType type, ResourceLocation path, Supplier<byte[]> data) {
        this.data.get(type).put(path, data);
    }

    public static byte[] fromJson(JsonElement json) {
        return GsonHelper.toStableString(json).getBytes(StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> section) throws IOException {
        return (T) this.packMetadata.get(section.getMetadataSectionName());
    }

    @Override
    public String packId() {
        return id;
    }

    @Override
    public void close() {}

    @Override
    public boolean isBuiltin() {
        return true;
    }

    public <T> void putMeta(MetadataSectionSerializer<T> serializer, T data) {
        this.packMetadata.put(serializer.getMetadataSectionName(), data);
    }
}
