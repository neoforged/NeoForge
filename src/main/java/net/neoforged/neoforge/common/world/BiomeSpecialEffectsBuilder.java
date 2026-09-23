/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world;

import java.util.Optional;
import net.minecraft.world.level.biome.BiomeSpecialEffects;

/// Extension of the vanilla [BiomeSpecialEffects.Builder]. Provides read access, support for removing overrides and a
/// helper for creating a builder from an existing [BiomeSpecialEffects] and enforces specification of required fields.
public class BiomeSpecialEffectsBuilder extends BiomeSpecialEffects.Builder {
    public static BiomeSpecialEffectsBuilder copyOf(BiomeSpecialEffects baseEffects) {
        BiomeSpecialEffectsBuilder builder = BiomeSpecialEffectsBuilder.create(baseEffects.waterColor());
        builder.grassColorModifier = baseEffects.grassColorModifier();
        baseEffects.foliageColorOverride().ifPresent(builder::foliageColorOverride);
        baseEffects.dryFoliageColorOverride().ifPresent(builder::dryFoliageColorOverride);
        baseEffects.grassColorOverride().ifPresent(builder::grassColorOverride);
        return builder;
    }

    public static BiomeSpecialEffectsBuilder create(int waterColor) {
        return new BiomeSpecialEffectsBuilder(waterColor);
    }

    protected BiomeSpecialEffectsBuilder(int waterColor) {
        super();
        this.waterColor(waterColor);
    }

    public BiomeSpecialEffectsBuilder foliageColorOverride(Optional<Integer> foliageColor) {
        this.foliageColorOverride = foliageColor;
        return this;
    }

    public BiomeSpecialEffectsBuilder dryFoliageColorOverride(Optional<Integer> dryFoliageColor) {
        this.dryFoliageColorOverride = dryFoliageColor;
        return this;
    }

    public BiomeSpecialEffectsBuilder grassColorOverride(Optional<Integer> grassColor) {
        this.grassColorOverride = grassColor;
        return this;
    }

    public int waterColor() {
        return this.waterColor.getAsInt();
    }

    public BiomeSpecialEffects.GrassColorModifier getGrassColorModifier() {
        return this.grassColorModifier;
    }

    public Optional<Integer> getFoliageColorOverride() {
        return this.foliageColorOverride;
    }

    public Optional<Integer> getDryFoliageColorOverride() {
        return this.dryFoliageColorOverride;
    }

    public Optional<Integer> getGrassColorOverride() {
        return this.grassColorOverride;
    }
}
