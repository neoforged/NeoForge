/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.damagesource;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.neoforge.event.entity.living.DamageBlockEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class InternalDamageContainer implements DamageContainer {
    private final EnumMap<Reduction, List<BiFunction<DamageContainer, Float, Float>>> reductionMap = new EnumMap<>(Reduction.class);
    private final float originalDamage;
    private final DamageSource source;
    private float newDamage;
    EnumMap<Reduction, Float> reductions = new EnumMap<>(Reduction.class);
    private float blockedDamage = 0f;
    private float shieldDamage = 0;
    private int invulnerabilityTicksAfterAttack = 20;

    public InternalDamageContainer(DamageSource source, float originalDamage) {
        this.source = source;
        this.originalDamage = originalDamage;
        this.newDamage = originalDamage;
    }

    @Override
    public float getOriginalDamage() {
        return originalDamage;
    }

    @Override
    public DamageSource getSource() {
        return source;
    }

    @Override
    public void setNewDamage(float damage) {
        this.newDamage = damage;
    }

    @Override
    public float getNewDamage() {
        return newDamage;
    }

    public void addModifier(Reduction type, BiFunction<DamageContainer, Float, Float> operation) {
        this.reductionMap.computeIfAbsent(type, a -> new ArrayList<>()).add(operation);
    }

    @Override
    public float getBlockedDamage() {
        return blockedDamage;
    }

    @Override
    public float getShieldDamage() {
        return shieldDamage;
    }

    @Override
    public void setPostAttackInvulnerabilityTicks(int ticks) {
        this.invulnerabilityTicksAfterAttack = ticks;
    }

    @Override
    public int getPostAttackInvulnerabilityTicks() {
        return invulnerabilityTicksAfterAttack;
    }

    @Override
    public float getReduction(Reduction type) {
        return reductions.getOrDefault(type, 0f);
    }

    //=============INTERNAL METHODS - DO NOT USE===================

    @ApiStatus.Internal
    public void setBlockedDamage(DamageBlockEvent event) {
        if (event.getBlocked()) {
            this.blockedDamage = event.getBlockedDamage();
            this.shieldDamage = event.shieldDamage();
            this.newDamage -= this.blockedDamage;
        }
    }

    @ApiStatus.Internal
    public void setReduction(Reduction reduction, float amount) {
        this.reductions.put(reduction, modifyReduction(Reduction.ABSORPTION, amount));
        this.newDamage -= Math.max(0, amount);
    }

    private float modifyReduction(Reduction type, float reduction) {
        for (var func : reductionMap.getOrDefault(type, new ArrayList<>())) {
            reduction = func.apply(this, reduction);
        }
        return reduction;
    }
}
