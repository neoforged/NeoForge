/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * A data provider for {@link net.minecraft.client.particle.ParticleDescription}s.
 *
 * <p>To use this provider, extend this class and implement {@link #addDescriptions()}.
 * Then, register an instance using {@link net.minecraft.data.DataGenerator#addProvider(boolean, Factory)}
 * via the {@link GatherDataEvent} on the mod event bus.
 *
 * <p>A description can be added to a {@link ParticleType} which uses a {@linkplain #sprite(ParticleType, ResourceLocation) sprite}
 * or {@linkplain #spriteSet(ParticleType, Iterable) sprite set}.
 *
 * <pre>{@code
 * @Override
 * protected void addDescriptions() {
 *     // Single sprite
 *     this.sprite(ParticleTypes.DRIPPING_LAVA, ResourceLocation.withDefaultNamespace("drip_hang"));
 *
 *     // Multiple sprites
 *     this.spriteSet(ParticleTypes.CLOUD, ResourceLocation.withDefaultNamespace("generic"), 8, true);
 * }
 * }</pre>
 *
 * <p>A particle description holds a list of textures used when rendering the
 * particle to the screen. All registered particle descriptions are stitched
 * together into a texture atlas called {@link net.minecraft.client.renderer.texture.TextureAtlas#LOCATION_PARTICLES}.
 * A {@link ParticleType} whose particle uses the texture atlas, typically via
 * the {@link net.minecraft.client.particle.ParticleRenderType}, can then reference
 * the necessary texture during rendering.
 *
 * <p>Particles with a particle description must have their particle providers
 * attached to a {@link ParticleType} as a {@linkplain RegisterParticleProvidersEvent#registerSprite(ParticleType, ParticleProvider.Sprite) sprite}
 * or {@linkplain RegisterParticleProvidersEvent#registerSpriteSet(ParticleType, ParticleEngine.SpriteParticleRegistration) sprite set}
 * consumer.
 *
 * @see DataProvider
 * @see net.minecraft.client.particle.ParticleDescription
 */
public abstract class ParticleDescriptionProvider implements DataProvider {
    private final PackOutput.PathProvider particlesPath;
    @VisibleForTesting
    protected final ExistingFileHelper fileHelper;
    @VisibleForTesting
    protected final Map<ResourceLocation, List<String>> descriptions;

    /**
     * Creates an instance of the data provider.
     *
     * @param output     the expected root directory the data generator outputs to
     * @param fileHelper the helper used to validate a texture's existence
     */
    protected ParticleDescriptionProvider(PackOutput output, ExistingFileHelper fileHelper) {
        this.particlesPath = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "particles");
        this.fileHelper = fileHelper;
        this.descriptions = new HashMap<>();
    }

    /**
     * Registers the particle descriptions generated by {@link #sprite(ParticleType, ResourceLocation)}
     * or one of the {@link #spriteSet(ParticleType, Iterable) sprite set methods}.
     */
    protected abstract void addDescriptions();

    /**
     * Creates a new particle description that contains a single texture for the
     * associated {@link ParticleType}.
     *
     * <p>Particle types with this description should be attached to a particle provider
     * via {@link RegisterParticleProvidersEvent#registerSprite(ParticleType, ParticleProvider.Sprite)}.
     *
     * @param type    the particle type the textures are applied
     *                for
     * @param texture the texture to render for the particle type
     * @throws NullPointerException     if the particle type is not registered
     * @throws IllegalArgumentException if a texture does not have an associated PNG
     *                                  file, or the particle type has already been
     *                                  provided
     */
    protected void sprite(ParticleType<?> type, ResourceLocation texture) {
        this.spriteSet(type, texture);
    }

    /**
     * Creates a new particle description that contains multiple textures for the
     * associated {@link ParticleType}. The textures are generated from a common
     * name and appended with a number representing the state of the animation.
     *
     * <pre>{@code
     * minecraft:generic_0
     * minecraft:generic_1
     * minecraft:generic_2
     * // ...
     * }</pre>
     *
     * <p>Particle types with this description should be attached to a particle provider
     * via {@link RegisterParticleProvidersEvent#registerSpriteSet(ParticleType, ParticleEngine.SpriteParticleRegistration)}.
     *
     * @param type          the particle type the textures are applied
     *                      for
     * @param baseName      the common name of all the textures
     * @param numOfTextures the number of textures within the set
     * @param reverse       when {@code true}, the textures will be
     *                      listed in descending order
     * @throws NullPointerException     if the particle type is not registered
     * @throws IllegalArgumentException if a texture does not have an associated PNG
     *                                  file, or the particle type has already been
     *                                  provided
     */
    protected void spriteSet(ParticleType<?> type, ResourceLocation baseName, int numOfTextures, boolean reverse) {
        Preconditions.checkArgument(numOfTextures > 0, "The number of textures to generate must be positive");
        this.spriteSet(type, () -> new Iterator<>() {
            private int counter = 0;

            @Override
            public boolean hasNext() {
                return this.counter < numOfTextures;
            }

            @Override
            public ResourceLocation next() {
                var texture = baseName.withSuffix("_" + (reverse ? numOfTextures - this.counter - 1 : this.counter));
                this.counter++;
                return texture;
            }
        });
    }

    /**
     * Creates a new particle description that contains multiple textures for the
     * associated {@link ParticleType}. The textures are passed as varargs with
     * at least one texture present.
     *
     * <p>Particle types with this description should be attached to a particle provider
     * via {@link RegisterParticleProvidersEvent#registerSpriteSet(ParticleType, ParticleEngine.SpriteParticleRegistration)}.
     *
     * @param type     the particle type the textures are applied
     *                 for
     * @param texture  the first texture in the description
     * @param textures a list of subsequent textures to render for
     *                 the particle type
     * @throws NullPointerException     if the particle type is not registered
     * @throws IllegalArgumentException if a texture does not have an associated PNG
     *                                  file, or the particle type has already been
     *                                  provided
     */
    protected void spriteSet(ParticleType<?> type, ResourceLocation texture, ResourceLocation... textures) {
        this.spriteSet(type, Stream.concat(Stream.of(texture), Arrays.stream(textures))::iterator);
    }

    /**
     * Creates a new particle description that contains multiple textures for the
     * associated {@link ParticleType}. The textures are passed as an iterable.
     *
     * <p>Particle types with this description should be attached to a particle provider
     * via {@link RegisterParticleProvidersEvent#registerSpriteSet(ParticleType, ParticleEngine.SpriteParticleRegistration)}.
     *
     * @param type     the particle type the textures are applied
     *                 for
     * @param textures a list of textures to render for the
     *                 particle type
     * @throws NullPointerException     if the particle type is not registered
     * @throws IllegalArgumentException if there are no textures provided, a texture
     *                                  does not have an associated PNG file, or
     *                                  the particle type has already been provided
     */
    protected void spriteSet(ParticleType<?> type, Iterable<ResourceLocation> textures) {
        // Make sure particle type is registered
        var particle = Preconditions.checkNotNull(BuiltInRegistries.PARTICLE_TYPE.getKey(type), "The particle type is not registered");

        // Validate textures
        List<String> desc = new ArrayList<>();
        for (var texture : textures) {
            Preconditions.checkArgument(this.fileHelper.exists(texture, PackType.CLIENT_RESOURCES, ".png", "textures/particle"),
                    "Texture '%s' does not exist in any known resource pack", texture);
            desc.add(texture.toString());
        }
        Preconditions.checkArgument(desc.size() > 0, "The particle type '%s' must have one texture", particle);

        // Insert into map
        if (this.descriptions.putIfAbsent(particle, desc) != null)
            throw new IllegalArgumentException(String.format("The particle type '%s' already has a description associated with it", particle));
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        this.addDescriptions();

        return CompletableFuture.allOf(
                this.descriptions.entrySet().stream().map(entry -> {
                    // Map entries to the description format
                    var textures = new JsonArray();
                    entry.getValue().forEach(textures::add);
                    return DataProvider.saveStable(cache,
                            Util.make(new JsonObject(), obj -> obj.add("textures", textures)),
                            this.particlesPath.json(entry.getKey()));
                }).toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Particle Descriptions";
    }
}
