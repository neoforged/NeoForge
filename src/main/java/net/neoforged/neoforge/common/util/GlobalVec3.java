/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * A subclass of {@link Vec3} that also holds a {@link Level}.
 * <p>
 * Note that the Level is held as a weak reference and is not required. Only the dimension ({@link ResourceKey}) is always available.
 * <p>
 * Methods that take a {@link Vec3} as parameter will throw an {@link IllegalStateException} if they are supplied with a {@link GlobalVec3}
 * that has a different dimension. To avoid this, downgrade the parameter with {@link #toVec3()} if necessary.
 */
public class GlobalVec3 extends Vec3 {
    public final ResourceKey<Level> dimension;
    private final WeakReference<Level> level;

    protected GlobalVec3(ResourceKey<Level> dimension, WeakReference<Level> level, double x, double y, double z) {
        super(x, y, z);
        this.dimension = dimension;
        this.level = level;
    }

    public GlobalVec3(Level level, double x, double y, double z) {
        this(level.dimension(), new WeakReference<>(level), x, y, z);
    }

    public GlobalVec3(ResourceKey<Level> dimension, double x, double y, double z) {
        this(dimension, new WeakReference<>(null), x, y, z);
    }

    public GlobalVec3(Level level, Vector3f vector3f) {
        this(level.dimension(), new WeakReference<>(level), vector3f.x, vector3f.y, vector3f.z);
    }

    public GlobalVec3(ResourceKey<Level> dimension, Vector3f vector3f) {
        this(dimension, new WeakReference<>(null), vector3f.x, vector3f.y, vector3f.z);
    }

    protected GlobalVec3(ResourceKey<Level> dimension, WeakReference<Level> level, Vec3 vec3) {
        this(dimension, level, vec3.x, vec3.y, vec3.z);
    }

    public GlobalVec3(Level level, Vec3 vec3) {
        this(level.dimension(), new WeakReference<>(level), vec3);
    }

    public GlobalVec3(ResourceKey<Level> dimension, Vec3 vec3) {
        this(dimension, new WeakReference<>(null), vec3);
    }

    public GlobalVec3(Entity entity) {
        this(entity.level(), entity.position());
    }

    public GlobalVec3(Level level, BlockPos blockpos) {
        this(level.dimension(), new WeakReference<>(level), blockpos.getX(), blockpos.getY(), blockpos.getZ());
    }

    public GlobalVec3(ResourceKey<Level> dimension, BlockPos blockpos) {
        this(dimension, new WeakReference<>(null), blockpos.getX(), blockpos.getY(), blockpos.getZ());
    }

    public GlobalVec3(GlobalPos globalpos) {
        this(globalpos.dimension(), globalpos.pos());
    }

    // setters

    public GlobalVec3 withX(double x) {
        return new GlobalVec3(dimension, level, x, y, z);
    }

    public GlobalVec3 withY(double y) {
        return new GlobalVec3(dimension, level, x, y, z);
    }

    public GlobalVec3 withZ(double z) {
        return new GlobalVec3(dimension, level, x, y, z);
    }

    public GlobalVec3 withLevel(Level level) {
        return new GlobalVec3(level.dimension(), new WeakReference<>(level), x, y, z);
    }

    public GlobalVec3 withDimension(ResourceKey<Level> dimension) {
        return new GlobalVec3(dimension, new WeakReference<>(null), x, y, z);
    }

    // getters

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    /**
     * Returns the level of this global position if it is available.
     * <p>
     * Use {@link #getLevel(MinecraftServer)} or {@link #getLevel(Level)} if possible.
     * 
     * @return The level.
     */
    public @Nullable Level getLevel() {
        return level.get();
    }

    public Level getLevel(Level anotherLevel) {
        return getLevel(anotherLevel.getServer());
    }

    public Level getLevel(MinecraftServer server) {
        final Level level2 = level.get();
        if (level2 == null) {
            return server.getLevel(dimension);
        }
        return level2;
    }

    // converters

    public Vec3 toVec3() {
        return super.add(0, 0, 0);
    }

    public BlockPos toBlockPos() {
        return BlockPos.containing(this);
    }

    public GlobalPos toGlobalPos() {
        return new GlobalPos(dimension, toBlockPos());
    }

    public GlobalVec3 copy() {
        return new GlobalVec3(dimension, level, x, y, z);
    }

    // method overrides

    protected void sameDim(Vec3 other) {
        if (other instanceof GlobalVec3 other3 && other3.dimension != dimension) {
            throw new IllegalStateException("Cannot compute with GlobalPoses (" + this + " and " + other + ") that are in different dimensions");
        }
    }

    @Override
    public GlobalVec3 vectorTo(Vec3 other) {
        sameDim(other);
        return new GlobalVec3(dimension, level, super.vectorTo(other));
    }

    @Override
    public GlobalVec3 cross(Vec3 other) {
        sameDim(other);
        return new GlobalVec3(dimension, level, super.cross(other));
    }

    @Override
    public GlobalVec3 add(Vec3 other) {
        sameDim(other);
        return this.add(other.x, other.y, other.z);
    }

    @Override
    public GlobalVec3 add(double otherX, double otherY, double otherZ) {
        return new GlobalVec3(dimension, level, super.add(otherX, otherY, otherZ));
    }

    @Override
    public double distanceTo(Vec3 other) {
        sameDim(other);
        return super.distanceTo(other);
    }

    @Override
    public double distanceToSqr(Vec3 other) {
        sameDim(other);
        return super.distanceToSqr(other);
    }

    @Override
    public boolean closerThan(Vec3 other, double horizontal, double vertical) {
        sameDim(other);
        return super.closerThan(other, horizontal, vertical);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof GlobalVec3 other3 && other3.dimension == dimension) {
            return super.equals(other);
        }
        return false;
    }

    @Override
    public GlobalVec3 align(EnumSet<Axis> axis) {
        return new GlobalVec3(dimension, level, super.align(axis));
    }

    @Override
    public GlobalVec3 normalize() {
        return new GlobalVec3(dimension, level, super.normalize());
    }

    @Override
    public GlobalVec3 subtract(Vec3 other) {
        sameDim(other);
        return new GlobalVec3(dimension, level, super.subtract(other));
    }

    @Override
    public GlobalVec3 subtract(double otherX, double otherY, double otherZ) {
        return new GlobalVec3(dimension, level, super.subtract(otherX, otherY, otherZ));
    }

    @Override
    public GlobalVec3 scale(double by) {
        return new GlobalVec3(dimension, level, super.scale(by));
    }

    @Override
    public GlobalVec3 reverse() {
        return new GlobalVec3(dimension, level, super.reverse());
    }

    @Override
    public GlobalVec3 multiply(Vec3 other) {
        sameDim(other);
        return new GlobalVec3(dimension, level, super.multiply(other));
    }

    @Override
    public GlobalVec3 multiply(double otherX, double otherY, double otherZ) {
        return new GlobalVec3(dimension, level, super.multiply(otherX, otherY, otherZ));
    }

    @Override
    public GlobalVec3 offsetRandom(RandomSource rand, float factor) {
        return new GlobalVec3(dimension, level, super.offsetRandom(rand, factor));
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(dimension);
    }

    @Override
    public String toString() {
        return "(" + dimension + ", " + x + ", " + y + ", " + z + ")";
    }

    @Override
    public GlobalVec3 lerp(Vec3 other, double scale) {
        sameDim(other);
        return new GlobalVec3(dimension, level, super.lerp(other, scale));
    }

    @Override
    public GlobalVec3 xRot(float amount) {
        return new GlobalVec3(dimension, level, super.xRot(amount));
    }

    @Override
    public GlobalVec3 yRot(float amount) {
        return new GlobalVec3(dimension, level, super.yRot(amount));
    }

    @Override
    public GlobalVec3 zRot(float amount) {
        return new GlobalVec3(dimension, level, super.zRot(amount));
    }

    @Override
    public GlobalVec3 relative(Direction direction, double scale) {
        return new GlobalVec3(dimension, level, super.relative(direction, scale));
    }

    @Override
    public GlobalVec3 with(Axis axis, double value) {
        return new GlobalVec3(dimension, level, super.with(axis, value));
    }
}
