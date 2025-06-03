/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Bogged;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * This interfaces allows shears (modded & vanilla) and {@linkplain Entity entities} (modded & vanilla) to cooperate
 * without needing advance knowledge of each other.
 * <p>
 * In the future, this system may function for implementations on {@link Block}s as well.
 *
 * TODO: Implement support for {@link Block} or remove default implementations from vanilla block classes.
 */
public interface IShearable {
    /**
     * Checks if this object can be sheared.
     * <p>
     * For example, Sheep return false when they have no wool.
     *
     * @param item  The shear tool that is being used, may be empty.
     * @param level The current level.
     * @param pos   The block position of this object (if this is an entity, the value of {@link Entity#blockPosition()}.
     * @return If this is shearable, and {@link #onSheared} may be called.
     */
    default boolean isShearable(@Nullable Player player, ItemStack item, Level level, BlockPos pos) {
        // Default to checking readyForShearing if we are the vanilla shearable interface, and if we aren't assume a default of true
        return !(this instanceof Shearable shearable) || shearable.readyForShearing();
    }

    /**
     * Shears this object. This function is called on both sides, and is responsible for performing any and all actions that happen when sheared, except spawning drops.
     * <p>
     * Drops that are spawned as a result of being sheared should be returned from this method, and will be spawned on the server using {@link #spawnShearedDrop}.
     * <p>
     * {@linkplain Entity Entities} may respect their internal position values instead of relying on the {@code pos} parameter.
     *
     * @param item  The shear tool that is being used, may be empty.
     * @param level The current level.
     * @param pos   The block position of this object (if this is an entity, the value of {@link Entity#blockPosition()}.
     * @return A list holding all dropped items resulting from the shear operation, or an empty list if nothing dropped.
     */
    @SuppressWarnings("deprecation") // Expected call to deprecated vanilla Shearable#shear
    default List<ItemStack> onSheared(@Nullable Player player, ItemStack item, Level level, BlockPos pos) {
        if (this instanceof LivingEntity entity && this instanceof Shearable shearable) {
            if (level instanceof ServerLevel serverLevel) {
                Collection<ItemEntity> previous = entity.captureDrops(new ArrayList<>());
                shearable.shear(serverLevel, player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS, item);
                return entity.captureDrops(previous).stream().map(ItemEntity::getItem).toList();
            }
        }
        return Collections.emptyList();
    }

    /**
     * Performs the logic used to drop a shear result into the world at the correct position and with the proper movement.
     * <br>
     * {@linkplain Entity Entities} may respect their internal position values instead of relying on the {@code pos} parameter.
     *
     * @param level The current level.
     * @param pos   The block position of this object (if this is an entity, the value of {@link Entity#blockPosition()}.
     * @param drop  The stack to drop, from the list of drops returned by {@link #onSheared}.
     */
    default void spawnShearedDrop(ServerLevel level, BlockPos pos, ItemStack drop) {
        if (this instanceof SnowGolem golem) {
            // SnowGolem#shear uses spawnAtLocation(..., this.getEyeHeight());
            golem.spawnAtLocation(level, drop, golem.getEyeHeight());
        } else if (this instanceof Bogged bogged) {
            // Bogged#spawnShearedMushrooms uses spawnAtLocation(..., this.getBbHeight());
            bogged.spawnAtLocation(level, drop, bogged.getBbHeight());
        } else if (this instanceof MushroomCow cow) {
            // We patch Mooshrooms from using addFreshEntity to spawnAtLocation to spawnAtLocation to capture the drops.
            // In case a mod is also capturing drops, we also replicate that logic here.
            ItemEntity itemEntity = cow.spawnAtLocation(level, drop, cow.getBbHeight());
            if (itemEntity != null) {
                itemEntity.setNoPickUpDelay();
            }
        } else if (this instanceof Entity entity) {
            // Everything else uses the "default" rules invented by Sheep#shear, which uses a y-offset of 1 and these random delta movement values.
            ItemEntity itemEntity = entity.spawnAtLocation(level, drop, 1);
            if (itemEntity != null) {
                RandomSource rand = entity.getRandom();
                Vec3 newDelta = itemEntity.getDeltaMovement().add(
                        (rand.nextFloat() - rand.nextFloat()) * 0.1F,
                        rand.nextFloat() * 0.05F,
                        (rand.nextFloat() - rand.nextFloat()) * 0.1F);
                itemEntity.setDeltaMovement(newDelta);
            }
        } else {
            // If we aren't an entity, fallback to spawning the item at the given position.
            level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), drop));
        }
    }
}
