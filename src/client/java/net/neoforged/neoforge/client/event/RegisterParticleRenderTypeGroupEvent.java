package net.neoforged.neoforge.client.event;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.function.Function;

/**
 * Fired for registering additional {@linkplain ParticleGroup particle group factories}.
 * <p>This is used when creating particle groups for particles in {@link ParticleEngine#createParticleGroup(ParticleRenderType)}.
 *
 * <p>This event is fired on the mod-specific event bus,
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterParticleRenderTypeGroupEvent extends Event implements IModBusEvent {
    private final Map<ParticleRenderType, Function<ParticleEngine, ParticleGroup<?>>> map;

    @ApiStatus.Internal
    public RegisterParticleRenderTypeGroupEvent(Map<ParticleRenderType, Function<ParticleEngine, ParticleGroup<?>>> map)  {
        this.map = map;
    }

    /**
     * Registers a factory function for the given {@linkplain Particle#getGroup() particle group}.
     *
     * @param particleRenderType An identifier uniquely identifying the particle group.
     * @param factory A factory function used to create a {@link ParticleGroup} for the particle group.
     *
     * @throws IllegalArgumentException when {@code particleRenderType} has already been registered.
     */
    public void register(ParticleRenderType particleRenderType, Function<ParticleEngine, ParticleGroup<?>> factory) {
        if (map.putIfAbsent(particleRenderType, factory) != null) {
            throw new IllegalArgumentException("Factory already registered for provided particle render type: " + particleRenderType.name());
        }
    }
}
