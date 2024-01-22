/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;

public final class NeoForgeDamageTypeTagsProvider extends DamageTypeTagsProvider {
    public NeoForgeDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, "neoforge", existingFileHelper);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(NeoForgeMod.POISON_DAMAGE, Tags.DamageTypes.IS_POISON);

        tag(DamageTypes.WITHER, Tags.DamageTypes.IS_WITHER);
        tag(DamageTypes.WITHER_SKULL, Tags.DamageTypes.IS_WITHER);

        tag(DamageTypes.MAGIC, Tags.DamageTypes.IS_MAGIC);
        tag(DamageTypes.INDIRECT_MAGIC, Tags.DamageTypes.IS_MAGIC);
        tag(DamageTypes.THORNS, Tags.DamageTypes.IS_MAGIC);
        tag(DamageTypes.DRAGON_BREATH, Tags.DamageTypes.IS_MAGIC);
        tag(Tags.DamageTypes.IS_MAGIC).addTags(Tags.DamageTypes.IS_POISON, Tags.DamageTypes.IS_WITHER);

        tag(DamageTypes.IN_FIRE, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.ON_FIRE, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.LAVA, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.HOT_FLOOR, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.DROWN, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.STARVE, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.DRY_OUT, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.FREEZE, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.LIGHTNING_BOLT, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.CACTUS, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.STALAGMITE, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.FALLING_STALACTITE, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.FALLING_BLOCK, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.FALLING_ANVIL, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.CRAMMING, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.FLY_INTO_WALL, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.SWEET_BERRY_BUSH, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.IN_WALL, Tags.DamageTypes.IS_ENVIRONMENT);

        tag(DamageTypes.CACTUS, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.STALAGMITE, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.FALLING_STALACTITE, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.FALLING_BLOCK, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.FALLING_ANVIL, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.CRAMMING, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.FLY_INTO_WALL, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.SWEET_BERRY_BUSH, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.FALL, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.STING, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.MOB_ATTACK, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.PLAYER_ATTACK, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.MOB_ATTACK_NO_AGGRO, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.ARROW, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.THROWN, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.TRIDENT, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.MOB_PROJECTILE, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.SONIC_BOOM, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.IN_WALL, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.GENERIC, Tags.DamageTypes.IS_PHYSICAL);

        tag(DamageTypes.GENERIC_KILL, Tags.DamageTypes.IS_TECHNICAL);
        tag(DamageTypes.OUTSIDE_BORDER, Tags.DamageTypes.IS_TECHNICAL);
        tag(DamageTypes.FELL_OUT_OF_WORLD, Tags.DamageTypes.IS_TECHNICAL);
    }

    @SafeVarargs
    private void tag(ResourceKey<DamageType> type, TagKey<DamageType>... tags) {
        for (TagKey<DamageType> key : tags) {
            tag(key).add(type);
        }
    }

    @Override
    public String getName() {
        return "NeoForge Damage Type Tags";
    }
}
