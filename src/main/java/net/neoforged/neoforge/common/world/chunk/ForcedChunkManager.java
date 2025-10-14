/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world.chunk;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TicketStorage;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ParametersAreNonnullByDefault
public class ForcedChunkManager {
    private static final Logger LOGGER = LogManager.getLogger();

    private static boolean initialised = false;
    private static Map<ResourceLocation, TicketController> controllers = Map.of();

    @ApiStatus.Internal
    public static synchronized void init() {
        if (initialised) {
            throw new UnsupportedOperationException("Cannot init ticket controllers multiple times!");
        }
        initialised = true;

        final Map<ResourceLocation, TicketController> controllers = new HashMap<>();
        ModLoader.postEvent(new RegisterTicketControllersEvent(controller -> {
            if (controllers.containsKey(controller.id())) {
                throw new IllegalArgumentException("Attempted to register two controllers with the same ID " + controller.id());
            }
            controllers.put(controller.id(), controller);
        }));
        ForcedChunkManager.controllers = Map.copyOf(controllers);
    }

    /**
     * Checks if a level has any custom forced chunks. Mainly used for seeing if a level should continue ticking with no players in it.
     */
    public static boolean hasForcedChunks(ServerLevel level) {
        TicketStorage data = level.getDataStorage().get(TicketStorage.TYPE);
        if (data == null) return false;
        return !data.getBlockForcedChunks().isEmpty() || !data.getEntityForcedChunks().isEmpty();
    }

    /**
     * Forces a chunk to be loaded for the given mod with the given "owner".
     *
     * @param add {@code true} to force the chunk, {@code false} to unforce the chunk.
     *
     * @implNote Based on {@link ServerLevel#setChunkForced(int, int, boolean)}
     */
    static <T extends Comparable<? super T>> boolean forceChunk(ServerLevel level, ResourceLocation id, T owner, int chunkX, int chunkZ, boolean add,
            boolean forceNaturalSpawning, Function<TicketStorage, TicketTracker<T>> ticketGetter) {
        if (!controllers.containsKey(id)) {
            throw new IllegalArgumentException("Controller with ID " + id + " is not registered!");
        }

        TicketStorage saveData = level.getDataStorage().computeIfAbsent(TicketStorage.TYPE);
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        long chunk = pos.toLong();
        TicketTracker<T> tickets = ticketGetter.apply(saveData);
        TicketOwner<T> ticketOwner = new TicketOwner<>(id, owner);
        if (add) {
            boolean success = tickets.add(ticketOwner, chunk, forceNaturalSpawning);
            if (success)//Force the chunk to actually load in case it was unloaded
                level.getChunk(chunkX, chunkZ);
            return success;
        }
        return tickets.remove(ticketOwner, chunk, forceNaturalSpawning, false);
    }

    /**
     * Reinstates NeoForge's forced chunks when vanilla initially loads a level and reinstates their forced chunks. This method also will validate all the forced
     * chunks with the registered {@link LoadingValidationCallback}s.
     */
    @ApiStatus.Internal
    public static void activateAllDeactivatedTickets(ServerLevel level, TicketStorage saveData) {
        TicketTracker<BlockPos> blockForcedChunks = saveData.getBlockForcedChunks();
        TicketTracker<UUID> entityForcedChunks = saveData.getEntityForcedChunks();
        if (blockForcedChunks.hasNoDeactivatedTickets() && entityForcedChunks.hasNoDeactivatedTickets()) {
            //Skip if there are no deactivated tickets at all
            return;
        }

        final var controllers = ForcedChunkManager.controllers.entrySet().stream()
                .filter(c -> c.getValue().callback() != null)
                .toList();

        if (!controllers.isEmpty()) {
            //If we have any callbacks, gather all owned tickets by controller for both blocks and entities
            final Map<ResourceLocation, Map<BlockPos, TicketSet>> blockTickets = gatherTicketsById(blockForcedChunks, false, true);
            final Map<ResourceLocation, Map<UUID, TicketSet>> entityTickets = gatherTicketsById(entityForcedChunks, false, true);
            //Fire the callbacks allowing them to remove any tickets they don't want anymore
            controllers.forEach((value) -> {
                boolean hasBlockTicket = blockTickets.containsKey(value.getKey());
                boolean hasEntityTicket = entityTickets.containsKey(value.getKey());
                if (hasBlockTicket || hasEntityTicket) {
                    Map<BlockPos, TicketSet> ownedBlockTickets = hasBlockTicket ? Collections.unmodifiableMap(blockTickets.get(value.getKey())) : Collections.emptyMap();
                    Map<UUID, TicketSet> ownedEntityTickets = hasEntityTicket ? Collections.unmodifiableMap(entityTickets.get(value.getKey())) : Collections.emptyMap();
                    value.getValue().callback().validateTickets(level, new TicketHelper(saveData, value.getKey(), ownedBlockTickets, ownedEntityTickets));
                }
            });
        }
        //Reinstate the chunks that we want to load
        blockForcedChunks.activateAllDeactivatedSources();
        entityForcedChunks.activateAllDeactivatedSources();
    }

