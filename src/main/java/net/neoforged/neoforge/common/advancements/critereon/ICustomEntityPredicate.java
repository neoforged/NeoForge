/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.advancements.critereon;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Interface that mods can use to define {@link EntityPredicate}s with custom matching logic.
 */
public interface ICustomEntityPredicate {
    /**
     * {@return the codec for this predicate}
     * <p>
     * The codec must be registered to {@link NeoForgeRegistries#ENTITY_PREDICATE_SERIALIZERS}.
     */
    Codec<? extends ICustomEntityPredicate> codec();

    /**
     * Convert to a vanilla {@link EntityPredicate}.
     */
    default EntityPredicate toVanilla() {
        return new EntityPredicate(this);
    }

    /**
     * Evaluates this predicate on the given arguments.
     *
     * @param level    Level the predicate is being tested in.
     * @param position Position in the level the test is being run from.
     * @param entity   Entity to test.
     *
     * @return {@code true} if the input arguments matches the predicate, otherwise {@code false}
     */
    boolean test(ServerLevel level, @Nullable Vec3 position, Entity entity);
}
