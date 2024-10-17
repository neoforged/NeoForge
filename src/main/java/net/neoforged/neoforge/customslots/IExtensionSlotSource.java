/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.customslots;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.LivingEntity;

public interface IExtensionSlotSource {
    /**
     * @return
     */
    LivingEntity getOwner();

    ImmutableList<IExtensionSlot> getSlots();

    void onContentsChanged(IExtensionSlot slot);
}
