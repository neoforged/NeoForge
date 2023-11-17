/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public final class ForgeEnchantmentTagsProvider extends EnchantmentTagsProvider
{
    public ForgeEnchantmentTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, "neoforge", existingFileHelper);
    }

    @Override
    public void addTags(HolderLookup.Provider lookupProvider)
    {
        tag(Tags.Enchantments.INCREASE_BLOCK_DROPS)
                .add(Enchantments.BLOCK_FORTUNE);
        tag(Tags.Enchantments.INCREASE_ENTITY_DROPS)
                .add(Enchantments.MOB_LOOTING);
        tag(Tags.Enchantments.WEAPON_DAMAGE_ENHANCEMENTS)
                .add(Enchantments.SHARPNESS)
                .add(Enchantments.SMITE)
                .add(Enchantments.BANE_OF_ARTHROPODS)
                .add(Enchantments.POWER_ARROWS)
                .add(Enchantments.IMPALING);
        tag(Tags.Enchantments.ENTITY_SPEED_ENHANCEMENTS)
                .add(Enchantments.SOUL_SPEED)
                .add(Enchantments.SWIFT_SNEAK)
                .add(Enchantments.DEPTH_STRIDER);
        tag(Tags.Enchantments.ENTITY_AUXILIARY_MOVEMENT_ENHANCEMENTS)
                .add(Enchantments.FALL_PROTECTION)
                .add(Enchantments.FROST_WALKER);
        tag(Tags.Enchantments.ENTITY_DEFENSE_ENHANCEMENTS)
                .add(Enchantments.ALL_DAMAGE_PROTECTION)
                .add(Enchantments.BLAST_PROTECTION)
                .add(Enchantments.PROJECTILE_PROTECTION)
                .add(Enchantments.FIRE_PROTECTION)
                .add(Enchantments.RESPIRATION)
                .add(Enchantments.FALL_PROTECTION);
    }

    @Override
    public String getName()
    {
        return "Neoforge Enchantment Tags";
    }
}
