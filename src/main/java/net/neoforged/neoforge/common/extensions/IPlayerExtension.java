/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.network.IContainerFactory;

public interface IPlayerExtension {
    private Player self() {
        return (Player) this;
    }

    /**
     * The entity reach is increased by 2 for creative players, unless it is currently zero, which disables attacks and entity interactions.
     * 
     * @return The entity reach of this player.
     */
    default double getEntityReach() {
        double range = self().getAttributeValue(NeoForgeMod.ENTITY_REACH.value());
        return range == 0 ? 0 : range + (self().isCreative() ? 2 : 0);
    }

    /**
     * The reach distance is increased by 0.5 for creative players, unless it is currently zero, which disables interactions.
     * 
     * @return The reach distance of this player.
     */
    default double getBlockReach() {
        double reach = self().getAttributeValue(NeoForgeMod.BLOCK_REACH.value());
        return reach == 0 ? 0 : reach + (self().isCreative() ? 0.5 : 0);
    }

    /**
     * Checks if the player can reach an entity by targeting the passed vector.<br>
     * On the server, additional padding is added to account for movement/lag.
     * 
     * @param entityHitVec The vector being range-checked.
     * @param padding      Extra validation distance.
     * @return If the player can attack the entity.
     * @apiNote Do not use for block checks, as this method uses {@link #getEntityReach()}
     */
    default boolean canReach(Vec3 entityHitVec, double padding) {
        return self().getEyePosition().closerThan(entityHitVec, getEntityReach() + padding);
    }

    /**
     * Checks if the player can reach an entity.<br>
     * On the server, additional padding is added to account for movement/lag.
     * 
     * @param entity  The entity being range-checked.
     * @param padding Extra validation distance.
     * @return If the player can attack the passed entity.
     * @apiNote Prefer using {@link #canReach(Vec3, double)} if you have a {@link HitResult} available.
     */
    default boolean canReach(Entity entity, double padding) {
        return isCloseEnough(entity, getEntityReach() + padding);
    }

    /**
     * Checks if the player can reach a block.<br>
     * On the server, additional padding is added to account for movement/lag.
     * 
     * @param pos     The position being range-checked.
     * @param padding Extra validation distance.
     * @return If the player can interact with this location.
     */
    default boolean canReach(BlockPos pos, double padding) {
        double reach = this.getBlockReach() + padding;
        return self().getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) < reach * reach;
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
    default OptionalInt openMenu(MenuProvider menuProvider, Consumer<FriendlyByteBuf> extraDataWriter) {
        return OptionalInt.empty();
    }

    /**
     * Determine whether a player is allowed creative flight via game mode or attribute.<br>
     * Modders are discouraged from setting {@link Abilities#mayfly} directly.
     *
     * @return true when creative flight is available
     * @see NeoForgeMod#CREATIVE_FLIGHT
     */
    @SuppressWarnings("deprecation")
    default boolean mayFly() {
        // TODO 1.20.5: consider forcing mods to use the attribute
        return self().getAbilities().mayfly || self().getAttributeValue(NeoForgeMod.CREATIVE_FLIGHT) > 0;
    }
}
