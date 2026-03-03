/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.extensions.ILevelExtension;

public abstract class LanguageProvider implements DataProvider {
    private static final Codec<Map<String, Component>> CODEC = Codec.unboundedMap(Codec.STRING, ComponentSerialization.CODEC);

    private final Map<String, Component> data = new TreeMap<>();
    private final PackOutput output;
    private final String modid;
    private final String locale;

    public LanguageProvider(PackOutput output, String modid, String locale) {
        this.output = output;
        this.modid = modid;
        this.locale = locale;
    }

    protected abstract void addTranslations();

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        addTranslations();

        if (!data.isEmpty())
            return save(cache, this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(this.modid).resolve("lang").resolve(this.locale + ".json"));

        return CompletableFuture.allOf();
    }

    @Override
    public String getName() {
        return "Languages: " + locale + " for mod: " + modid;
    }

    private CompletableFuture<?> save(CachedOutput cache, Path target) {
        final JsonElement json = CODEC.encode(this.data, JsonOps.INSTANCE, new JsonObject()).getOrThrow();
        return DataProvider.saveStable(cache, json, target);
    }

    public void addBlock(Supplier<? extends Block> key, String name) {
        add(key.get(), name);
    }

    public void add(Block key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addItem(Supplier<? extends Item> key, String name) {
        add(key.get(), name);
    }

    public void add(Item key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addEffect(Supplier<? extends MobEffect> key, String name) {
        add(key.get(), name);
    }

    public void add(MobEffect key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addEntityType(Supplier<? extends EntityType<?>> key, String name) {
        add(key.get(), name);
    }

    public void add(EntityType<?> key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addTag(Supplier<? extends TagKey<?>> key, String name) {
        add(key.get(), name);
    }

    public void add(TagKey<?> tagKey, String name) {
        add(Tags.getTagTranslationKey(tagKey), name);
    }

    public void add(String key, String value) {
        add(key, Component.literal(value)); // Literals are serialized as strings directly by the codec
    }

    public void add(String key, Component value) {
        if (data.put(key, value) != null) {
            throw new IllegalStateException("Duplicate translation key " + key);
        }
    }

    public void addDimension(ResourceKey<Level> dimension, String value) {
        add(dimension.identifier().toLanguageKey(ILevelExtension.TRANSLATION_PREFIX), value);
    }

    public void addBiome(ResourceKey<Biome> biome, String value) {
        add(biome.identifier().toLanguageKey("biome"), value);
    }
}
