/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

/**
 * Extended {@link TooltipContext} used when generating attribute tooltips.
 */
public interface AttributeTooltipContext extends Item.TooltipContext {
    /**
     * {@return the player for whom tooltips are being generated for, if known}
     */
    @Nullable
    Player player();

    /**
     * {@return the tooltip display}
     */
    TooltipDisplay tooltipDisplay();

    /**
     * {@return the current tooltip flag}
     */
    TooltipFlag flag();

    public static AttributeTooltipContext of(@Nullable Player player, Item.TooltipContext itemCtx, TooltipDisplay tooltipDisplay, TooltipFlag flag) {
        return new AttributeTooltipContext() {
            @Override
            @Nullable
            public Provider registries() {
                return itemCtx.registries();
            }

            @Override
            public float tickRate() {
                return itemCtx.tickRate();
            }

            @Override
            @Nullable
            public MapItemSavedData mapData(MapId id) {
                return itemCtx.mapData(id);
            }

            @Override
            @Nullable
            public Level level() {
                return itemCtx.level();
            }

            @Override
            public boolean isPeaceful() {
                return itemCtx.isPeaceful();
            }

            @Nullable
            @Override
            public Player player() {
                return player;
            }

            @Override
            public TooltipDisplay tooltipDisplay() {
                return tooltipDisplay;
            }

            @Override
            public TooltipFlag flag() {
                return flag;
            }
        };
    }
}
