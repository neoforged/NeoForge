/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.advancements.critereon;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TridentEntityPredicate implements EntitySubPredicate {
    public static final TridentEntityPredicate INSTANCE = new TridentEntityPredicate();
    public static final MapCodec<TridentEntityPredicate> CODEC = MapCodec.unit(INSTANCE);

    private TridentEntityPredicate() {}

    @Override
    public MapCodec<TridentEntityPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
        return entity instanceof ThrownTrident;
    }
}
