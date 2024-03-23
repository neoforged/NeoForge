/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.IContainerFactory;

public interface IPlayerExtension {
    private Player self() {
        return (Player) this;
    }

    /**
     * Utility check to see if the player is close enough to a target entity. Uses "eye-to-closest-corner" checks.
     * 
     * @param entity The entity being checked against
     * @param dist   The max distance allowed
     * @return If the eye-to-center distance between this player and the passed entity is less than dist.
     * @implNote This method inflates the bounding box by the pick radius, which differs from vanilla. But vanilla doesn't use the pick radius, the only entity with > 0 is AbstractHurtingProjectile.
     */
    default boolean isCloseEnough(Entity entity, double dist) {
        Vec3 eye = self().getEyePosition();
        AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());
        return aabb.distanceToSqr(eye) < dist * dist;
    }

    /**
     * Request to open a GUI on the client, from the server
     * <p>
     * Refer to {@link MenuType#create(IContainerFactory)} for how to provide a function to consume
     * these GUI requests on the client.
     *
     * @param menuProvider A supplier of container properties including the registry name of the container
     * @param pos          A block pos, which will be encoded into the additional data for this request
     *
     */
    default OptionalInt openMenu(MenuProvider menuProvider, BlockPos pos) {
        return openMenu(menuProvider, buf -> buf.writeBlockPos(pos));
    }

    /**
     * Request to open a GUI on the client, from the server
     * <p>
     * Refer to {@link MenuType#create(IContainerFactory)} for how to provide a function to consume
     * these GUI requests on the client.
     * <p>
     * The maximum size for #extraDataWriter is 32600 bytes.
     *
     * @param menuProvider    A supplier of container properties including the registry name of the container
     * @param extraDataWriter Consumer to write any additional data the GUI needs
     * @return The window ID of the opened GUI, or empty if the GUI could not be opened
     */
    default OptionalInt openMenu(MenuProvider menuProvider, Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        return OptionalInt.empty();
    }
}
