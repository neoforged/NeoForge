/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Data provider for the {@code sounds.json} file, which identifies sound definitions
 * for the various sound events in Minecraft.
 */
public abstract class SoundDefinitionsProvider implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private final PackOutput output;
    private final String modId;

    private final Map<String, SoundDefinition> sounds = new LinkedHashMap<>();

    /**
     * Creates a new instance of this data provider.
     *
     * @param output The {@linkplain PackOutput} instance provided by the data generator.
     * @param modId  The mod ID of the current mod.
     */
    protected SoundDefinitionsProvider(final PackOutput output, final String modId) {
        this.output = output;
        this.modId = modId;
    }

    /**
     * Registers the sound definitions that should be generated via one of the {@code add} methods.
     */
    public abstract void registerSounds();

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        this.sounds.clear();
        this.registerSounds();
        this.validate();
        if (!this.sounds.isEmpty()) {
            return this.save(cache, this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(this.modId).resolve("sounds.json"));
        }

        return CompletableFuture.allOf();
    }

    @Override
    public String getName() {
        return "Sound Definitions";
    }

    // Quick helpers
    /**
     * Creates a new {@link SoundDefinition}, which will host a set of
     * {@link SoundDefinition.Sound}s and the necessary parameters.
     */
    protected static SoundDefinition definition() {
        return SoundDefinition.definition();
    }

    /**
     * Creates a new sound with the given name and type.
     *
     * @param name The name of the sound to create.
     * @param type The type of sound to create.
     */
    protected static SoundDefinition.Sound sound(final ResourceLocation name, final SoundDefinition.SoundType type) {
        return SoundDefinition.Sound.sound(name, type);
    }

    /**
     * Creates a new sound with the given name and {@link SoundDefinition.SoundType#SOUND} as
     * sound type.
     *
     * @param name The name of the sound to create.
     */
    protected static SoundDefinition.Sound sound(final ResourceLocation name) {
        return sound(name, SoundDefinition.SoundType.SOUND);
    }

    /**
     * Creates a new sound with the given name and type.
     *
     * @param name The name of the sound to create.
     * @param type The type of sound to create.
     */
    protected static SoundDefinition.Sound sound(final String name, final SoundDefinition.SoundType type) {
        return sound(ResourceLocation.parse(name), type);
    }

    /**
     * Creates a new sound with the given name and {@link SoundDefinition.SoundType#SOUND} as
     * sound type.
     *
     * @param name The name of the sound to create.
     */
    protected static SoundDefinition.Sound sound(final String name) {
        return sound(ResourceLocation.parse(name));
    }

    // Addition methods
    /**
     * Adds the entry name associated with the supplied {@link SoundEvent} with the given
     * {@link SoundDefinition} to the list.
     *
     * <p>This method should be preferred when dealing with a {@code DeferredHolder}.</p>
     *
     * @param soundEvent A {@code Holder} for the given {@link SoundEvent}.
     * @param definition A {@link SoundDefinition} that defines the given sound.
     */
    protected void add(final Holder<SoundEvent> soundEvent, final SoundDefinition definition) {
        this.add(soundEvent.value(), definition);
    }

    /**
     * Adds the entry name associated with the given {@link SoundEvent} with the
     * {@link SoundDefinition} to the list.
     *
     * <p>This method should be preferred when a {@code SoundEvent} is already
     * available in the method context. If you already have a {@code Holder} for
     * it, refer to {@link #add(Holder, SoundDefinition)}.</p>
     *
     * @param soundEvent A {@link SoundEvent}.
     * @param definition The {@link SoundDefinition} that defines the given event.
     */
    protected void add(final SoundEvent soundEvent, final SoundDefinition definition) {
        this.add(soundEvent.location(), definition);
    }

    /**
     * Adds the {@link SoundEvent} referenced by the given {@link ResourceLocation} with the
     * {@link SoundDefinition} to the list.
     *
     * @param soundEvent The {@link ResourceLocation} that identifies the event.
     * @param definition The {@link SoundDefinition} that defines the given event.
     */
    protected void add(final ResourceLocation soundEvent, final SoundDefinition definition) {
        this.addSounds(soundEvent.getPath(), definition);
    }

    /**
     * Adds the {@link SoundEvent} with the specified name along with its {@link SoundDefinition}
     * to the list.
     *
     * <p>The given sound event must NOT contain the namespace the name is a part of, since
     * the sound definition specification doesn't allow sounds to be defined outside the
     * namespace they're in. For this reason, any namespace will automatically be stripped
     * from the name.</p>
     *
     * @param soundEvent The name of the {@link SoundEvent}.
     * @param definition The {@link SoundDefinition} that defines the given event.
     */
    protected void add(final String soundEvent, final SoundDefinition definition) {
        this.add(ResourceLocation.parse(soundEvent), definition);
    }

    private void addSounds(final String soundEvent, final SoundDefinition definition) {
        if (this.sounds.put(soundEvent, definition) != null) {
            throw new IllegalStateException("Sound event '" + this.modId + ":" + soundEvent + "' already exists");
        }
    }

    // Internal handling stuff
    private void validate() {
        final List<String> notValid = this.sounds.entrySet().stream()
                .filter(it -> !this.validate(it.getKey(), it.getValue()))
                .map(Map.Entry::getKey)
                .map(it -> this.modId + ":" + it)
                .toList();
        if (!notValid.isEmpty()) {
            throw new IllegalStateException("Found invalid sound events: " + notValid);
        }
    }

    private boolean validate(final String name, final SoundDefinition def) {
        return def.soundList().stream()
                .filter(it -> it.type() == SoundDefinition.SoundType.EVENT)
                .allMatch(it -> {
                    final boolean valid = this.sounds.containsKey(name) || BuiltInRegistries.SOUND_EVENT.containsKey(it.name());
                    if (!valid) {
                        LOGGER.warn("Unable to find event '{}' referenced from '{}'", it.name(), name);
                    }
                    return valid;
                });
    }

    private CompletableFuture<?> save(final CachedOutput cache, final Path targetFile) {
        return DataProvider.saveStable(cache, this.mapToJson(this.sounds), targetFile);
    }

    private JsonObject mapToJson(final Map<String, SoundDefinition> map) {
        final JsonObject obj = new JsonObject();
        // namespaces are ignored when serializing
        map.forEach((k, v) -> obj.add(k, v.serialize()));
        return obj;
    }
}
