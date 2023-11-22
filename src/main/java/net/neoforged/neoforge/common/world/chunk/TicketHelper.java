/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world.chunk;

import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ForcedChunksSavedData;

/**
 * Class to help mods remove no longer valid tickets.
 */
public class TicketHelper {
    private final Map<BlockPos, TicketSet> blockTickets;
    private final Map<UUID, TicketSet> entityTickets;
    private final ForcedChunksSavedData saveData;
    private final ResourceLocation controllerId;

    TicketHelper(ForcedChunksSavedData saveData, ResourceLocation controllerId, Map<BlockPos, TicketSet> blockTickets, Map<UUID, TicketSet> entityTickets) {
        this.saveData = saveData;
        this.controllerId = controllerId;
        this.blockTickets = blockTickets;
        this.entityTickets = entityTickets;
    }

    /**
     * {@return all "BLOCK" tickets this controller had registered and which block positions are forcing which chunks}
     *
     * @apiNote This map is unmodifiable and does not update to reflect removed tickets so it is safe to call the remove methods while iterating it.
     */
    public Map<BlockPos, TicketSet> getBlockTickets() {
        return blockTickets;
    }

    /**
     * {@return all "ENTITY" tickets this controller had registered and which entity (UUID) is forcing which chunks}
     *
     * @apiNote This map is unmodifiable and does not update to reflect removed tickets so it is safe to call the remove methods while iterating it.
     */
    public Map<UUID, TicketSet> getEntityTickets() {
        return entityTickets;
    }

    /**
     * Removes all tickets that a given block was responsible for; both ticking and not ticking.
     *
     * @param owner Block that was responsible.
     */
    public void removeAllTickets(BlockPos owner) {
        removeAllTickets(saveData.getBlockForcedChunks(), owner);
    }

    /**
     * Removes all tickets that a given entity (UUID) was responsible for; both ticking and not ticking.
     *
     * @param owner Entity (UUID) that was responsible.
     */
    public void removeAllTickets(UUID owner) {
        removeAllTickets(saveData.getEntityForcedChunks(), owner);
    }

    /**
     * Removes all tickets that a given owner was responsible for; both ticking and not ticking.
     */
    private <T extends Comparable<? super T>> void removeAllTickets(ForcedChunkManager.TicketTracker<T> tickets, T owner) {
        ForcedChunkManager.TicketOwner<T> ticketOwner = new ForcedChunkManager.TicketOwner<>(controllerId, owner);
        if (tickets.chunks.containsKey(ticketOwner) || tickets.tickingChunks.containsKey(ticketOwner)) {
            tickets.chunks.remove(ticketOwner);
            tickets.tickingChunks.remove(ticketOwner);
            saveData.setDirty(true);
        }
    }

    /**
     * Removes the ticket for the given chunk that a given block was responsible for.
     *
     * @param owner   block that was responsible
     * @param chunk   chunk to remove ticket of
     * @param ticking whether or not the ticket to remove represents a ticking set of tickets or not
     */
    public void removeTicket(BlockPos owner, long chunk, boolean ticking) {
        removeTicket(saveData.getBlockForcedChunks(), owner, chunk, ticking);
    }

    /**
     * Removes the ticket for the given chunk that a given entity (UUID) was responsible for.
     *
     * @param owner   entity (UUID) that was responsible
     * @param chunk   chunk to remove ticket of
     * @param ticking whether or not the ticket to remove represents a ticking set of tickets or not
     */
    public void removeTicket(UUID owner, long chunk, boolean ticking) {
        removeTicket(saveData.getEntityForcedChunks(), owner, chunk, ticking);
    }

    private <T extends Comparable<? super T>> void removeTicket(ForcedChunkManager.TicketTracker<T> tickets, T owner, long chunk, boolean ticking) {
        if (tickets.remove(new ForcedChunkManager.TicketOwner<>(controllerId, owner), chunk, ticking))
            saveData.setDirty(true);
    }
}
