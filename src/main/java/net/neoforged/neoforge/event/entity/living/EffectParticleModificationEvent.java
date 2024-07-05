/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import java.util.Objects;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

/**
 * Fires for each effect to allow modification or replacement of the particle options (you can set it to null to reset it to default).
 * <br>
 * This event is not {@link ICancellableEvent}.
 * <br>
 * This event is fired on the {@link NeoForge#EVENT_BUS}.
 */
public class EffectParticleModificationEvent extends LivingEvent {
    private final MobEffectInstance effect;
    private final ParticleOptions originalOptions;
    private ParticleOptions options;
    private boolean isVisible;

    public EffectParticleModificationEvent(LivingEntity entity, MobEffectInstance effect) {
        super(entity);
        this.effect = effect;
        this.isVisible = effect.isVisible();
        this.originalOptions = effect.getParticleOptions();
        this.options = this.originalOptions;
    }

    public MobEffectInstance getEffect() {
        return effect;
    }

    public ParticleOptions getOriginalParticleOptions() {
        return originalOptions;
    }

    public ParticleOptions getParticleOptions() {
        return options;
    }

    public void setParticleOptions(@Nullable ParticleOptions options) {
        this.options = Objects.requireNonNullElse(options, originalOptions);
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }
}
