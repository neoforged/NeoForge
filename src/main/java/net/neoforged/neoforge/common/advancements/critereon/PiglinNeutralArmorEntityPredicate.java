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
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class PiglinNeutralArmorEntityPredicate implements EntitySubPredicate {
    public static final PiglinNeutralArmorEntityPredicate INSTANCE = new PiglinNeutralArmorEntityPredicate();
    public static final Codec<PiglinNeutralArmorEntityPredicate> CODEC = MapCodec.unitCodec(INSTANCE);

    private PiglinNeutralArmorEntityPredicate() {}

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
        if (entity instanceof LivingEntity living) {
            for (EquipmentSlot equipmentSlot : EquipmentSlotGroup.ARMOR) {
                if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                    ItemStack armor = living.getItemBySlot(equipmentSlot);
                    if (!armor.isEmpty() && armor.makesPiglinsNeutral(living)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
