/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import java.util.List;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * ProjectileWeaponLoadEvent is fired when a living entity is loading projectiles for a {@link ProjectileWeaponItem}.
 */
public abstract class ProjectileWeaponLoadEvent extends LivingEvent {
    private final ItemStack weaponItem;
    private final ItemStack projectileItem;

    public ProjectileWeaponLoadEvent(LivingEntity entity, ItemStack weaponItem, ItemStack projectile) {
        super(entity);
        this.weaponItem = weaponItem;
        this.projectileItem = projectile;
    }

    /**
     * @return the {@link ProjectileWeaponItem} used by the entity.
     */
    public ItemStack getWeaponItem() {
        return this.weaponItem;
    }

    /**
     * @return the projectile item to be loaded.
     *         <br>
     *         With vanilla behavior, this usually returns the result {@link LivingEntity#getProjectile(ItemStack)},
     *         but it's possible for that to not be the case if modder uses a different implementation of the {@link ProjectileWeaponItem}.
     *         <br>
     *         Modifying the returned projectile has no effect,
     *         subscribe to {@link LivingGetProjectileEvent} instead for controlling the projectile.
     */
    public ItemStack getProjectileItem() {
        return this.projectileItem;
    }

    /**
     * ProjectileWeaponLoadEvent.Pre is fired when a living entity is about to start loading the {@link ProjectileWeaponItem},
     * before {@link LivingEntity#startUsingItem(InteractionHand)} is called.
     * <p>
     * This event is not {@link ICancellableEvent cancellable} and does not have a result,
     * instead, use {@link #setCanLoad(boolean)} to determine if the projectile weapon can start loading.
     */
    public static class Pre extends ProjectileWeaponLoadEvent {
        private final InteractionHand hand;
        private boolean canLoad;

        public Pre(LivingEntity entity, ItemStack weapon, ItemStack projectile, InteractionHand hand, boolean canLoad) {
            super(entity, weapon, projectile);
            this.hand = hand;
            this.canLoad = canLoad;
        }

        /**
         * @return the {@link InteractionHand hand} helding the projectile weapon.
         */
        public InteractionHand getHand() {
            return this.hand;
        }

        /**
         * @return if the {@link ProjectileWeaponItem} can load.
         *         <br>
         *         With vanilla behavior, this usually returns true if {@link #getProjectileItem()} is not empty,
         *         or the living entity is player with {@link Player#hasInfiniteMaterials()} returning true.
         *         <br>
         *         but it's possible for that to not be the case if modder uses a different implementation of the {@link ProjectileWeaponItem}.
         */
        public boolean canLoad() {
            return this.canLoad;
        }

        /**
         * Set if the {@link ProjectileWeaponItem} can load.
         */
        public void setCanLoad(boolean canLoad) {
            this.canLoad = canLoad;
        }
    }

    /**
     * ProjectileWeaponLoadEvent.Post is fired when a living entity finish loading the projectiles in {@link ProjectileWeaponItem#releaseUsing(ItemStack, Level, LivingEntity, int)}.
     * <p>
     * This event is {@link ICancellableEvent cancellable} and does not have a result.
     * <p>
     * When the event is cancelled or {@link #getLoadedProjectiles()} returns an empty list, the {@link ProjectileWeaponItem} fails to load.
     * <br>
     * For {@link BowItem}, no arrows are shot.
     * <br>
     * For {@link CrossbowItem}, the crossbow is not charged.
     */
    public static class Post extends ProjectileWeaponLoadEvent implements ICancellableEvent {
        private final List<ItemStack> loadedProjectiles;

        public Post(LivingEntity entity, ItemStack weapon, ItemStack projectile, List<ItemStack> loadedProjectiles) {
            super(entity, weapon, projectile);
            this.loadedProjectiles = loadedProjectiles;
        }

        /**
         * @return a modifiable list of the loaded projectiles.
         *         With vanilla behavior, this usually returns the result of {@link ProjectileWeaponItem#draw(ItemStack, ItemStack, LivingEntity)},
         *         but it's also possible for that to not be the case if modder uses a different implementation of {@link ProjectileWeaponItem}.
         */
        public List<ItemStack> getLoadedProjectiles() {
            return this.loadedProjectiles;
        }
    }
}