    /**
     * Gathers tickets into an ID filtered map for use in providing all tickets a controller has registered to its {@link LoadingValidationCallback}.
     */
    private static <T extends Comparable<? super T>> Map<ResourceLocation, Map<T, TicketSet>> gatherTicketsById(TicketTracker<T> tickets, boolean includeLoaded,
            boolean includeDeactivated) {
        Map<ResourceLocation, Map<T, TicketSet>> modSortedOwnedChunks = new HashMap<>();
        if (includeLoaded) {
            gatherTicketsById(tickets.sourcesLoading, TicketSet::normal, modSortedOwnedChunks);
            gatherTicketsById(tickets.sourcesLoadingNaturalSpawning, TicketSet::naturalSpawning, modSortedOwnedChunks);
        }
        if (includeDeactivated) {
            gatherTicketsById(tickets.deactivatedSourcesLoading, TicketSet::normal, modSortedOwnedChunks);
            gatherTicketsById(tickets.deactivatedSourcesLoadingNaturalSpawning, TicketSet::naturalSpawning, modSortedOwnedChunks);
        }
        return modSortedOwnedChunks;
    }

    /**
     * Gathers tickets into an ID filtered map for use in providing all tickets a controller has registered to its {@link LoadingValidationCallback}.
     */
    private static <T extends Comparable<? super T>> void gatherTicketsById(Long2ObjectMap<Set<TicketOwner<T>>> tickets, Function<TicketSet, LongSet> typeGetter,
            Map<ResourceLocation, Map<T, TicketSet>> modSortedOwnedChunks) {
        for (Long2ObjectMap.Entry<Set<TicketOwner<T>>> entry : Long2ObjectMaps.fastIterable(tickets)) {
            long chunk = entry.getLongKey();
            Set<TicketOwner<T>> owners = entry.getValue();
            for (TicketOwner<T> owner : owners) {
                TicketSet pair = modSortedOwnedChunks.computeIfAbsent(owner.id, modId -> new HashMap<>())
                        .computeIfAbsent(owner.owner, o -> new TicketSet(new LongOpenHashSet(), new LongOpenHashSet()));
                typeGetter.apply(pair).add(chunk);
            }
        }
    }

