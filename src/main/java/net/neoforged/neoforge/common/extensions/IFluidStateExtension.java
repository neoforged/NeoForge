package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.common.world.FluidBehaviour.DripstoneDripInfo;
import org.jetbrains.annotations.Nullable;

public interface IFluidStateExtension {
    private FluidState self() {
        return (FluidState) this;
    }

    default HolderSet<Fluid> getFamily() {
        return self().getType().getFamily()
            .orElse(HolderSet.direct(self().holder()));
    }

    /**
     * Returns whether an entity can ride in a vehicle under the fluid.
     *
     * @param level  the level which contains this fluid
     * @param pos    the location of the fluid
     * @param entity the vehicle being ridden
     * @param rider  the entity riding the vehicle
     * @return {@code true} if the vehicle can be ridden in under this fluid,
     *         {@code false} otherwise
     */
    default boolean canVehicleRideUnder(BlockGetter level, BlockPos pos, Entity entity, Entity rider) {
        return self().getType().canVehicleRideUnder(self(), level, pos, entity, rider);
    }

    /**
     * Returns the explosion resistance of the fluid.
     *
     * @param level the level which contains this fluid
     * @param pos   the location of the fluid
     * @param explosion the explosion the fluid is absorbing
     * @return the amount of the explosion the fluid can absorb
     */
    default float getExplosionResistance(BlockGetter level, BlockPos pos, Explosion explosion) {
        return self().getType().getExplosionResistance(self(), level, pos, explosion);
    }

    /**
     * Performs how a living entity moves when within the fluid. If using custom
     * movement logic, the method should return {@code true}. Otherwise, the
     * movement logic will default to water.
     *
     * @param entity         the entity moving within the fluid
     * @param movementVector the velocity of how the entity wants to move
     * @param gravity        the gravity to apply to the entity
     * @return {@code true} if custom movement logic is performed, {@code false} otherwise
     */
    default boolean move(LivingEntity entity, Vec3 movementVector, double gravity) {
        return self().getType().move(self(), entity, movementVector, gravity);
    }

    /**
     * Performs how an item entity moves when within the fluid. If using custom
     * movement logic, the method should return {@code true}. Otherwise, the
     * movement logic will default to water.
     *
     * @param entity         the entity moving within the fluid
     * @param movementVector the velocity of how the entity wants to move
     * @param gravity        the gravity to apply to the entity
     * @return {@code true} if custom movement logic is performed, {@code false} otherwise
     */
    default boolean move(ItemEntity entity, Vec3 movementVector, double gravity) {
        return self().getType().move(self(), entity, movementVector, gravity);
    }

    /**
     * Returns whether the fluid can create a source.
     *
     * @param level the level which contains this fluid
     * @param pos   the location of the fluid
     * @return {@code true} if the fluid can create a source, {@code false} otherwise
     */
    default boolean canConvertToSource(ServerLevel level, BlockPos pos) {
        return self().getType().canConvertToSource(self(), level, pos);
    }

    /**
     * Returns whether the boat can be used on the fluid.
     *
     * @param boat the boat trying to be used on the fluid
     * @return {@code true} if the boat can be used, {@code false} otherwise
     */
    default boolean supportsBoating(AbstractBoat boat) {
        return self().getType().supportsBoating(self(), boat);
    }

    /**
     * Gets the path type of this fluid when an entity is pathfinding. When
     * {@code null}, uses vanilla behavior.
     *
     * @param level       the level which contains this fluid
     * @param pos         the position of the fluid
     * @param mob         the mob currently pathfinding, may be {@code null}
     * @param canFluidLog {@code true} if the path is being applied for fluids that can log blocks,
     *                    should be checked against if the fluid can log a block
     * @return the path type of this fluid
     */
    @Nullable
    default PathType getBlockPathType(BlockGetter level, BlockPos pos, @org.jetbrains.annotations.Nullable Mob mob, boolean canFluidLog) {
        return self().getType().getBlockPathType(self(), level, pos, mob, canFluidLog);
    }

