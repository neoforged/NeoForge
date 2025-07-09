/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.payload.SyncAttachmentsPayload;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.neoforged.neoforge.registries.callback.AddCallback;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
@EventBusSubscriber(modid = NeoForgeVersion.MOD_ID)
public final class AttachmentSync {
    /**
     * Contains all entries added to {@link NeoForgeRegistries#ATTACHMENT_TYPES} with a sync handler.
     * This ensures that non-synced attachments can be used freely on either side,
     * but synced attachments must match across client and server.
     * This also ensures that we can use the raw ids for network syncing.
     *
     * <p>Should never be registered against directly.
     * Entries are automatically added with {@link #ATTACHMENT_TYPE_ADD_CALLBACK}.
     */
    public static final Registry<AttachmentType<?>> SYNCED_ATTACHMENT_TYPES = new RegistryBuilder<>(
            ResourceKey.<AttachmentType<?>>createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "synced_attachment_types")))
                            .sync(true)
                            .callback((AddCallback<AttachmentType<?>>) (registry, id, key, value) -> {
                                // Sanity check to ensure that no entries are added to this registry by accident
                                if (!NeoForgeRegistries.ATTACHMENT_TYPES.containsKey(key.location())
                                        || !NeoForgeRegistries.ATTACHMENT_TYPES.containsValue(value)
                                        || NeoForgeRegistries.ATTACHMENT_TYPES.getValue(key.location()) != value) {
                                    throw new IllegalStateException("Cannot add entries to the SYNCED_ATTACHMENT_TYPES registry directly.");
                                }
                            })
                            .create();

    public static final AddCallback<AttachmentType<?>> ATTACHMENT_TYPE_ADD_CALLBACK = (registry, id, key, value) -> {
        if (value.syncHandler != null) {
            Registry.register(SYNCED_ATTACHMENT_TYPES, key.location(), value);
        }
    };

    private static SyncAttachmentsPayload.Target syncTarget(AttachmentHolder holder) {
        return switch (holder) {
            case BlockEntity blockEntity -> new SyncAttachmentsPayload.BlockEntityTarget(blockEntity.getBlockPos());
            case AttachmentHolder.AsField asField when asField.getExposedHolder() instanceof LevelChunk chunk -> new SyncAttachmentsPayload.ChunkTarget(chunk.getPos());
            case Entity entity -> new SyncAttachmentsPayload.EntityTarget(entity.getId());
            case Level ignored -> new SyncAttachmentsPayload.LevelTarget();
            default -> throw new UnsupportedOperationException("Attachment holder class is not supported: " + holder);
        };
    }

    /**
     * Syncs the update (possibly removal) of a single attachment type to a list of players.
     */
    private static <T> void syncUpdate(AttachmentHolder holder, AttachmentType<T> type, List<ServerPlayer> players) {
        RegistryAccess registryAccess = null;
        for (var player : players) {
            if (type.syncHandler.sendToPlayer(holder.getExposedHolder(), player)) {
                registryAccess = player.registryAccess();
                break;
            }
        }
        // This also serves as a short-circuit if there are no players to sync data to.
        if (registryAccess == null) {
            return;
        }
        var data = FriendlyByteBufUtil.writeCustomData(buf -> {
            var existingData = holder.getExistingDataOrNull(type);
            if (existingData != null) {
                buf.writeBoolean(true);
                type.syncHandler.write(buf, holder.getData(type), false);
            } else {
                buf.writeBoolean(false);
            }
        }, registryAccess);
        var packet = new SyncAttachmentsPayload(syncTarget(holder), List.of(type), data).toVanillaClientbound();
        for (var player : players) {
            if (type.syncHandler.sendToPlayer(holder.getExposedHolder(), player)) {
                player.connection.send(packet);
            }
        }
    }

    public static void syncBlockEntityUpdate(BlockEntity blockEntity, AttachmentType<?> type) {
        if (type.syncHandler == null || !(blockEntity.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        syncUpdate(blockEntity, type, serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(blockEntity.getBlockPos()), false));
    }

    public static void syncChunkUpdate(LevelChunk chunk, AttachmentHolder.AsField holder, AttachmentType<?> type) {
        if (type.syncHandler == null || !(chunk.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        syncUpdate(holder, type, serverLevel.getChunkSource().chunkMap.getPlayers(chunk.getPos(), false));
    }

    public static void syncEntityUpdate(Entity entity, AttachmentType<?> type) {
        if (type.syncHandler == null || !(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        var players = serverLevel.getChunkSource().chunkMap.getPlayersWatching(entity);
        if (entity instanceof ServerPlayer serverPlayer) {
            // Players do not track themselves
            var newPlayers = new ArrayList<ServerPlayer>(players.size() + 1);
            newPlayers.addAll(players);
            newPlayers.add(serverPlayer);
            players = newPlayers;
        }
        syncUpdate(entity, type, players);
    }

    public static void syncLevelUpdate(ServerLevel level, AttachmentType<?> type) {
        if (type.syncHandler == null) {
            return;
        }
        syncUpdate(level, type, level.players());
    }

    /**
     * Constructs a payload to sync all syncable attachments to a player, if any.
     */
    @Nullable
    private static SyncAttachmentsPayload syncInitialAttachments(AttachmentHolder holder, ServerPlayer to) {
        if (holder.attachments == null) {
            return null;
        }
        boolean anySyncableAttachment = false;
        for (var attachment : holder.attachments.keySet()) {
            anySyncableAttachment = anySyncableAttachment | attachment.syncHandler != null;
        }
        if (!anySyncableAttachment) {
            return null;
        }
        List<AttachmentType<?>> syncedTypes = new ArrayList<>();
        var data = FriendlyByteBufUtil.writeCustomData(buf -> {
            for (var entry : holder.attachments.entrySet()) {
                AttachmentType<?> type = entry.getKey();
                @SuppressWarnings("unchecked")
                var syncHandler = (AttachmentSyncHandler<Object>) type.syncHandler;
                if (syncHandler != null) {
                    int indexBefore = buf.writerIndex();
                    buf.writeBoolean(true);
                    int indexBetween = buf.writerIndex();
                    syncHandler.write(buf, entry.getValue(), true);
                    if (indexBetween < buf.writerIndex()) {
                        // Actually wrote something
                        syncedTypes.add(type);
                    } else {
                        buf.writerIndex(indexBefore);
                    }
                }
            }
        }, to.registryAccess());
        return new SyncAttachmentsPayload(syncTarget(holder), syncedTypes, data);
    }

    /**
     * Handles initial syncing of block entity and chunk attachments.
     */
    @SubscribeEvent
    public static void onChunkSent(ChunkWatchEvent.Sent event) {
        List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
        var chunkPayload = syncInitialAttachments(event.getChunk().getAttachmentHolder(), event.getPlayer());
        if (chunkPayload != null) {
            packets.add(chunkPayload.toVanillaClientbound());
        }
        for (var blockEntity : event.getChunk().getBlockEntities().values()) {
            var blockEntityPayload = syncInitialAttachments(blockEntity, event.getPlayer());
            if (blockEntityPayload != null) {
                packets.add(blockEntityPayload.toVanillaClientbound());
            }
        }
        if (!packets.isEmpty()) {
            event.getPlayer().connection.send(new ClientboundBundlePacket(packets));
        }
    }

    /**
     * Handles initial syncing of entity attachments, except for a player's own attachments.
     */
    public static void syncInitialEntityAttachments(Entity entity, ServerPlayer to, Consumer<Packet<? super ClientGamePacketListener>> packetConsumer) {
        var packet = syncInitialAttachments(entity, to);
        if (packet != null) {
            packetConsumer.accept(packet.toVanillaClientbound());
        }
    }

    /**
     * Handles initial syncing of a player's own attachments.
     */
    public static void syncInitialPlayerAttachments(ServerPlayer player) {
        var packet = syncInitialAttachments(player, player);
        if (packet != null) {
            player.connection.send(packet.toVanillaClientbound());
        }
    }

    /**
     * Handles initial syncing of level attachments. Needs to be called for login, respawn and teleports.
     */
    public static void syncInitialLevelAttachments(ServerLevel level, ServerPlayer to) {
        var packet = syncInitialAttachments(level, to);
        if (packet != null) {
            to.connection.send(packet.toVanillaClientbound());
        }
    }

    public static void receiveSyncedDataAttachments(AttachmentHolder holder, RegistryAccess registryAccess, List<AttachmentType<?>> types, byte[] bytes) {
        var buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(bytes), registryAccess, ConnectionType.NEOFORGE);
        try {
            for (var type : types) {
                @SuppressWarnings("unchecked")
                var syncHandler = (AttachmentSyncHandler<Object>) type.syncHandler;
                if (syncHandler == null) {
                    throw new IllegalArgumentException("Received synced attachment type without a sync handler registered: " + NeoForgeRegistries.ATTACHMENT_TYPES.getKey(type));
                }
                var previousValue = holder.attachments == null ? null : holder.attachments.get(type);
                boolean hasAttachment = buf.readBoolean();
                var result = hasAttachment ? syncHandler.read(holder.getExposedHolder(), buf, previousValue) : null;
                if (result == null) {
                    if (holder.attachments != null) {
                        holder.attachments.remove(type);
                    }
                } else {
                    holder.getAttachmentMap().put(type, result);
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException("Encountered exception when reading synced data attachments: " + types, exception);
        } finally {
            buf.release();
        }
    }

    private AttachmentSync() {}
}
