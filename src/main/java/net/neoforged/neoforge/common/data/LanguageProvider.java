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
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.fluids.FluidType;

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
        addKey(dimension, ILevelExtension.TRANSLATION_PREFIX, value);
    }

    public void addBiome(ResourceKey<Biome> biome, String value) {
        addKey(biome, "biome", value);
    }

    public void add(GameRule<?> gameRule, String value) {
        add(gameRule.getDescriptionId(), value);
    }

    public void add(GameRule<?> gameRule, String value, String description) {
        add(gameRule, value);
        add(gameRule.getDescriptionId() + ".description", description);
    }

    public void add(GameRuleCategory gameRuleCategory, String value) {
        addTranslatableComponent(gameRuleCategory.label(), value);
    }

    public void addGameRule(Supplier<? extends GameRule<?>> gameRule, String value) {
        add(gameRule.get(), value);
    }

    public void addGameRule(Supplier<? extends GameRule<?>> gameRule, String value, String description) {
        add(gameRule.get(), value, description);
    }

    public void add(FluidType fluidType, String value) {
        add(fluidType.getDescriptionId(), value);
    }

    public void addFluidType(Supplier<? extends FluidType> fluidType, String value) {
        add(fluidType.get(), value);
    }

    public void addKey(ResourceKey<?> registryKey, String type, String value) {
        add(registryKey.identifier().toLanguageKey(type), value);
    }

    /// Adds a translation by extracting its key from the given [translatable][TranslatableContents] [Component].
    ///
    /// @param key the [Component] containing the [TranslatableContents] used to extract the key
    /// @param value the translation value
    /// @throws IllegalArgumentException if the given [Component] does not contain [TranslatableContents]
    public void addTranslatableComponent(Component key, String value) {
        if (key.getContents() instanceof TranslatableContents translatable) {
            add(translatable.getKey(), value);
        } else {
            throw new IllegalArgumentException("Only TranslatableContents Components are allowed!");
        }
    }

    public void addConfigCategory(String key, String catValue, String catButtonValue, String catTooltipValue) {
        add(key, catValue);
        add(key + ".button", catButtonValue);
        add(key + ".tooltip", catTooltipValue);
    }

    public void addConfigValue(ModConfigSpec.ConfigValue<?> configValue, String value) {
        var translationKey = configValue.getSpec().getTranslationKey();

        if (translationKey == null) {
            return;
        }

        add(translationKey, value);

        var comment = configValue.getSpec().getComment();

        if (comment != null) {
            add(translationKey + ".tooltip", comment);
        }
    }
}
