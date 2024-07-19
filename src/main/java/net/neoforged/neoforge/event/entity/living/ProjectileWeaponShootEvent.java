/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

/**
 * ProjectileWeaponShootEvent is fired when a living entity shoots a {@link Projectile} with a {@link ProjectileWeaponItem}.
 */
public abstract class ProjectileWeaponShootEvent extends LivingEvent {
    private final @Nullable LivingEntity target;
    private final ItemStack weaponItem;
    private final ItemStack projectileItem;

    private ProjectileWeaponShootEvent(LivingEntity entity, @Nullable LivingEntity target, ItemStack weaponItem, ItemStack projectileItem) {
        super(entity);
        this.target = target;
        this.weaponItem = weaponItem;
        this.projectileItem = projectileItem;
    }

    /**
     * @return the target of the living entity, could be null.
     */
    public @Nullable LivingEntity getTarget() {
        return this.target;
    }

    /**
     * @return the {@link ProjectileWeaponItem} used for shooting.
     */
    public ItemStack getWeaponItem() {
        return weaponItem;
    }

    /**
     * @return the projectile item representing the projectile.
     */
    public ItemStack getProjectileItem() {
        return projectileItem;
    }

    /**
     * ProjectileWeaponShootEvent.Pre is fired when a living entity shoots a {@link Projectile} using a {@link ProjectileWeaponItem},
     * before the movement of the {@link Projectile} is set by {@link Projectile#shoot(double, double, double, float, float)}.
     * <p>
     * This event is {@link ICancellableEvent cancellable} and does not have a result.
     * <p>
     * When the event is cancelled, the {@link Projectile} will not be shot, the ProjectileWeaponShootEvent.Post will not be fired, and the {@link ProjectileWeaponItem} will not be damaged.
     */
    public static class Pre extends ProjectileWeaponShootEvent implements ICancellableEvent {
        private Projectile projectile;
        private float power;
        private float divergence;

        public Pre(LivingEntity entity, Projectile projectile, @Nullable LivingEntity target, ItemStack weaponItem, ItemStack projectileItem, float power, float divergence) {
            super(entity, target, weaponItem, projectileItem);
            this.projectile = projectile;
            this.power = power;
            this.divergence = divergence;
        }

        /**
         * @return the projectile entity
         */
        public Projectile getProjectile() {
            return this.projectile;
        }

        /**
         * Set the projectile entity.
         * 
         * @param projectile the new projectile entity.
         */
        public void setProjectile(Projectile projectile) {
            this.projectile = projectile;
        }

        /**
         * @return the power of the projectile.
         *         With the default implementation of {@link Projectile}, this equals to the speed (m/tick) of the projectile when {@link #getDivergence()} is 0.
         *         <br>
         *         For vanilla {@link BowItem}, the power is 3 when fully charged.
         *         <br>
         *         For vanilla {@link CrossbowItem}, the power is 1.6 for firework rockets, 3.15 for arrows.
         * @see Projectile#getMovementToShoot(double, double, double, float, float)
         * @see BowItem#getPowerForTime(int)
         * @see CrossbowItem#getShootingPower(ChargedProjectiles)
         */
        public float getPower() {
            return this.power;
        }

        /**
         * Set the power of the projectile.
         * 
         * @param power the new power.
         */
        public void setPower(float power) {
            this.power = power;
        }

        /**
         * @return the divergence of the projectile.
         *         With the default implementation of {@link Projectile}, this will add a random offset of ±(1.72275 * divergence)%
         *         to each dimension of the {@link Projectile}'s movement vector.
         * @see Projectile#getMovementToShoot(double, double, double, float, float)
         */
        public float getDivergence() {
            return this.divergence;
        }

        /**
         * Set the divergence of the projectile.
         * 
         * @param divergence the new divergence.
         */
        public void setDivergence(float divergence) {
            this.divergence = divergence;
        }
    }

    /**
     * ProjectileWeaponShootEvent.Post is fired when a living entity has shot a {@link Projectile} using a {@link ProjectileWeaponItem},
     * after the {@link Projectile} has its movement set and joined the {@link Level}, before the {@link ProjectileWeaponItem} is damaged.
     * <p>
     * This event is not {@link ICancellableEvent cancellable} and does not have a result.
     */
    public static class Post extends ProjectileWeaponShootEvent {
        private final Projectile projectile;
        private int weaponDamage;

        public Post(LivingEntity entity, Projectile projectile, @Nullable LivingEntity target, ItemStack weaponItem, ItemStack projectileItem, int weaponDamage) {
            super(entity, target, weaponItem, projectileItem);
            this.projectile = projectile;
        }

        /**
         * @return the projectile entity
         */
        public Projectile getProjectile() {
            return this.projectile;
        }

        /**
         * @return the damage to the {@link ProjectileWeaponItem}.
         */
        public int getWeaponDamage() {
            return this.weaponDamage;
        }

        /**
         * Set the damage to the {@link ProjectileWeaponItem}.
         * 
         * @param weaponDamage the new damage, 0 or negative value result in the item not damaged.
         */
        public void setWeaponDamage(int weaponDamage) {
            this.weaponDamage = weaponDamage;
        }
    }
}
