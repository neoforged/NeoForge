/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

public class PlayerTestStore extends SavedData {
    public static final Codec<PlayerTestStore> FACTORY = ExtraCodecs.converter(NbtOps.INSTANCE)
            .xmap(PlayerTestStore::decode, PlayerTestStore::save);

    private final Map<UUID, Set<String>> playerToTests = new HashMap<>();

    @Nullable
    public Set<String> getLast(UUID uuid) {
        return playerToTests.get(uuid);
    }

    public void put(UUID uuid, Collection<String> tests) {
        playerToTests.put(uuid, new HashSet<>(tests));
        setDirty(true);
    }

    public static PlayerTestStore decode(Tag tag) {
        var compound = (CompoundTag) tag;

        var store = new PlayerTestStore();
        final CompoundTag testsTag = compound.getCompoundOrEmpty("tests");
        testsTag.keySet().forEach(uuid -> {
            store.put(UUID.fromString(uuid), testsTag.getListOrEmpty(uuid).stream().map(Tag::asString).filter(Optional::isPresent).map(Optional::get).toList());
        });
        return store;
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        final CompoundTag testsTag = new CompoundTag();
        playerToTests.forEach((uuid, tests) -> {
            final ListTag testsNbt = new ListTag();
            tests.forEach(it -> testsNbt.add(StringTag.valueOf(it)));
            testsTag.put(uuid.toString(), testsNbt);
        });
        tag.put("tests", testsTag);
        return tag;
    }
}
