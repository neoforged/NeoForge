/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.advancements.critereon;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class PiglinNeutralArmorEntityPredicate implements EntitySubPredicate {
    public static final PiglinNeutralArmorEntityPredicate INSTANCE = new PiglinNeutralArmorEntityPredicate();
    public static final MapCodec<PiglinNeutralArmorEntityPredicate> CODEC = MapCodec.unit(INSTANCE);

    private PiglinNeutralArmorEntityPredicate() {}

    @Override
    public MapCodec<PiglinNeutralArmorEntityPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
        if (entity instanceof LivingEntity living) {
            for (ItemStack armor : living.getArmorSlots()) {
                if (!armor.isEmpty() && armor.makesPiglinsNeutral(living)) {
                    return true;
                }
            }
        }
        return false;
    }
}
