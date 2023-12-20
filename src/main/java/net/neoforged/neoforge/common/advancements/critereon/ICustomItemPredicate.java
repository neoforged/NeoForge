/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.advancements.critereon;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Interface that mods can use to define {@link ItemPredicate}s with custom matching logic.
 */
public interface ICustomItemPredicate extends Predicate<ItemStack> {
    /**
     * {@return the codec for this predicate}
     * <p>
     * The codec must be registered to {@link NeoForgeRegistries#ITEM_PREDICATE_SERIALIZERS}.
     */
    Codec<? extends ICustomItemPredicate> codec();

    /**
     * Convert to a vanilla {@link ItemPredicate}.
     */
    default ItemPredicate toVanilla() {
        return new ItemPredicate(this);
    }
}