    public record OwnedChunks(ResourceLocation controller, Map<BlockPos, TicketSet> blockChunks, Map<UUID, TicketSet> entityChunks) {
        private static final Codec<Map<BlockPos, TicketSet>> BLOCK_CHUNK_CODEC = NeoForgeExtraCodecs.unboundedMapAsList("position", BlockPos.CODEC, "tickets", TicketSet.CODEC);
        private static final Codec<Map<UUID, TicketSet>> ENTITY_CHUNK_CODEC = NeoForgeExtraCodecs.unboundedMapAsList("uuid", UUIDUtil.CODEC, "tickets", TicketSet.CODEC);
        public static final Codec<OwnedChunks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("controller").forGetter(OwnedChunks::controller),
                BLOCK_CHUNK_CODEC.optionalFieldOf("block_chunks", Map.of()).forGetter(OwnedChunks::blockChunks),
                ENTITY_CHUNK_CODEC.optionalFieldOf("entity_chunks", Map.of()).forGetter(OwnedChunks::entityChunks)).apply(instance, OwnedChunks::new));
    }

    /**
     * Defines any extra parameters we are adding via a P1#add call
     */
    @ApiStatus.Internal
    public static App<Mu<TicketStorage>, List<OwnedChunks>> defineExtraStorageParams() {
        return OwnedChunks.CODEC.listOf().optionalFieldOf("neo_ticket_data", List.of()).forGetter(storage -> {
            //Like vanilla's TicketStorage we want to collect both activated and deactivated tickets so that we can save them
            Map<ResourceLocation, Map<BlockPos, TicketSet>> blockTickets = gatherTicketsById(storage.getBlockForcedChunks(), true, true);
            Map<ResourceLocation, Map<UUID, TicketSet>> entityTickets = gatherTicketsById(storage.getEntityForcedChunks(), true, true);
            Map<ResourceLocation, OwnedChunks> ownedChunks = new HashMap<>();
            for (Map.Entry<ResourceLocation, Map<BlockPos, TicketSet>> entry : blockTickets.entrySet()) {
                ResourceLocation controllerId = entry.getKey();
                ownedChunks.put(controllerId, new OwnedChunks(controllerId, entry.getValue(), new HashMap<>()));
            }
            for (Map.Entry<ResourceLocation, Map<UUID, TicketSet>> entry : entityTickets.entrySet()) {
                ResourceLocation controllerId = entry.getKey();
                OwnedChunks owned = ownedChunks.get(controllerId);
                if (owned == null) {
                    ownedChunks.put(controllerId, new OwnedChunks(controllerId, new HashMap<>(), entry.getValue()));
                } else {
                    owned.entityChunks().putAll(entry.getValue());
                }
            }
            return List.copyOf(ownedChunks.values());
        });
    }

    /**
     * Reads any forced chunks we might have previously saved.
     */
    @ApiStatus.Internal
    public static TicketStorage readStoredTickets(Function<List<Pair<ChunkPos, Ticket>>, TicketStorage> vanillaInitializer, List<Pair<ChunkPos, Ticket>> tickets,
            List<OwnedChunks> ownedChunks) {
        TicketStorage ticketStorage = vanillaInitializer.apply(tickets);
        TicketTracker<BlockPos> blockForcedChunks = ticketStorage.getBlockForcedChunks();
        TicketTracker<UUID> entityForcedChunks = ticketStorage.getEntityForcedChunks();
        for (OwnedChunks ownedChunk : ownedChunks) {
            ResourceLocation controllerId = ownedChunk.controller();
            if (controllers.containsKey(controllerId)) {
                for (Map.Entry<BlockPos, TicketSet> entry : ownedChunk.blockChunks().entrySet()) {
                    blockForcedChunks.inheritDeactivated(new TicketOwner<>(controllerId, entry.getKey()), entry.getValue());
                }
                for (Map.Entry<UUID, TicketSet> entry : ownedChunk.entityChunks().entrySet()) {
                    entityForcedChunks.inheritDeactivated(new TicketOwner<>(controllerId, entry.getKey()), entry.getValue());
                }
            } else {
                LOGGER.warn("Found chunk loading data for controller id {} which is currently not available or active - it will be removed from the level save.", controllerId);
            }
        }
        return ticketStorage;
    }

    /**
     * Helper class to keep track of a ticket owner by controller ID and owner object
     */
    static class TicketOwner<T extends Comparable<? super T>> implements Comparable<TicketOwner<T>> {
        private final ResourceLocation id;
        private final T owner;

        TicketOwner(ResourceLocation id, T owner) {
            this.id = id;
            this.owner = owner;
        }

        @Override
        public int compareTo(TicketOwner<T> other) {
            int res = id.compareTo(other.id);
            return res == 0 ? owner.compareTo(other.owner) : res;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TicketOwner<?> that = (TicketOwner<?>) o;
            return Objects.equals(id, that.id) && Objects.equals(owner, that.owner);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, owner);
        }
    }

    /**
     * Helper class to manage tracking and handling loaded tickets.
     */
    public static class TicketTracker<T extends Comparable<? super T>> {
        private final Long2ObjectMap<Set<TicketOwner<T>>> sourcesLoading = new Long2ObjectOpenHashMap<>();
        private final Long2ObjectMap<Set<TicketOwner<T>>> sourcesLoadingNaturalSpawning = new Long2ObjectOpenHashMap<>();
        private final Long2ObjectMap<Set<TicketOwner<T>>> deactivatedSourcesLoading = new Long2ObjectOpenHashMap<>();
        private final Long2ObjectMap<Set<TicketOwner<T>>> deactivatedSourcesLoadingNaturalSpawning = new Long2ObjectOpenHashMap<>();
        private final Holder<TicketType> naturalSpawningTicketType;
        private final Holder<TicketType> ticketType;
        private final TicketStorage ticketStorage;

        public TicketTracker(TicketStorage ticketStorage, Holder<TicketType> ticketType, Holder<TicketType> naturalSpawningTicketType) {
            this.ticketStorage = ticketStorage;
            this.ticketType = ticketType;
            this.naturalSpawningTicketType = naturalSpawningTicketType;
        }

        public void deactivateTicketsOnClosing() {
            inheritSources(null, sourcesLoading, deactivatedSourcesLoading);
            inheritSources(null, sourcesLoadingNaturalSpawning, deactivatedSourcesLoadingNaturalSpawning);
        }

        private void inheritDeactivated(TicketOwner<T> owner, TicketSet ticketSet) {
            for (long chunk : ticketSet.normal()) {
                deactivatedSourcesLoading.computeIfAbsent(chunk, c -> new HashSet<>()).add(owner);
            }
            for (long chunk : ticketSet.naturalSpawning()) {
                deactivatedSourcesLoadingNaturalSpawning.computeIfAbsent(chunk, c -> new HashSet<>()).add(owner);
            }
        }

        private void activateAllDeactivatedSources() {
            inheritSources(ticketType, deactivatedSourcesLoading, sourcesLoading);
            inheritSources(naturalSpawningTicketType, deactivatedSourcesLoadingNaturalSpawning, sourcesLoadingNaturalSpawning);
        }

        private void inheritSources(@Nullable Holder<TicketType> ticketType, Long2ObjectMap<Set<TicketOwner<T>>> fromSource, Long2ObjectMap<Set<TicketOwner<T>>> toSource) {
            Ticket ticket = ticketType == null ? null : new Ticket(ticketType.value(), ChunkMap.FORCED_TICKET_LEVEL);
            for (Long2ObjectMap.Entry<Set<TicketOwner<T>>> entry : Long2ObjectMaps.fastIterable(fromSource)) {
                long chunk = entry.getLongKey();
                if (ticket != null) {
                    ticketStorage.addTicket(entry.getLongKey(), ticket);
                }
                toSource.computeIfAbsent(chunk, c -> new HashSet<>()).addAll(entry.getValue());
            }
            fromSource.clear();
        }

        /**
         * Checks if this tracker has no deactivated tickets.
         *
         * @return {@code true} if there are no tickets that are currently deactivated.
         */
        public boolean hasNoDeactivatedTickets() {
            return deactivatedSourcesLoading.isEmpty() && deactivatedSourcesLoadingNaturalSpawning.isEmpty();
        }

        /**
         * Checks if this tracker is empty.
         *
         * @return {@code true} if there are no chunks or chunks with forced natural spawning being tracked.
         */
        public boolean isEmpty() {
            return sourcesLoading.isEmpty() && sourcesLoadingNaturalSpawning.isEmpty();
        }

        private Long2ObjectMap<Set<TicketOwner<T>>> getSourcesLoading(boolean forceNaturalSpawning, boolean targetDeactivated) {
            if (targetDeactivated) {
                return forceNaturalSpawning ? deactivatedSourcesLoadingNaturalSpawning : deactivatedSourcesLoading;
            }
            return forceNaturalSpawning ? sourcesLoadingNaturalSpawning : sourcesLoading;
        }

        private Ticket makeTicket(boolean forceNaturalSpawning) {
            Holder<TicketType> type = forceNaturalSpawning ? naturalSpawningTicketType : ticketType;
            return new Ticket(type.value(), ChunkMap.FORCED_TICKET_LEVEL);
        }

        /**
         * @return {@code true} if the state changed.
         */
        public boolean remove(TicketOwner<T> owner, long chunk, boolean forceNaturalSpawning, boolean targetDeactivated) {
            Long2ObjectMap<Set<TicketOwner<T>>> sourcesLoading = getSourcesLoading(forceNaturalSpawning, targetDeactivated);
            Set<TicketOwner<T>> sources = sourcesLoading.get(chunk);
            //If the given source is currently loading the chunk, try to remove it
            if (sources != null && sources.remove(owner)) {
                if (sources.isEmpty()) {
                    //No sources are loading the chunk anymore, we can remove it and the corresponding ticket
                    sourcesLoading.remove(chunk);
                    if (!targetDeactivated) {
                        //If we are targeting a deactivated ticket, we don't need to remove it from the storage as it shouldn't be there
                        ticketStorage.removeTicket(chunk, makeTicket(forceNaturalSpawning));
                    }
                }
                ticketStorage.setDirty();
                return true;
            }
            return false;
        }

        /**
         * @return {@code true} if the state changed.
         */
        private boolean add(TicketOwner<T> owner, long chunk, boolean forceNaturalSpawning) {
            Long2ObjectMap<Set<TicketOwner<T>>> sourcesLoading = getSourcesLoading(forceNaturalSpawning, false);
            Set<TicketOwner<T>> sources = sourcesLoading.computeIfAbsent(chunk, c -> new HashSet<>());
            if (sources.isEmpty()) {
                //Newly managed chunk, add a ticket for it
                ticketStorage.addTicket(chunk, makeTicket(forceNaturalSpawning));
            }
            if (sources.add(owner)) {
                ticketStorage.setDirty();
                return true;
            }
            return false;
        }
    }
}
