/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.advancements.critereon;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SnowBootsEntityPredicate implements EntitySubPredicate {
    public static final SnowBootsEntityPredicate INSTANCE = new SnowBootsEntityPredicate();
    public static final MapCodec<SnowBootsEntityPredicate> CODEC = MapCodec.unit(INSTANCE);

    private SnowBootsEntityPredicate() {}

    @Override
    public MapCodec<SnowBootsEntityPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
        return entity instanceof LivingEntity living && living.getItemBySlot(EquipmentSlot.FEET).canWalkOnPowderedSnow(living);
    }
}
