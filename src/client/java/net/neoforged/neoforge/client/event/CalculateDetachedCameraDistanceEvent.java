/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.Camera;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired for hooking the maximum distance from the player to the camera in third-person view.
 * The ray-cast that reduces this distance based on the blocks around the player is invoked after this event is fired.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class CalculateDetachedCameraDistanceEvent extends Event {
    private final Camera camera;
    private final boolean cameraFlipped;
    private final float entityScale;
    private final float entityDistance;
    private final float vehicleEntityScale;
    private final float vehicleEntityDistance;

    private float distance;

    @ApiStatus.Internal
    public CalculateDetachedCameraDistanceEvent(Camera camera, boolean cameraFlipped, float entityScale, float entityDistance, float vehicleEntityScale, float vehicleDistance) {
        this.camera = camera;
        this.cameraFlipped = cameraFlipped;
        this.entityScale = entityScale;
        this.entityDistance = entityDistance;
        this.vehicleEntityScale = vehicleEntityScale;
        this.vehicleEntityDistance = vehicleDistance;
        this.distance = Math.max(entityScale * entityDistance, vehicleEntityScale * vehicleDistance);
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
     * Returns the scaling factor that will be applied to the {@link #entityDistance} to compute the final distance,
     * based on the size of the {@link Camera#getEntity() camera entity}.
     */
    public float getEntityScalingFactor() {
        return entityScale;
    }

    /**
     * Returns the unscaled camera distance provided by the {@link Camera#getEntity() camera entity}.
     */
    public float getEntityDistance() {
        return entityDistance;
    }

    /**
     * Returns the scaling factor that will be applied to the {@link #vehicleEntityDistance} to compute the final distance,
     * based on the size of the vehicle ridden by the {@link Camera#getEntity() camera entity}, if any.
     */
    public float getVehicleEntityScalingFactor() {
        return vehicleEntityScale;
    }

    /**
     * Returns the unscaled camera distance provided by the vehicle ridden by the {@link Camera#getEntity() camera entity}, if any.
     */
    public float getVehicleEntityDistance() {
        return vehicleEntityDistance;
    }

    /**
     * Returns the final distance from the camera to the {@linkplain Camera#getEntity() camera entity}.
     */
    public float getDistance() {
        return distance;
    }

    /**
     * Sets final distance from the camera to the {@linkplain Camera#getEntity() camera entity}.
     * 
     * @param distance The new distance from the camera to the {@linkplain Camera#getEntity() camera entity}
     */
    public void setDistance(float distance) {
        this.distance = distance;
    }
}