    /**
     * Gets the path type of the adjacent fluid to a pathfinding entity.
     * Path types with a negative malus are not traversable for the entity.
     * Pathfinding entities will favor paths consisting of a lower malus.
     * When {@code null}, uses vanilla behavior.
     *
     * @param level        the level which contains this fluid
     * @param pos          the position of the fluid
     * @param mob          the mob currently pathfinding, may be {@code null}
     * @param originalType the path type of the source the entity is on
     * @return the path type of this fluid
     */
    @Nullable
    default PathType getAdjacentBlockPathType(BlockGetter level, BlockPos pos, @org.jetbrains.annotations.Nullable Mob mob, PathType originalType) {
        return self().getType().getAdjacentBlockPathType(self(), level, pos, mob, originalType);
    }

    /**
     * Returns whether the block can be hydrated by this fluid.
     *
     * <p>Hydration is an arbitrary word which depends on the block.
     * <ul>
     * <li>A farmland has moisture</li>
     * <li>A sponge can soak up the liquid</li>
     * <li>A coral can live</li>
     * </ul>
     *
     * @param level     the level which contains this fluid
     * @param pos       the position of the fluid
     * @param source    the state of the block being hydrated
     * @param sourcePos the position of the block being hydrated
     * @return {@code true} if the block can be hydrated, {@code false} otherwise
     */
    default boolean canHydrate(BlockGetter level, BlockPos pos, BlockState source, BlockPos sourcePos) {
        return self().getType().canHydrate(self(), level, pos, source, sourcePos);
    }

    /**
     * Returns whether the entity can be hydrated by this fluid.
     *
     * <p>Hydration is an arbitrary word which depends on the entity.
     *
     * @return {@code true} if the block can be hydrated, {@code false} otherwise
     */
    default boolean canHydrate(Entity entity) {
        return self().getType().canHydrate(self(), entity);
    }

    /**
     * Returns whether the block can be extinguished by this fluid.
     *
     * @param level  the level which contains this fluid
     * @param pos    the position of the fluid
     * @return {@code true} if the block can be extinguished, {@code false} otherwise
     */
    default boolean canExtinguish(BlockGetter level, BlockPos pos) {
        return self().getType().canExtinguish(self(), level, pos);
    }

    /**
     * Returns whether the entity can be extinguished by this fluid.
     *
     * @return {@code true} if the block can be extinguished, {@code false} otherwise
     */
    default boolean canExtinguish(Entity entity) {
        return self().getType().canExtinguish(self(), entity);
    }

    /**
     * Returns how much this fluid should scale the damage done to a falling
     * entity when hitting the ground per tick.
     *
     * @param entity the entity in the fluid
     * @return a scalar to multiply to the fall damage
     */
    default float getFallDistanceModifier(Entity entity) {
        return self().getType().getFallDistanceModifier(self(), entity);
    }

    /**
     * Returns whether the entity can drown in this fluid.
     *
     * @param livingEntity the entity to check.
     * @return {@code true} if the entity can drown, {@code false} otherwise
     */
    default boolean canDrownIn(LivingEntity livingEntity) {
        return self().getType().canDrownIn(self(), livingEntity);
    }

    default boolean canStartSwimming(Entity entity) {
        return self().getType().canStartSwimming(self(), entity);
    }

    default boolean canContinueSwimming(Entity entity) {
        return self().getType().canContinueSwimming(self(), entity);
    }

    default double motionScale(Entity entity) {
        return self().getType().motionScale(self(), entity);
    }

    default boolean canPushEntity(Entity entity) {
        return self().getType().canPushEntity(self(), entity);
    }

    @Nullable
    default SoundEvent getSound(Entity entity, SoundAction action) {
        return self().getType().getSound(self(), entity, action);
    }

    /**
     * Determines if a fluid adjacent to the block on the given side should not be rendered.
     *
     * @param selfFace      the face of this block that the fluid is adjacent to
     * @param adjacentFluid the fluid that is touching that face
     * @return true if this block should cause the fluid's face to not render
     */
    default boolean shouldHideAdjacentFluidFace(Direction selfFace, FluidState adjacentFluid) {
        return self().getType().shouldHideAdjacentFluidFace(self(), selfFace, adjacentFluid);
    }

    @javax.annotation.Nullable
    default DripstoneDripInfo getDripInfo() {
        return self().getType().getDripInfo(self());
    }
}
