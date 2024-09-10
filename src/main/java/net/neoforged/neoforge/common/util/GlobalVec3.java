/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class GlobalVec3 extends Vec3 {
    public final Level level;

    public GlobalVec3(Level level, double x, double y, double z) {
        super(x, y, z);
        this.level = level;
    }

    public GlobalVec3(Level level, Vector3f vector3f) {
        super(vector3f);
        this.level = level;
    }

    public GlobalVec3(Level level, Vec3 vec3) {
        super(vec3.x, vec3.y, vec3.z);
        this.level = level;
    }

    public GlobalVec3(Entity entity) {
        this(entity.level(), entity.position());
    }

    public GlobalVec3(Level level, BlockPos blockpos) {
        this(level, blockpos.getX(), blockpos.getY(), blockpos.getZ());
    }

    public GlobalVec3 withX(double x) {
        return new GlobalVec3(level, x, y, z);
    }

    public GlobalVec3 withY(double y) {
        return new GlobalVec3(level, x, y, z);
    }

    public GlobalVec3 withZ(double z) {
        return new GlobalVec3(level, x, y, z);
    }

    public GlobalVec3 withLevel(Level level) {
        return new GlobalVec3(level, x, y, z);
    }
}
