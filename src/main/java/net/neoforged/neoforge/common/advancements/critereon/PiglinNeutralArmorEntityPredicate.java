/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.advancements.critereon;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class PiglinNeutralArmorEntityPredicate implements ICustomEntityPredicate {
    public static final PiglinNeutralArmorEntityPredicate INSTANCE = new PiglinNeutralArmorEntityPredicate();
    public static final Codec<PiglinNeutralArmorEntityPredicate> CODEC = Codec.unit(INSTANCE);

    private PiglinNeutralArmorEntityPredicate() {}

    @Override
    public Codec<PiglinNeutralArmorEntityPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean test(ServerLevel level, @Nullable Vec3 position, Entity entity) {
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
