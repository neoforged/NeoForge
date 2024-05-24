/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Bogged;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 *
 * This allows for mods to create there own Shear-like items
 * and have them interact with Blocks/Entities without extra work.
 * Also, if your block/entity supports the Shears, this allows you
 * to support mod-shears as well.
 *
 * TODO: reconsider this system, currently it is implemented but not checked for, for blocks.
 */
public interface IShearable {
    /**
     * Checks if the object is currently shearable
     * Example: Sheep return false when they have no wool
     *
     * @param item  The ItemStack that is being used, may be empty.
     * @param level The current level.
     * @param pos   Block's position in level.
     * @return If this is shearable, and onSheared should be called.
     */
    default boolean isShearable(@Nullable Player player, ItemStack item, Level level, BlockPos pos) {
        //Default to checking readyForShearing if we are the vanilla shearable interface, and if we aren't assume a default of true
        return !(this instanceof Shearable shearable) || shearable.readyForShearing();
    }

    /**
     * Performs the shear function on this object.
     * This is called for both client, and server.
     * The object should perform all actions related to being sheared,
     * except for dropping of the items, and removal of the block.
     * As those are handled by ItemShears itself.
     * <p>
     * For entities, they should trust their internal location information over the values passed into this function.
     *
     * @param item    The ItemStack that is being used, may be empty.
     * @param level   The current level.
     * @param pos     If this is a block, the block's position in level.
     * @return A List containing all items that resulted from the shearing process. May be empty.
     */
    default List<ItemStack> onSheared(@Nullable Player player, ItemStack item, Level level, BlockPos pos) {
        if (this instanceof LivingEntity entity && this instanceof Shearable shearable) {
            if (!level.isClientSide) {
                List<ItemEntity> drops = new ArrayList<>();
                entity.captureDrops(drops);
                shearable.shear(player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS);
                return entity.captureDrops(null).stream().map(ItemEntity::getItem).toList();
            }
        }
        return Collections.emptyList();
    }

    /**
     * Performs the logic used to drop a shear result into the world at the correct position and with the proper movement.
     * <br>
     * For entities, they should trust their internal location information over the values passed into this function.
     *
     * @param level The current level.
     * @param pos   If this is a block, the block's position in level.
     * @param drop  The ItemStack to drop.
     */
    default void spawnShearedDrop(Level level, BlockPos pos, ItemStack drop) {
        if (this instanceof SnowGolem golem) {
            golem.spawnAtLocation(drop, 1.7F);
        } else if (this instanceof Bogged bogged) {
            bogged.spawnAtLocation(drop);
        } else if (this instanceof MushroomCow cow) {
            // Note: Vanilla uses addFreshEntity instead of spawnAtLocation for spawning mooshrooms drops
            // In case a mod is capturing drops for the entity we instead do it the same way we patch in MushroomCow#shear
            ItemEntity itemEntity = cow.spawnAtLocation(drop, cow.getBbHeight());
            if (itemEntity != null) itemEntity.setNoPickUpDelay();
        } else if (this instanceof LivingEntity entity) {
            ItemEntity itemEntity = entity.spawnAtLocation(drop, 1);
            if (itemEntity != null) {
                itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add(
                        ((entity.getRandom().nextFloat() - entity.getRandom().nextFloat()) * 0.1F),
                        (entity.getRandom().nextFloat() * 0.05F),
                        ((entity.getRandom().nextFloat() - entity.getRandom().nextFloat()) * 0.1F)));
            }
        } else {
            level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), drop));
        }
    }
}
