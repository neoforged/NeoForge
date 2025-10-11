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
 * <p>This is used when creating particle groups for particles in {@link ParticleEngine}.
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
     * Registers a factory function for the given {@linkplain Particle#getGroup() particle group}.
     *
     * @param group   The {@linkplain Particle#getGroup() particle group} to register for.
     * @param factory A factory function used to create the {@link ParticleGroup}.
     * @throws IllegalArgumentException when a factory has already been registered for {@code group}.
     */
    public void register(ParticleRenderType group, Function<ParticleEngine, ParticleGroup<?>> factory) {
        if (particleGroupFactories.putIfAbsent(group, factory) != null) {
            throw new IllegalArgumentException("Factory already registered for provided particle render type: " + group.name());
        }

        particleRenderOrder.add(group);
    }
}
