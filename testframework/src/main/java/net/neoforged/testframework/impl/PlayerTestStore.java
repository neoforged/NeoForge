/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

public class PlayerTestStore extends SavedData {
    public static final Factory<PlayerTestStore> FACTORY = new Factory<>(PlayerTestStore::new, (compoundTag, provider) -> new PlayerTestStore().decode(compoundTag));

    private final Map<UUID, Set<String>> playerToTests = new HashMap<>();

    @Nullable
    public Set<String> getLast(UUID uuid) {
        return playerToTests.get(uuid);
    }

    public void put(UUID uuid, Collection<String> tests) {
        playerToTests.put(uuid, new HashSet<>(tests));
        setDirty(true);
    }

    public PlayerTestStore decode(CompoundTag tag) {
        final CompoundTag testsTag = tag.getCompound("tests");
        testsTag.getAllKeys().forEach(uuid -> {
            put(UUID.fromString(uuid), testsTag.getList(uuid, Tag.TAG_STRING).stream().map(Tag::getAsString).toList());
        });
        return this;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        final CompoundTag testsTag = new CompoundTag();
        playerToTests.forEach((uuid, tests) -> {
            final ListTag testsNbt = new ListTag();
            tests.forEach(it -> testsNbt.add(StringTag.valueOf(it)));
            testsTag.put(uuid.toString(), testsNbt);
        });
        tag.put("tests", testsTag);
        return tag;
    }

    @Override
    public void save(File file, HolderLookup.Provider prov) {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        super.save(file, prov);
    }
}
