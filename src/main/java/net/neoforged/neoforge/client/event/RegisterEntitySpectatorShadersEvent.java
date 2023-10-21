/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

/**
 * Allows users to register custom shaders to be used when the player spectates a certain kind of entity.
 * Vanilla examples of this are the green effect for creepers and the invert effect for endermen.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 *
 * <p>This event is fired on the {@linkplain FMLJavaModLoadingContext#getModEventBus() mod-specific event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterEntitySpectatorShadersEvent extends Event implements IModBusEvent
{
    private final Map<EntityType<?>, ResourceLocation> shaders;

    @ApiStatus.Internal
    public RegisterEntitySpectatorShadersEvent(Map<EntityType<?>, ResourceLocation> shaders)
    {
        this.shaders = shaders;
    }

    /**
     * Registers a spectator shader for a given entity type.
     */
    public void register(EntityType<?> entityType, ResourceLocation shader)
    {
        shaders.put(entityType, shader);
    }
}
