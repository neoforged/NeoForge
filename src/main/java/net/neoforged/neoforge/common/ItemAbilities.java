/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.neoforged.neoforge.common.extensions.IItemExtension;

public class ItemAbilities {
    /**
     * Exposed by axes to allow querying tool behaviours
     */
    public static final ItemAbility AXE_DIG = ItemAbility.get("axe_dig");

    /**
     * Exposed by pickaxes to allow querying tool behaviours
     */
    public static final ItemAbility PICKAXE_DIG = ItemAbility.get("pickaxe_dig");

    /**
     * Exposed by shovels to allow querying tool behaviours
     */
    public static final ItemAbility SHOVEL_DIG = ItemAbility.get("shovel_dig");

    /**
     * Exposed by hoes to allow querying tool behaviours
     */
    public static final ItemAbility HOE_DIG = ItemAbility.get("hoe_dig");

    /**
     * Exposed by swords to allow querying tool behaviours
     */
    public static final ItemAbility SWORD_DIG = ItemAbility.get("sword_dig");

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
     * Used during player attack to figure out if a sweep attack should be performed
     * 
     * @see IItemExtension#getSweepHitBox
     */
    public static final ItemAbility SWORD_SWEEP = ItemAbility.get("sword_sweep");

    /**
     * This action is exposed by shears and corresponds to a harvest action that is triggered with a right click on a block that supports such behaviour.
     * Example: Right click with shears on a beehive with honey level 5 to harvest it
     */
    public static final ItemAbility SHEARS_HARVEST = ItemAbility.get("shears_harvest");

    /**
     * This action is exposed by shears and corresponds to a carve action that is triggered with a right click on a block that supports such behaviour.
     * Example: Right click with shears o a pumpkin to carve it
     */
    public static final ItemAbility SHEARS_CARVE = ItemAbility.get("shears_carve");

    /**
     * This action is exposed by shears and corresponds to a disarm action that is triggered by breaking a block that supports such behaviour.
     * Example: Breaking a trip wire with shears to disarm it.
     */
    public static final ItemAbility SHEARS_DISARM = ItemAbility.get("shears_disarm");

    /**
     * Passed onto {@link IBlockExtension#getToolModifiedState} when a hoe wants to turn dirt into soil
     */
    public static final ItemAbility HOE_TILL = ItemAbility.get("till");

    /**
     * An item ability corresponding to the 'block' action of shields.
     * Items should expose this item ability in order to enable damage blocking when the item is being "used".
     */
    public static final ItemAbility SHIELD_BLOCK = ItemAbility.get("shield_block");

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

    // Default actions supported by each tool type
    public static final Set<ItemAbility> DEFAULT_AXE_ACTIONS = of(AXE_DIG, AXE_STRIP, AXE_SCRAPE, AXE_WAX_OFF);
    public static final Set<ItemAbility> DEFAULT_HOE_ACTIONS = of(HOE_DIG, HOE_TILL);
    public static final Set<ItemAbility> DEFAULT_SHOVEL_ACTIONS = of(SHOVEL_DIG, SHOVEL_FLATTEN);
    public static final Set<ItemAbility> DEFAULT_PICKAXE_ACTIONS = of(PICKAXE_DIG);
    public static final Set<ItemAbility> DEFAULT_SWORD_ACTIONS = of(SWORD_DIG, SWORD_SWEEP);
    public static final Set<ItemAbility> DEFAULT_SHEARS_ACTIONS = of(SHEARS_DIG, SHEARS_HARVEST, SHEARS_CARVE, SHEARS_DISARM);
    public static final Set<ItemAbility> DEFAULT_SHIELD_ACTIONS = of(SHIELD_BLOCK);
    public static final Set<ItemAbility> DEFAULT_FISHING_ROD_ACTIONS = of(FISHING_ROD_CAST);
    public static final Set<ItemAbility> DEFAULT_TRIDENT_ACTIONS = of(TRIDENT_THROW);
    public static final Set<ItemAbility> DEFAULT_BRUSH_ACTIONS = of(BRUSH_BRUSH);

    private static Set<ItemAbility> of(ItemAbility... actions) {
        return Stream.of(actions).collect(Collectors.toCollection(Sets::newIdentityHashSet));
    }
}
