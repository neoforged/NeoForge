/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.fml.ModLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

@ParametersAreNonnullByDefault
public class ForcedChunkManager {
    private static final Logger LOGGER = LogManager.getLogger();
    static final TicketType<TicketOwner<BlockPos>> BLOCK = TicketType.create("neoforge:block", Comparator.comparing(info -> info));
    static final TicketType<TicketOwner<BlockPos>> BLOCK_TICKING = TicketType.create("neoforge:block_ticking", Comparator.comparing(info -> info));
    static final TicketType<TicketOwner<UUID>> ENTITY = TicketType.create("neoforge:entity", Comparator.comparing(info -> info));
    static final TicketType<TicketOwner<UUID>> ENTITY_TICKING = TicketType.create("neoforge:entity_ticking", Comparator.comparing(info -> info));

    private static boolean initialised = false;
    private static Map<ResourceLocation, TicketController> controllers = Map.of();

    @ApiStatus.Internal
    public static synchronized void init() {
        if (initialised) {
            throw new UnsupportedOperationException("Cannot init ticket controllers multiple times!");
        }
        initialised = true;

        final Map<ResourceLocation, TicketController> controllers = new HashMap<>();
        ModLoader.get().postEvent(new RegisterTicketControllersEvent(controller -> {
            if (controllers.containsKey(controller.id())) {
                throw new IllegalArgumentException("Attempted to register two controllers with the same ID " + controller.id());
            }
            controllers.put(controller.id(), controller);
        }));
        ForcedChunkManager.controllers = Map.copyOf(controllers);
    }

    /**
     * Checks if a level has any forced chunks. Mainly used for seeing if a level should continue ticking with no players in it.
     */
    public static boolean hasForcedChunks(ServerLevel level) {
        ForcedChunksSavedData data = level.getDataStorage().get(new SavedData.Factory<>(ForcedChunksSavedData::new, ForcedChunksSavedData::load), "chunks");
        if (data == null) return false;
        return !data.getChunks().isEmpty() || !data.getBlockForcedChunks().isEmpty() || !data.getEntityForcedChunks().isEmpty();
    }

    /**
     * Forces a chunk to be loaded for the given mod with the given "owner".
     *
     * @param add {@code true} to force the chunk, {@code false} to unforce the chunk.
     *
     * @implNote Based on {@link ServerLevel#setChunkForced(int, int, boolean)}
     */
    static <T extends Comparable<? super T>> boolean forceChunk(ServerLevel level, ResourceLocation id, T owner, int chunkX, int chunkZ, boolean add, boolean ticking,
            TicketType<TicketOwner<T>> type, Function<ForcedChunksSavedData, TicketTracker<T>> ticketGetter) {
        if (!controllers.containsKey(id)) {
            throw new IllegalArgumentException("Controller with ID " + id + " is not registered!");
        }

        ForcedChunksSavedData saveData = level.getDataStorage().computeIfAbsent(ForcedChunksSavedData.factory(), "chunks");
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        long chunk = pos.toLong();
        TicketTracker<T> tickets = ticketGetter.apply(saveData);
        TicketOwner<T> ticketOwner = new TicketOwner<>(id, owner);
        boolean success;
        if (add) {
            success = tickets.add(ticketOwner, chunk, ticking);
            if (success)
                level.getChunk(chunkX, chunkZ);
        } else {
            success = tickets.remove(ticketOwner, chunk, ticking);
        }
        if (success) {
            saveData.setDirty(true);
            forceChunk(level, pos, type, ticketOwner, add, ticking);
        }
        return success;
    }

    /**
     * Adds/Removes a ticket from the level's chunk provider with the proper levels to match the forced chunks.
     *
     * @param add     {@code true} to force the chunk, {@code false} to unforce the chunk.
     * @param ticking {@code true} to make the chunk receive full chunk ticks even if there is no player nearby.
     *
     * @implNote We use distance 2 for what we pass, as when using register/releaseTicket the ticket's level is set to 33 - distance and the level that forced chunks use
     *           is 31.
     */
    private static <T extends Comparable<? super T>> void forceChunk(ServerLevel level, ChunkPos pos, TicketType<TicketOwner<T>> type, TicketOwner<T> owner, boolean add,
            boolean ticking) {
        if (add)
            level.getChunkSource().addRegionTicket(type, pos, 2, owner, ticking);
        else
            level.getChunkSource().removeRegionTicket(type, pos, 2, owner, ticking);
    }

