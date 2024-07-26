/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.BooleanAttribute;
import org.jetbrains.annotations.Nullable;

public interface IAttributeExtension {
    /**
     * Returns the amount for display in tooltip.
     * <p>
     * With vanilla default, this returns {@code modifier.amount() * 100} for
     * {@link net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation#ADD_MULTIPLIED_BASE} and
     * {@link net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation#ADD_MULTIPLIED_TOTAL}
     * representing the percentage value, and return {@code modifier.amount()} for
     * {@link net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation#ADD_VALUE}
     * representing the absolute value.<br>
     * Especially, {@link Attributes#KNOCKBACK_RESISTANCE} overrides the
     * {@link net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation#ADD_VALUE}
     * case, returning {@code modifier.amount() * 10} for a more user-friendly value.
     * <p>
     * For a full control over the attribute modifier description,
     * override the {@link #getModifierDescription(Player, AttributeModifier, TooltipFlag)} instead.
     * 
     * @param modifier the attribute modifier
     * @return the amount for display
     * @see net.minecraft.world.item.ItemStack#addModifierTooltip(Consumer, Player, Holder, AttributeModifier, TooltipFlag)
     * @see net.minecraft.world.item.alchemy.PotionContents#addPotionTooltip(Iterable, Consumer, float, float, TooltipFlag, Component)
     */
    default double getAmountForDisplay(AttributeModifier modifier, TooltipFlag flag) {
        if (modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE || modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
            return modifier.amount() * 100;
        } else if (this == Attributes.KNOCKBACK_RESISTANCE.value()) {
            return modifier.amount() * 10;
        } else return modifier.amount();
    }

    /**
     * Returns the custom description for the attribute modifier, or null to fall back to default description.
     * <p>
     * Useful for adding modifier specific description like {@link Item#BASE_ATTACK_DAMAGE_ID} and {@link Item#BASE_ATTACK_SPEED_ID}.
     * <p>
     * {@link BooleanAttribute} also overrides this to provide proper description showing enable/disable instead of number values.
     * 
     * @param player   the local player, might be null.
     * @param modifier the attribute modifier
     * @return the custom desription, or null to fall back to default description.
     * @see net.minecraft.world.item.ItemStack#addModifierTooltip(Consumer, Player, Holder, AttributeModifier, TooltipFlag)
     * @see net.minecraft.world.item.alchemy.PotionContents#addPotionTooltip(Iterable, Consumer, float, float, TooltipFlag, Component)
     */
    @Nullable
    default Component getModifierDescription(@Nullable Player player, AttributeModifier modifier, TooltipFlag flag) {
        return null;
    }
}
