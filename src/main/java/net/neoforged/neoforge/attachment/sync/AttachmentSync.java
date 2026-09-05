/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.storage.AttachmentDataStorage;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.neoforged.neoforge.registries.callback.AddCallback;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
@EventBusSubscriber(modid = NeoForgeMod.MOD_ID)
public final class AttachmentSync {
    /**
     * Contains all entries added to {@link NeoForgeRegistries#ATTACHMENT_HOLDER_SYNC_HANDLERS} with a sync handler.
     * This ensures that non-synced attachments can be used freely on either side,
     * but synced attachments must match across client and server.
     * This also ensures that we can use the raw ids for network syncing.
     *
     * <p>Should never be registered against directly.
     * Entries are automatically added with {@link #ATTACHMENT_TYPE_ADD_CALLBACK}.
     */
    public static final Registry<net.neoforged.neoforge.attachment.AttachmentType<?>> SYNCED_ATTACHMENT_TYPES = new RegistryBuilder<>(
            ResourceKey.<net.neoforged.neoforge.attachment.AttachmentType<?>>createRegistryKey(
                    Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "synced_attachment_types")))
                            .sync(true)
                            .callback((AddCallback<net.neoforged.neoforge.attachment.AttachmentType<?>>) (registry, id, key, value) -> {
                                // Sanity check to ensure that no entries are added to this registry by accident
                                if (!NeoForgeRegistries.ATTACHMENT_TYPES.containsKey(key.identifier())
                                        || !NeoForgeRegistries.ATTACHMENT_TYPES.containsValue(value)
                                        || NeoForgeRegistries.ATTACHMENT_TYPES.getValue(key.identifier()) != value) {
                                    throw new IllegalStateException("Cannot add entries to the SYNCED_ATTACHMENT_TYPES registry directly.");
                                }
                            })
                            .create();

    public static final AddCallback<net.neoforged.neoforge.attachment.AttachmentType<?>> ATTACHMENT_TYPE_ADD_CALLBACK = (registry, id, key, value) -> {
        if (value.syncHandler != null) {
            Registry.register(SYNCED_ATTACHMENT_TYPES, key.identifier(), value);
        }
    };

    /**
     * Syncs the update (possibly removal) of a single attachment type to a list of players.
     */
    private static <T> void syncUpdate(IAttachmentHolder holder, AttachmentType<T> type, List<ServerPlayer> players) {
        RegistryAccess registryAccess = null;
        for (var player : players) {
            if (type.syncHandler.sendToPlayer(holder, player)) {
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

        var syncHandler = holder.attachmentSyncHandler();
        var handlerKey = NeoForgeRegistries.ATTACHMENT_HOLDER_SYNC_HANDLERS
                .getResourceKey(syncHandler)
                .orElseThrow();

        var packet = new SyncAttachmentsPayload(handlerKey, List.of(type), data).toVanillaClientbound();
        for (var player : players) {
            if (type.syncHandler.sendToPlayer(holder, player)) {
                if (player.connection.hasChannel(SyncAttachmentsPayload.TYPE)) {
                    player.connection.send(packet);
                }
            }
        }
    }

    public static void syncBlockEntityUpdates(BlockEntity blockEntity, List<ServerPlayer> players) {
        var toSync = blockEntity.getAndClearAttachmentTypesToSync();
        if (toSync == null) {
            return;
        }
        // For now, we send one packet per attachment type. In the future, consider bundling all the updates in a single packet.
        for (var type : toSync) {
            if (type.syncHandler == null) {
                continue;
            }
            syncUpdate(blockEntity, type, players);
        }
    }

    public static void syncChunkUpdate(LevelChunk chunk, net.neoforged.neoforge.attachment.AttachmentType<?> type) {
        if (type.syncHandler == null || !(chunk.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (chunk.attachmentDataStorage() instanceof AttachmentHolder attachmentHolder)
            syncUpdate(attachmentHolder, type, serverLevel.getChunkSource().chunkMap.getPlayers(chunk.getPos(), false));
    }

    public static void syncEntityUpdate(Entity entity, net.neoforged.neoforge.attachment.AttachmentType<?> type) {
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

    public static void syncLevelUpdate(ServerLevel level, net.neoforged.neoforge.attachment.AttachmentType<?> type) {
        if (type.syncHandler == null) {
            return;
        }
        syncUpdate(level, type, level.players());
    }

    public static void syncServerUpdate(MinecraftServer server, net.neoforged.neoforge.attachment.AttachmentType<?> type) {
        if (type.syncHandler == null) {
            return;
        }

        if (server.getAttachmentHolder() instanceof AttachmentHolder attachmentHolder)
            syncUpdate(attachmentHolder, type, server.getPlayerList().getPlayers());
    }

    /**
     * Constructs a payload to sync all syncable attachments to a player, if any.
     */
    @Nullable
    private static SyncAttachmentsPayload syncInitialAttachments(IAttachmentHolder holder, AttachmentDataStorage storage, ServerPlayer to) {
        if (!to.connection.hasChannel(SyncAttachmentsPayload.TYPE)) {
            return null;
        }

        var handlerKey = NeoForgeRegistries.ATTACHMENT_HOLDER_SYNC_HANDLERS
                .getResourceKey(holder.attachmentSyncHandler())
                .orElseThrow();

        boolean anySyncableAttachment = storage.storedTypes()
                .anyMatch(type -> type.syncHandler != null);
        if (!anySyncableAttachment) {
            return null;
        }

        List<net.neoforged.neoforge.attachment.AttachmentType<?>> syncedTypes = new ArrayList<>();
        var data = FriendlyByteBufUtil.writeCustomData(buf -> {
            storage.storedTypes()
                    .filter(type -> type.syncHandler != null)
                    .forEach(type -> {
                        int indexBefore = buf.writerIndex();
                        buf.writeBoolean(true);
                        int indexBetween = buf.writerIndex();

                        final var d = storage.getExistingDataOrNull(type);
                        @SuppressWarnings("unchecked")
                        final var untypedHandler = (AttachmentSyncHandler<java.lang.Object>) type.syncHandler().orElseThrow();
                        untypedHandler.write(buf, d, true);

                        if (indexBetween < buf.writerIndex()) {
                            // Actually wrote something
                            syncedTypes.add(type);
                        } else {
                            buf.writerIndex(indexBefore);
                        }
                    });
        }, to.registryAccess());

        return new SyncAttachmentsPayload(handlerKey, syncedTypes, data);
    }

    /**
     * Handles initial syncing of block entity and chunk attachments.
     */
    @SubscribeEvent
    public static void onChunkSent(ChunkWatchEvent.Sent event) {
        if (!(event.getChunk().attachmentDataStorage() instanceof AttachmentHolder attachments))
            return;

        List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
        var chunkPayload = syncInitialAttachments(attachments, attachments, event.getPlayer());
        if (chunkPayload != null) {
            packets.add(chunkPayload.toVanillaClientbound());
        }
        for (var blockEntity : event.getChunk().getBlockEntities().values()) {
            var blockEntityPayload = syncInitialAttachments(blockEntity, blockEntity, event.getPlayer());
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
        var packet = syncInitialAttachments(entity, entity, to);
        if (packet != null) {
            packetConsumer.accept(packet.toVanillaClientbound());
        }
    }

    /**
     * Handles initial syncing of a player's own attachments.
     */
    public static void syncInitialPlayerAttachments(ServerPlayer player) {
        var packet = syncInitialAttachments(player, player, player);
        if (packet != null) {
            player.connection.send(packet.toVanillaClientbound());
        }
    }

    /**
     * Handles initial syncing of level attachments. Needs to be called for login, respawn and teleports.
     */
    public static void syncInitialLevelAttachments(ServerLevel level, ServerPlayer to) {
        var packet = syncInitialAttachments(level, level, to);
        if (packet != null) {
            to.connection.send(packet.toVanillaClientbound());
        }
    }

//    public static void receiveSyncedDataAttachments(IAttachmentHolder holder, RegistryAccess registryAccess, List<net.neoforged.neoforge.attachment.AttachmentType<?>> types, byte[] bytes) {
//        var buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(bytes), registryAccess, ConnectionType.NEOFORGE);
//        try {
//            for (var type : types) {
//                @SuppressWarnings("unchecked")
//                var syncHandler = (AttachmentSyncHandler<java.lang.Object>) type.syncHandler;
//                if (syncHandler == null) {
//                    throw new IllegalArgumentException("Received synced attachment type without a sync handler registered: " + NeoForgeRegistries.ATTACHMENT_TYPES.getKey(type));
//                }
//
//                var previousValue = holder.getExistingDataOrNull(type);
//                boolean hasAttachment = buf.readBoolean();
//                var result = hasAttachment ? syncHandler.read(holder, buf, previousValue) : null;
//                if (result == null) {
//                    if (holder.hasAttachments()) {
//                        holder.removeData(type);
//                    }
//                } else {
//                    holder.setData(type, result);
//                }
//            }
//        } catch (Exception exception) {
//            throw new RuntimeException("Encountered exception when reading synced data attachments: " + types, exception);
//        } finally {
//            buf.release();
//        }
//    }

    private AttachmentSync() {}
}
