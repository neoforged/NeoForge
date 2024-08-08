/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.color.ColorApplicationResult;
import net.neoforged.neoforge.common.util.TriState;

public interface IDyeItemExtension {
    default DyeItem self() {
        return (DyeItem) this;
    }

    /**
     * Dye is being used on a living entity (ie Sheep). This is only for a DyeItem patch - modded implementations
     * should not use this, and favor handling the result of the colorable capability.
     *
     * @param player   The player using the dye on the entity.
     * @param target   The entity being dyed.
     * @param stack    The item stack being used.
     * @param dyeColor The dye color being used.
     * @return DEFAULT = vanilla logic, TRUE = InteractionResult.SUCCESS, FALSE = InteractionResult.PASS
     */
    default TriState vanillaUseDyeOnEntity(Player player, LivingEntity target, ItemStack stack, DyeColor dyeColor) {
        final var colorable = target.getCapability(Capabilities.Colorable.ENTITY_DYE);
        if (colorable == null) {
            return TriState.DEFAULT;
        }

        final var result = colorable.apply(dyeColor);
        return switch (result) {
            case ALREADY_APPLIED -> TriState.FALSE;
            case APPLIED -> {
                stack.consume(1, player);
                target.level().playSound(player, target, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                yield TriState.TRUE;
            }
            case CANNOT_APPLY -> TriState.FALSE;
        };
    }

    default InteractionResult vanillaUseOn(UseOnContext context) {
        final var player = context.getPlayer();
        final var blockHit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
        final var stack = context.getItemInHand();

        var colorable = context.getLevel().getCapability(Capabilities.Colorable.BLOCK_DYE, context.getClickedPos(), blockHit);
        if (colorable != null) {
            var result = colorable.apply(self().getDyeColor());
            if (result == ColorApplicationResult.APPLIED) {
                return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
            }
        }

        return InteractionResult.PASS;
    }
}
