/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.ApiStatus;

/**
 * A version of {@link TooltipFlag} that knows about Screen and can provide modifier key states. It is patched into all vanilla uses of TooltipFlags in client classes.
 * <p>
 * When calling any tooltip method that needs a TooltipFlag yourself, use either this (by calling {@link #of(TooltipFlag)}) or {@link TooltipFlag.Default} depending on the <em>logical</em> side you're on.
 */
public record ClientTooltipFlag(boolean advanced, boolean creative, boolean shiftDown, boolean controlDown, boolean altDown) implements TooltipFlag {
    @ApiStatus.Internal
    public ClientTooltipFlag {}

    @Override
    public boolean isAdvanced() {
        return this.advanced;
    }

    @Override
    public boolean isCreative() {
        return this.creative;
    }

    @Override
    public boolean hasControlDown() {
        return controlDown;
    }

    @Override
    public boolean hasShiftDown() {
        return shiftDown;
    }

    @Override
    public boolean hasAltDown() {
        return altDown;
    }

    public static TooltipFlag of(TooltipFlag other) {
        Minecraft mc = Minecraft.getInstance();
        return new ClientTooltipFlag(other.isAdvanced(), other.isCreative(), mc.hasShiftDown(), mc.hasControlDown(), mc.hasAltDown());
    }
}
