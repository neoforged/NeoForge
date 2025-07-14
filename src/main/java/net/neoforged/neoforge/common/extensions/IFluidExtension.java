package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.common.world.FluidBehaviour.DripstoneDripInfo;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;

public interface IFluidExtension
{
    private Fluid self() {
        return (Fluid) this;
    }

    default boolean canBePlacedInWorld(@Nullable Entity entity, BlockGetter level, BlockPos pos, FluidStack stack) {
        return self().canBePlacedInWorld(entity, level, pos, self().getStateForPlacement(level, pos, stack));
    }

    default boolean canBePlacedInWorld(@Nullable Entity entity, BlockGetter level, BlockPos pos, FluidState state) {
        return self().canBePlacedInWorld(entity, level, pos);
    }

    default boolean canBePlacedInWorld(@Nullable Entity entity, BlockGetter level, BlockPos pos) {
        return self().canBePlacedInWorld(level, pos);
    }

    default float getExplosionResistance(FluidState fluidState, BlockGetter level, BlockPos pos, Explosion explosion) {
        return self().getExplosionResistance();
    }

    default boolean canVehicleRideUnder(FluidState fluidState, BlockGetter level, BlockPos pos, Entity entity, Entity rider)  {
        return self().canVehicleRideUnder(level, pos, entity, rider);
    }

    default boolean move(FluidState fluidState, LivingEntity entity, Vec3 movementVector, double gravity) {
        return self().move(entity, movementVector, gravity);
    }

    default boolean move(FluidState fluidState, ItemEntity entity, Vec3 movementVector, double gravity) {
        return self().move(entity, movementVector, gravity);
    }

    default boolean canConvertToSource(FluidState fluidState, ServerLevel level, BlockPos pos) {
        return self().canConvertToSource(level, pos);
    }

    default boolean supportsBoating(FluidState fluidState, AbstractBoat boat) {
        return self().supportsBoating(boat);
    }

    @org.jetbrains.annotations.Nullable
    default PathType getBlockPathType(FluidState fluidState, BlockGetter level, BlockPos pos, @org.jetbrains.annotations.Nullable Mob mob, boolean canFluidLog) {
        return self().getBlockPathType(level, pos, mob, canFluidLog);
    }

    @org.jetbrains.annotations.Nullable
    default PathType getAdjacentBlockPathType(FluidState fluidState, BlockGetter level, BlockPos pos, @org.jetbrains.annotations.Nullable Mob mob, PathType originalType) {
        return self().getAdjacentBlockPathType(level, pos, mob, originalType);
    }

    default boolean canHydrate(FluidState fluidState, BlockGetter level, BlockPos pos, BlockState target, BlockPos targetPos) {
        return self().canHydrate(level, pos, target, targetPos);
    }

    default boolean canExtinguish(FluidState fluidState, BlockGetter level, BlockPos pos) {
        return self().canExtinguish(level, pos);
    }

    @Nullable
    default SoundEvent getSound(FluidState state, FluidStack stack, SoundAction action) {
        return self().getSound(stack, action);
    }

    @Nullable
    default SoundEvent getSound(FluidState state, Entity entity, SoundAction action) {
        return self().getSound(entity, action);
    }

    default float getFallDistanceModifier(FluidState fluidState, Entity entity) {
        return self().getFallDistanceModifier(entity);
    }

    default boolean canDrownIn(FluidState fluidState, LivingEntity livingEntity) {
        return self().canDrownIn(livingEntity);
    }

    default boolean canStartSwimming(FluidState state, Entity entity) {
        return self().canStartSwimming(entity);
    }

    default boolean canContinueSwimming(FluidState state, Entity entity) {
        return self().canContinueSwimming(entity);
    }

    default double motionScale(FluidState state, Entity entity) {
        return self().motionScale(entity);
    }

    default boolean canPushEntity(FluidState state, Entity entity) {
        return self().canPushEntity(entity);
    }

    default boolean canHydrate(FluidState state, Entity entity) {
        return self().canHydrate(entity);
    }

    default boolean canExtinguish(FluidState state, Entity entity) {
        return self().canExtinguish(entity);
    }

    default boolean shouldHideAdjacentFluidFace(FluidState state, Direction selfFace, FluidState adjacentFluid) {
        return self().shouldHideAdjacentFluidFace(selfFace, adjacentFluid);
    }

    @Nullable
    default DripstoneDripInfo getDripInfo(FluidState state) {
        return self().getDripInfo();
    }

    /**
     * Gets the tint of the fluid in world.
     *
     * @param state The state of the fluid.
     * @param level The level which contains this fluid.
     * @param position The position of the fluid.
     *
     * @return The tint color of the fluid in world, in {@link ARGB} format.
     */
    default int getTintColor(FluidState state, BlockAndTintGetter level, BlockPos position) {
        return 0xFF_FF_FF_FF;
    }
}
