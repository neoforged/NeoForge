/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.damagesource;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DeathMessageType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * An {@link IDeathMessageProvider} is used by custom {@link DeathMessageType} instances.<br>
 * This allows providing custom death messages based on the available parameters, instead of the hard-coded vanilla defaults.
 */
public interface IDeathMessageProvider {
    /**
     * Default death message provider used by the vanilla {@link DeathMessageType}s.
     * 
     * @implNote Based off of the implementation in {@link CombatTracker#getDeathMessage()}.
     */
    IDeathMessageProvider DEFAULT = (entity, lastEntry, sigFall) -> {
        DamageSource dmgSrc = lastEntry.source();
        DeathMessageType msgType = dmgSrc.type().deathMessageType();
        if (msgType == DeathMessageType.FALL_VARIANTS && sigFall != null) {
            return entity.getCombatTracker().getFallMessage(sigFall, dmgSrc.getEntity());
        } else if (msgType == DeathMessageType.INTENTIONAL_GAME_DESIGN) {
            String s = "death.attack." + dmgSrc.getMsgId();
            Component component = ComponentUtils.wrapInSquareBrackets(Component.translatable(s + ".link")).withStyle(CombatTracker.INTENTIONAL_GAME_DESIGN_STYLE);
            return Component.translatable(s + ".message", entity.getDisplayName(), component);
        } else {
            // DeathMessageType.DEFAULT or DeathMessageType.FALL_VARIANTS and no fall was available.
            return dmgSrc.getLocalizedDeathMessage(entity);
        }
    };

    /**
     * Computes the death message from the available context.<br>
     * This method is not invoked if there are no available combat entries, since the damage source would not be available.
     * 
     * @param entity              The entity being killed.
     * @param lastEntry           The last entry from the entity's {@link CombatTracker}
     * @param mostSignificantFall The most significant fall inflicted to the entity, from {@link CombatTracker#getMostSignificantFall()}.
     * @return The death message for the slain entity.
     * @see {@link LivingEntity#getCombatTracker()} for additional context.
     */
    Component getDeathMessage(LivingEntity entity, CombatEntry lastEntry, @Nullable CombatEntry mostSignificantFall);
}
