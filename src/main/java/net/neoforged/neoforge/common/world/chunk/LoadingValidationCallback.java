/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world.chunk;

import net.minecraft.server.level.ServerLevel;

@FunctionalInterface
public interface LoadingValidationCallback {
    /**
     * Called back when tickets are about to be loaded and reinstated to allow mods to invalidate and remove specific tickets that may no longer be valid.
     *
     * @param level        The level
     * @param ticketHelper Ticket helper to remove any invalid tickets.
     */
    void validateTickets(ServerLevel level, TicketHelper ticketHelper);
}