    /**
     * Reinstates NeoForge's forced chunks when vanilla initially loads a level and reinstates their forced chunks. This method also will validate all the forced
     * chunks with the registered {@link LoadingValidationCallback}s.
     */
    @ApiStatus.Internal
    public static void reinstatePersistentChunks(ServerLevel level, ForcedChunksSavedData saveData) {
        final var controllers = ForcedChunkManager.controllers.entrySet().stream()
                .filter(c -> c.getValue().callback() != null)
                .toList();

        if (!controllers.isEmpty()) {
            //If we have any callbacks, gather all owned tickets by controller for both blocks and entities
            final Map<ResourceLocation, Map<BlockPos, TicketSet>> blockTickets = gatherTicketsById(saveData.getBlockForcedChunks());
            final Map<ResourceLocation, Map<UUID, TicketSet>> entityTickets = gatherTicketsById(saveData.getEntityForcedChunks());
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
        reinstatePersistentChunks(level, BLOCK, saveData.getBlockForcedChunks().chunks, false);
        reinstatePersistentChunks(level, BLOCK_TICKING, saveData.getBlockForcedChunks().tickingChunks, true);
        reinstatePersistentChunks(level, ENTITY, saveData.getEntityForcedChunks().chunks, false);
        reinstatePersistentChunks(level, ENTITY_TICKING, saveData.getEntityForcedChunks().tickingChunks, true);
    }

    /**
     * Gathers tickets into an ID filtered map for use in providing all tickets a controller has registered to its {@link LoadingValidationCallback}.
     */
    private static <T extends Comparable<? super T>> Map<ResourceLocation, Map<T, TicketSet>> gatherTicketsById(TicketTracker<T> tickets) {
        Map<ResourceLocation, Map<T, TicketSet>> modSortedOwnedChunks = new HashMap<>();
        gatherTicketsById(tickets.chunks, TicketSet::nonTicking, modSortedOwnedChunks);
        gatherTicketsById(tickets.tickingChunks, TicketSet::ticking, modSortedOwnedChunks);
        return modSortedOwnedChunks;
    }

    /**
     * Gathers tickets into an ID filtered map for use in providing all tickets a controller has registered to its {@link LoadingValidationCallback}.
     */
    private static <T extends Comparable<? super T>> void gatherTicketsById(Map<TicketOwner<T>, LongSet> tickets, Function<TicketSet, LongSet> typeGetter,
            Map<ResourceLocation, Map<T, TicketSet>> modSortedOwnedChunks) {
        tickets.forEach((owner, values) -> {
            TicketSet pair = modSortedOwnedChunks.computeIfAbsent(owner.id, modId -> new HashMap<>()).computeIfAbsent(owner.owner, o -> new TicketSet(new LongOpenHashSet(), new LongOpenHashSet()));
            typeGetter.apply(pair).addAll(values);
        });
    }

    /**
     * Adds back any persistent forced chunks to the level's chunk provider.
     */
    private static <T extends Comparable<? super T>> void reinstatePersistentChunks(ServerLevel level, TicketType<TicketOwner<T>> type,
            Map<TicketOwner<T>, LongSet> tickets, boolean ticking) {
        tickets.forEach((owner, values) -> {
            for (long chunk : values) {
                forceChunk(level, new ChunkPos(chunk), type, owner, true, ticking);
            }
        });
    }

    /**
     * Writes the mod forced chunks into the NBT compound. Format is List{controllerId, List{ChunkPos, List{BlockPos}, List{UUID}}}
     */
    @ApiStatus.Internal
    public static void writeModForcedChunks(CompoundTag nbt, TicketTracker<BlockPos> blockForcedChunks, TicketTracker<UUID> entityForcedChunks) {
        if (!blockForcedChunks.isEmpty() || !entityForcedChunks.isEmpty()) {
            Map<ResourceLocation, Long2ObjectMap<CompoundTag>> forcedEntries = new HashMap<>();
            writeForcedChunkOwners(forcedEntries, blockForcedChunks, "Blocks", Tag.TAG_COMPOUND, (pos, forcedBlocks) -> forcedBlocks.add(NbtUtils.writeBlockPos(pos)));
            writeForcedChunkOwners(forcedEntries, entityForcedChunks, "Entities", Tag.TAG_INT_ARRAY, (uuid, forcedEntities) -> forcedEntities.add(NbtUtils.createUUID(uuid)));
            ListTag forcedChunks = new ListTag();
            for (Map.Entry<ResourceLocation, Long2ObjectMap<CompoundTag>> entry : forcedEntries.entrySet()) {
                CompoundTag forcedEntry = new CompoundTag();
                forcedEntry.putString("Controller", entry.getKey().toString());
                ListTag modForced = new ListTag();
                modForced.addAll(entry.getValue().values());
                forcedEntry.put("ModForced", modForced);
                forcedChunks.add(forcedEntry);
            }
            nbt.put("ModForced", forcedChunks);
        }
    }

    private static <T extends Comparable<? super T>> void writeForcedChunkOwners(Map<ResourceLocation, Long2ObjectMap<CompoundTag>> forcedEntries, TicketTracker<T> tracker,
            String listKey, int listType, BiConsumer<T, ListTag> ownerWriter) {
        writeForcedChunkOwners(forcedEntries, tracker.chunks, listKey, listType, ownerWriter);
        writeForcedChunkOwners(forcedEntries, tracker.tickingChunks, "Ticking" + listKey, listType, ownerWriter);
    }

    private static <T extends Comparable<? super T>> void writeForcedChunkOwners(Map<ResourceLocation, Long2ObjectMap<CompoundTag>> forcedEntries,
            Map<TicketOwner<T>, LongSet> forcedChunks, String listKey, int listType, BiConsumer<T, ListTag> ownerWriter) {
        for (Map.Entry<TicketOwner<T>, LongSet> entry : forcedChunks.entrySet()) {
            Long2ObjectMap<CompoundTag> modForced = forcedEntries.computeIfAbsent(entry.getKey().id, modId -> new Long2ObjectOpenHashMap<>());
            for (long chunk : entry.getValue()) {
                CompoundTag modEntry = modForced.computeIfAbsent(chunk, chunkPos -> {
                    CompoundTag baseEntry = new CompoundTag();
                    baseEntry.putLong("Chunk", chunkPos);
                    return baseEntry;
                });
                ListTag ownerList = modEntry.getList(listKey, listType);
                ownerWriter.accept(entry.getKey().owner, ownerList);
                //Note: As getList returns a new list in the case the data is of the wrong type,
                // we need to mimic was vanilla does in various places and put our list back in
                // the CompoundNBT regardless.
                modEntry.put(listKey, ownerList);
            }
        }
    }

    /**
     * Reads the mod forced chunks from the NBT compound. Format is List{controllerId, List{ChunkPos, List{BlockPos}, List{UUID}}}
     */
    @ApiStatus.Internal
    public static void readModForcedChunks(CompoundTag nbt, TicketTracker<BlockPos> blockForcedChunks, TicketTracker<UUID> entityForcedChunks) {
        ListTag forcedChunks = nbt.getList("ModForced", Tag.TAG_COMPOUND);
        for (int i = 0; i < forcedChunks.size(); i++) {
            CompoundTag forcedEntry = forcedChunks.getCompound(i);
            ResourceLocation controllerId;
            if (forcedEntry.contains("Controller", Tag.TAG_STRING)) {
                controllerId = new ResourceLocation(forcedEntry.getString("Controller"));
            } else {
                controllerId = new ResourceLocation(forcedEntry.getString("Mod"), "default");
            }
            if (controllers.containsKey(controllerId)) {
                ListTag modForced = forcedEntry.getList("ModForced", Tag.TAG_COMPOUND);
                for (int j = 0; j < modForced.size(); j++) {
                    CompoundTag modEntry = modForced.getCompound(j);
                    long chunkPos = modEntry.getLong("Chunk");
                    readBlockForcedChunks(controllerId, chunkPos, modEntry, "Blocks", blockForcedChunks.chunks);
                    readBlockForcedChunks(controllerId, chunkPos, modEntry, "TickingBlocks", blockForcedChunks.tickingChunks);
                    readEntityForcedChunks(controllerId, chunkPos, modEntry, "Entities", entityForcedChunks.chunks);
                    readEntityForcedChunks(controllerId, chunkPos, modEntry, "TickingEntities", entityForcedChunks.tickingChunks);
                }
            } else {
                LOGGER.warn("Found chunk loading data for controller id {} which is currently not available or active - it will be removed from the level save.", controllerId);
            }
        }
    }

    /**
     * Reads the forge block forced chunks.
     */
    private static void readBlockForcedChunks(ResourceLocation controllerId, long chunkPos, CompoundTag modEntry, String key, Map<TicketOwner<BlockPos>, LongSet> blockForcedChunks) {
        ListTag forcedBlocks = modEntry.getList(key, Tag.TAG_COMPOUND);
        for (int k = 0; k < forcedBlocks.size(); k++) {
            blockForcedChunks.computeIfAbsent(new TicketOwner<>(controllerId, NbtUtils.readBlockPos(forcedBlocks.getCompound(k))), owner -> new LongOpenHashSet()).add(chunkPos);
        }
    }

    /**
     * Reads the forge entity forced chunks.
     */
    private static void readEntityForcedChunks(ResourceLocation controllerId, long chunkPos, CompoundTag modEntry, String key, Map<TicketOwner<UUID>, LongSet> entityForcedChunks) {
        ListTag forcedEntities = modEntry.getList(key, Tag.TAG_INT_ARRAY);
        for (Tag uuid : forcedEntities) {
            entityForcedChunks.computeIfAbsent(new TicketOwner<>(controllerId, NbtUtils.loadUUID(uuid)), owner -> new LongOpenHashSet()).add(chunkPos);
        }
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
        final Map<TicketOwner<T>, LongSet> chunks = new HashMap<>();
        final Map<TicketOwner<T>, LongSet> tickingChunks = new HashMap<>();

        /**
         * Gets an unmodifiable view of the tracked chunks.
         */
        public Map<TicketOwner<T>, LongSet> getChunks() {
            return Collections.unmodifiableMap(chunks);
        }

        /**
         * Gets an unmodifiable view of the tracked fully ticking chunks.
         */
        public Map<TicketOwner<T>, LongSet> getTickingChunks() {
            return Collections.unmodifiableMap(tickingChunks);
        }

        /**
         * Checks if this tracker is empty.
         *
         * @return {@code true} if there are no chunks or ticking chunks being tracked.
         */
        public boolean isEmpty() {
            return chunks.isEmpty() && tickingChunks.isEmpty();
        }

        private Map<TicketOwner<T>, LongSet> getTickets(boolean ticking) {
            return ticking ? tickingChunks : chunks;
        }

        /**
         * @return {@code true} if the state changed.
         */
        public boolean remove(TicketOwner<T> owner, long chunk, boolean ticking) {
            Map<TicketOwner<T>, LongSet> tickets = getTickets(ticking);
            if (tickets.containsKey(owner)) {
                LongSet ticketChunks = tickets.get(owner);
                if (ticketChunks.remove(chunk)) {
                    if (ticketChunks.isEmpty())
                        tickets.remove(owner);
                    return true;
                }
            }
            return false;
        }

        /**
         * @return {@code true} if the state changed.
         */
        private boolean add(TicketOwner<T> owner, long chunk, boolean ticking) {
            return getTickets(ticking).computeIfAbsent(owner, o -> new LongOpenHashSet()).add(chunk);
        }
    }
}
