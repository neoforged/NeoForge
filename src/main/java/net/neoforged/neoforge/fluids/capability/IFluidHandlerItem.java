/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.capability;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

/**
 * ItemStacks handled by an {@link IFluidHandler} may change, so this class allows
 * users of the fluid handler to get the container after it has been used.
 *
 * @deprecated Use {@link ResourceHandler} with a {@link FluidResource} instead.
 *             To apply changes to an underlying item container, capture an {@link ItemAccess},
 *             as provided for example by the {@link Capabilities.Fluid#ITEM} capability.
 *             Code that is written against {@link IFluidHandlerItem}
 *             can temporarily use {@link FluidUtil#getFluidHandler(ItemStack)} to ease migration.
 */
@Deprecated(since = "1.21.9", forRemoval = true)
public interface IFluidHandlerItem extends IFluidHandler {
    /**
     * Get the container currently acted on by this fluid handler.
     * The ItemStack may be different from its initial state, in the case of fluid containers that have different items
     * for their filled and empty states.
     * May be an empty item if the container was drained and is consumable.
     *
     * @deprecated There is no equivalent to this method, since in the new system the container is changed directly via an {@link ItemAccess}.
     */
    @Deprecated(since = "1.21.9", forRemoval = true)
    ItemStack getContainer();
}
