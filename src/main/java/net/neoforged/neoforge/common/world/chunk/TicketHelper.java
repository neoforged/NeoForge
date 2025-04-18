/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world.chunk;

import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.TicketStorage;
import org.jetbrains.annotations.Nullable;

/**
 * Class to help mods remove no longer valid tickets before they are activated on load.
 */
public class TicketHelper {
    private final Map<BlockPos, TicketSet> blockTickets;
    private final Map<UUID, TicketSet> entityTickets;
    private final TicketStorage saveData;
    private final ResourceLocation controllerId;

    TicketHelper(TicketStorage saveData, ResourceLocation controllerId, Map<BlockPos, TicketSet> blockTickets, Map<UUID, TicketSet> entityTickets) {
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
     * Removes all tickets that a given block was responsible for; both normal and ones forcing natural spawning.
     *
     * @param owner Block that was responsible.
     */
    public void removeAllTickets(BlockPos owner) {
        removeAllTickets(saveData.getBlockForcedChunks(), owner, blockTickets.get(owner));
    }

    /**
     * Removes all tickets that a given entity (UUID) was responsible for; both normal and ones forcing natural spawning.
     *
     * @param owner Entity (UUID) that was responsible.
     */
    public void removeAllTickets(UUID owner) {
        removeAllTickets(saveData.getEntityForcedChunks(), owner, entityTickets.get(owner));
    }

    /**
     * Removes all tickets that a given owner was responsible for; both normal and ones forcing natural spawning.
     */
    private <T extends Comparable<? super T>> void removeAllTickets(ForcedChunkManager.TicketTracker<T> tickets, T owner, @Nullable TicketSet existingTickets) {
        if (existingTickets != null && !existingTickets.isEmpty()) {
            ForcedChunkManager.TicketOwner<T> ticketOwner = new ForcedChunkManager.TicketOwner<>(controllerId, owner);
            for (long chunk : existingTickets.normal()) {
                tickets.remove(ticketOwner, chunk, false, true);
            }
            for (long chunk : existingTickets.naturalSpawning()) {
                tickets.remove(ticketOwner, chunk, true, true);
            }
        }
    }

    /**
     * Removes the ticket for the given chunk that a given block was responsible for.
     *
     * @param owner                block that was responsible
     * @param chunk                chunk to remove ticket of
     * @param forceNaturalSpawning whether the ticket to remove represents a ticket that is forcing natural spawning
     */
    public void removeTicket(BlockPos owner, long chunk, boolean forceNaturalSpawning) {
        TicketSet ticketSet = blockTickets.get(owner);
        if (ticketSet != null && ticketSet.getChunks(forceNaturalSpawning).contains(chunk)) {
            //Don't bother trying to remove it from the save if we don't have it in our set of existing tickets
            removeTicket(saveData.getBlockForcedChunks(), owner, chunk, forceNaturalSpawning);
        }
    }

    /**
     * Removes the ticket for the given chunk that a given entity (UUID) was responsible for.
     *
     * @param owner                entity (UUID) that was responsible
     * @param chunk                chunk to remove ticket of
     * @param forceNaturalSpawning whether the ticket to remove represents a ticket that is forcing natural spawning
     */
    public void removeTicket(UUID owner, long chunk, boolean forceNaturalSpawning) {
        TicketSet ticketSet = entityTickets.get(owner);
        if (ticketSet != null && ticketSet.getChunks(forceNaturalSpawning).contains(chunk)) {
            //Don't bother trying to remove it from the save if we don't have it in our set of existing tickets
            removeTicket(saveData.getEntityForcedChunks(), owner, chunk, forceNaturalSpawning);
        }
    }

    private <T extends Comparable<? super T>> void removeTicket(ForcedChunkManager.TicketTracker<T> tickets, T owner, long chunk, boolean forceNaturalSpawning) {
        tickets.remove(new ForcedChunkManager.TicketOwner<>(controllerId, owner), chunk, forceNaturalSpawning, true);
    }
}
