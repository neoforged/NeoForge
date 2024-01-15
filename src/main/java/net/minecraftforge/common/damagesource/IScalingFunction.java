/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.damagesource;

import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

/**
 * An {@link IScalingFunction} is used by custom {@link DamageScaling} instances.<br>
 * This allows finer control over the actual scaling value, instead of the hard-coded vanilla defaults.
 */
@FunctionalInterface
public interface IScalingFunction
{
    /**
     * Default Scaling function used by the vanilla {@link DamageScaling} values.
     * 
     * @implNote Values are based on the code found in {@link Player#hurt(DamageSource, float)}.
     */
    @SuppressWarnings("deprecation")
    IScalingFunction DEFAULT = (source, target, amount, difficulty) -> {
        if (source.scalesWithDifficulty())
        {
            return switch (target.level().getDifficulty())
            {
                case PEACEFUL -> 0.0F;
                case EASY -> Math.min(amount / 2.0F + 1.0F, amount);
                case NORMAL -> amount;
                case HARD -> amount * 1.5F;
            };
        }
        return amount;
    };

    /**
     * Scales the incoming damage amount based on the current difficulty.<br>
     * Only damage dealt to players is scaled, other damage is not impacted.
     * 
     * @param source     The source of the incoming damage.
     * @param target     The player which is being attacked.
     * @param amount     The amount of damage being dealt.
     * @param difficulty The current game difficulty.
     * @return The scaled damage value.
     */
    float scaleDamage(DamageSource source, Player target, float amount, Difficulty difficulty);
}
