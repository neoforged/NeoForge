/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.predicates.entity.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SnowBootsEntityPredicate implements EntitySubPredicate {
    public static final SnowBootsEntityPredicate INSTANCE = new SnowBootsEntityPredicate();
    public static final Codec<SnowBootsEntityPredicate> CODEC = MapCodec.unitCodec(INSTANCE);

    private SnowBootsEntityPredicate() {}

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
        return entity instanceof LivingEntity living && living.getItemBySlot(EquipmentSlot.FEET).canWalkOnPowderedSnow(living);
    }
}
