/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired in {@linkplain Camera#setup(BlockGetter, Entity, boolean, boolean, float) Camera#setup(BlockGetter, Entity, boolean, boolean, float)} when camera is detached before calculating the distance of the camera from the {@linkplain Camera#getEntity() camera entity} based on a hard-coded maximum and a raycast.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class CalculateDetachedCameraDistanceEvent extends Event {
    
    private final Camera camera;
    private final boolean cameraFlipped;
    
    private double distance;
    
    @ApiStatus.Internal
    public CalculateDetachedCameraDistanceEvent(Camera camera, boolean cameraFlipped, double distance) {
        this.camera = camera;
        this.cameraFlipped = cameraFlipped;
        this.distance = distance;
    }
    
    /**
     * Returns the {@linkplain Camera camera} instance.
     */
    public Camera getCamera() {
        return camera;
    }
    
    /**
     * Returns `true` if the camera is flipped (ie facing backward instead of forward).
     */
    public boolean isCameraFlipped() {
        return cameraFlipped;
    }
    
    /**
     * Returns the distance from the camera to the {@linkplain Camera#getEntity() camera entity}.
     */
    public double getDistance() {
        return distance;
    }
    
    /**
     * Sets the distance from the camera to the {@linkplain Camera#getEntity() camera entity}.
     * 
     * @param distance The new distance from the camera to the {@linkplain Camera#getEntity() camera entity}
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }
    
}
