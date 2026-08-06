package org.bukkit.entity.model;

import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a part of a player model/texture.
 */
@ApiStatus.Experimental
public enum PlayerModelPart {

    /**
     * Player cape.
     */
    CAPE,
    /**
     * Player torso covering.
     */
    JACKET,
    /**
     * Player left arm covering.
     */
    LEFT_SLEEVE,
    /**
     * Player right arm covering.
     */
    RIGHT_SLEEVE,
    /**
     * Player left leg covering.
     */
    LEFT_PANTS_LEG,
    /**
     * Player right leg covering.
     */
    RIGHT_PANTS_LEG,
    /**
     * Player hat/helmet.
     */
    HAT;
}
