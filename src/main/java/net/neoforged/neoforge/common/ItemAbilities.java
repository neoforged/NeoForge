/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.neoforged.neoforge.common.extensions.IItemExtension;

public class ItemAbilities {
    /**
     * Exposed by shears to allow querying tool behaviours
     */
    public static final ItemAbility SHEARS_DIG = ItemAbility.get("shears_dig");

    /**
     * Passed onto {@link IBlockExtension#getToolModifiedState} when an axe wants to strip a log
     */
    public static final ItemAbility AXE_STRIP = ItemAbility.get("axe_strip");

    /**
     * Passed onto {@link IBlockExtension#getToolModifiedState} when an axe wants to scrape oxidization off copper
     */
    public static final ItemAbility AXE_SCRAPE = ItemAbility.get("axe_scrape");

    /**
     * Passed onto {@link IBlockExtension#getToolModifiedState} when an axe wants to remove wax out of copper
     */
    public static final ItemAbility AXE_WAX_OFF = ItemAbility.get("axe_wax_off");

    /**
     * Passed onto {@link IBlockExtension#getToolModifiedState} when a shovel wants to turn dirt into path
     */
    public static final ItemAbility SHOVEL_FLATTEN = ItemAbility.get("shovel_flatten");

    /**
     * Passed onto {@link IBlockExtension#getToolModifiedState} when a shovel wants to douse a campfire
     */
    public static final ItemAbility SHOVEL_DOUSE = ItemAbility.get("shovel_douse");

    /**
     * Used during player attack to figure out if a sweep attack should be performed
     * 
     * @see IItemExtension#getSweepHitBox
     */
    public static final ItemAbility SWORD_SWEEP = ItemAbility.get("sword_sweep");

    /**
     * This action is exposed by shears and corresponds to a harvest action that is triggered with a right click on a block that supports such behaviour.
     * Example: Right click with shears on a beehive with honey level 5 to harvest it.
     * 
     * @see CommonHooks#tryDispenseShearsHarvestBlock(BlockSource, ItemStack, ServerLevel, BlockPos)
     */
    public static final ItemAbility SHEARS_HARVEST = ItemAbility.get("shears_harvest");

    /**
     * This action is exposed by shears and corresponds to a harvest action that is triggered with a right click on armored wolves.
     */
    public static final ItemAbility SHEARS_REMOVE_ARMOR = ItemAbility.get("shears_remove_armor");

    /**
     * This action is exposed by shears and corresponds to a carve action that is triggered with a right click on a block that supports such behaviour.
     * Example: Right click with shears on a pumpkin to carve it.
     */
    public static final ItemAbility SHEARS_CARVE = ItemAbility.get("shears_carve");

    /**
     * This action is exposed by shears and corresponds to a disarm action that is triggered by breaking a block that supports such behaviour.
     * Example: Breaking a trip wire with shears to disarm it.
     */
    public static final ItemAbility SHEARS_DISARM = ItemAbility.get("shears_disarm");

    /**
     * This action is exposed by shears and corresponds to a trim action that is triggered with a right click on a block that supports such behavior.
     * Example: Right click with shears on a {@link net.minecraft.world.level.block.GrowingPlantHeadBlock growing plant} to stop it from growing.
     */
    public static final ItemAbility SHEARS_TRIM = ItemAbility.get("shears_trim");

    /**
     * Passed onto {@link IBlockExtension#getToolModifiedState} when a hoe wants to turn dirt into soil
     */
    public static final ItemAbility HOE_TILL = ItemAbility.get("till");

    /**
     * This action corresponds to right-clicking the fishing rod to reel it in after earlier casting.
     * Needed for modded fishing rods so that the FishingHook entity can properly function.
     */
    public static final ItemAbility FISHING_ROD_CAST = ItemAbility.get("fishing_rod_cast");

    /**
     * Exposed by trident-like items to allow querying tool behaviours for items that can be thrown like Tridents.
     */
    public static final ItemAbility TRIDENT_THROW = ItemAbility.get("trident_throw");

    /**
     * Exposed by brushes to allow querying tool behaviours for items that can brush Suspicious Blocks.
     */
    public static final ItemAbility BRUSH_BRUSH = ItemAbility.get("brush_brush");

    /**
     * Passed onto {@link IBlockExtension#getToolModifiedState} when flint and steel or fire charge want to light a campfire/candle/cake.
     * Note that dispensers with flint and steel will also use this but will have no player.
     */
    public static final ItemAbility FIRESTARTER_LIGHT = ItemAbility.get("firestarter_light");

    /**
     * Exposed by spyglasses to allow querying tool behaviours for items that can be used to scope.
     */
    public static final ItemAbility SPYGLASS_SCOPE = ItemAbility.get("spyglass_scope");

    // Default actions supported by each tool type
    public static final Set<ItemAbility> DEFAULT_AXE_ACTIONS = of(AXE_STRIP, AXE_SCRAPE, AXE_WAX_OFF);
    public static final Set<ItemAbility> DEFAULT_HOE_ACTIONS = of(HOE_TILL);
    public static final Set<ItemAbility> DEFAULT_SHOVEL_ACTIONS = of(SHOVEL_FLATTEN, SHOVEL_DOUSE);
    public static final Set<ItemAbility> DEFAULT_SHEARS_ACTIONS = of(SHEARS_DIG, SHEARS_HARVEST, SHEARS_REMOVE_ARMOR, SHEARS_CARVE, SHEARS_DISARM, SHEARS_TRIM);
    public static final Set<ItemAbility> DEFAULT_FISHING_ROD_ACTIONS = of(FISHING_ROD_CAST);
    public static final Set<ItemAbility> DEFAULT_TRIDENT_ACTIONS = of(TRIDENT_THROW);
    public static final Set<ItemAbility> DEFAULT_BRUSH_ACTIONS = of(BRUSH_BRUSH);
    public static final Set<ItemAbility> DEFAULT_FLINT_ACTIONS = of(FIRESTARTER_LIGHT);
    public static final Set<ItemAbility> DEFAULT_FIRECHARGE_ACTIONS = of(FIRESTARTER_LIGHT);
    public static final Set<ItemAbility> DEFAULT_SPYGLASS_ACTIONS = of(SPYGLASS_SCOPE);

    private static Set<ItemAbility> of(ItemAbility... actions) {
        return Stream.of(actions).collect(Collectors.toCollection(Sets::newIdentityHashSet));
    }
}
