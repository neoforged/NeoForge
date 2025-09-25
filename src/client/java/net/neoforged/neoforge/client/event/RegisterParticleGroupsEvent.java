/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired for registering additional {@linkplain ParticleGroup particle group factories}.
 * <p>This is used when creating particle groups for particles in {@link ParticleEngine#createParticleGroup(ParticleRenderType)}.
 *
 * <p>This event is fired on the mod-specific event bus,
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterParticleGroupsEvent extends Event implements IModBusEvent {
    private final Map<ParticleRenderType, Function<ParticleEngine, ParticleGroup<?>>> particleGroupFactories;
    private final List<ParticleRenderType> particleRenderOrder;

    @ApiStatus.Internal
    public RegisterParticleGroupsEvent(
            Map<ParticleRenderType, Function<ParticleEngine, ParticleGroup<?>>> particleGroupFactories,
            List<ParticleRenderType> particleRenderOrder) {
        this.particleGroupFactories = particleGroupFactories;
        this.particleRenderOrder = particleRenderOrder;
    }

    /**
     * Registers a factory function for the given {@linkplain Particle#getGroup() particle group}, to be rendered
     * before all currently registered types.
     *
     * @param particleRenderType An identifier uniquely identifying the particle group.
     * @param factory            A factory function used to create a {@link ParticleGroup} for the particle group.
     *
     * @throws IllegalArgumentException when {@code particleRenderType} has already been registered.
     */
    public void registerBeforeAll(ParticleRenderType particleRenderType, Function<ParticleEngine, ParticleGroup<?>> factory) {
        if (particleGroupFactories.putIfAbsent(particleRenderType, factory) != null) {
            throw new IllegalArgumentException("Factory already registered for provided particle render type: " + particleRenderType.name());
        }

        particleRenderOrder.addFirst(particleRenderType);
    }

    /**
     * Registers a factory function for the given {@linkplain Particle#getGroup() particle group}, to be rendered
     * after all currently registered types.
     *
     * @param particleRenderType An identifier uniquely identifying the particle group.
     * @param factory            A factory function used to create a {@link ParticleGroup} for the particle group.
     *
     * @throws IllegalArgumentException when {@code particleRenderType} has already been registered.
     */
    public void registerAfterAll(ParticleRenderType particleRenderType, Function<ParticleEngine, ParticleGroup<?>> factory) {
        if (particleGroupFactories.putIfAbsent(particleRenderType, factory) != null) {
            throw new IllegalArgumentException("Factory already registered for provided particle render type: " + particleRenderType.name());
        }

        particleRenderOrder.add(particleRenderType);
    }

    /**
     * Registers a factory function for the given {@linkplain Particle#getGroup() particle group}, to be rendered
     * before the given type.
     *
     * @param before             An identifier uniquely identifying that particle group to register before
     * @param particleRenderType An identifier uniquely identifying the particle group.
     * @param factory            A factory function used to create a {@link ParticleGroup} for the particle group.
     *
     * @throws IllegalArgumentException when {@code before} has not been registered.
     * @throws IllegalArgumentException when {@code particleRenderType} has already been registered.
     */
    public void registerBefore(ParticleRenderType before, ParticleRenderType particleRenderType, Function<ParticleEngine, ParticleGroup<?>> factory) {
        var idx = particleRenderOrder.indexOf(before);
        if (idx < 0) {
            throw new IllegalArgumentException("Unknown particle render type: " + before.name());
        }

        if (particleGroupFactories.putIfAbsent(particleRenderType, factory) != null) {
            throw new IllegalArgumentException("Factory already registered for provided particle render type: " + particleRenderType.name());
        }

        particleRenderOrder.add(idx, particleRenderType);
    }

    /**
     * Registers a factory function for the given {@linkplain Particle#getGroup() particle group}, to be rendered
     * after the given type.
     *
     * @param after              An identifier uniquely identifying that particle group to register after
     * @param particleRenderType An identifier uniquely identifying the particle group.
     * @param factory            A factory function used to create a {@link ParticleGroup} for the particle group.
     *
     * @throws IllegalArgumentException when {@code after} has not been registered.
     * @throws IllegalArgumentException when {@code particleRenderType} has already been registered.
     */
    public void registerAfter(ParticleRenderType after, ParticleRenderType particleRenderType, Function<ParticleEngine, ParticleGroup<?>> factory) {
        var idx = particleRenderOrder.indexOf(after);
        if (idx < 0) {
            throw new IllegalArgumentException("Unknown particle render type: " + after.name());
        }

        if (particleGroupFactories.putIfAbsent(particleRenderType, factory) != null) {
            throw new IllegalArgumentException("Factory already registered for provided particle render type: " + particleRenderType.name());
        }

        particleRenderOrder.add(idx + 1, particleRenderType);
    }
}
