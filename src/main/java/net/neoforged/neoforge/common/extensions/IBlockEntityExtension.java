/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface IBlockEntityExtension extends INBTSerializable<CompoundTag> {
    private BlockEntity self() {
        return (BlockEntity) this;
    }

    @Override
    default void deserializeNBT(CompoundTag nbt) {
        self().load(nbt);
    }

    @Override
    default CompoundTag serializeNBT() {
        return self().saveWithFullMetadata();
    }

    /**
     * Called when you receive a TileEntityData packet for the location this
     * TileEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whomever is responsible for
     * sending the packet.
     *
     * @param net The NetworkManager the packet originated from
     * @param pkt The data packet
     */
    default void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag compoundtag = pkt.getTag();
        if (compoundtag != null) {
            self().load(compoundtag);
        }
    }

    /**
     * Called when the chunk's TE update tag, gotten from {@link BlockEntity#getUpdateTag()}, is received on the client.
     * <p>
     * Used to handle this tag in a special way. By default this simply calls {@link BlockEntity#load(CompoundTag)}.
     *
     * @param tag The {@link CompoundTag} sent from {@link BlockEntity#getUpdateTag()}
     */
    default void handleUpdateTag(CompoundTag tag) {
        self().load(tag);
    }

    /**
     * Gets a {@link CompoundTag} that can be used to store custom data for this block entity.
     * It will be written, and read from disc, so it persists over world saves.
     *
     * @return A compound tag for custom persistent data
     */
    CompoundTag getPersistentData();

    default void onChunkUnloaded() {}

    /**
     * Called when this is first added to the world (by {@link LevelChunk#addAndRegisterBlockEntity(BlockEntity)})
     * or right before the first tick when the chunk is generated or loaded from disk.
     * Override instead of adding {@code if (firstTick)} stuff in update.
     */
    default void onLoad() {
        requestModelDataUpdate();
    }

    /**
     * Requests a refresh for the model data of your TE
     * Call this every time your {@link #getModelData()} changes
     */
    default void requestModelDataUpdate() {
        BlockEntity te = self();
        Level level = te.getLevel();
        if (level != null && level.isClientSide) {
            var modelDataManager = level.getModelDataManager();
            if (modelDataManager != null) {
                modelDataManager.requestRefresh(te);
            }
        }
    }

    /**
     * Allows you to return additional model data.
     * This data can be used to provide additional functionality in your {@link BakedModel}
     * You need to schedule a refresh of you model data via {@link #requestModelDataUpdate()} if the result of this function changes.
     * <b>Note that this method may be called on a chunk render thread instead of the main client thread</b>
     * 
     * @return Your model data
     */
    default @NotNull ModelData getModelData() {
        return ModelData.EMPTY;
    }

    /**
     * Returns whether this {@link BlockEntity} has custom outline rendering behavior.
     *
     * @param player the local player currently viewing this {@code BlockEntity}
     * @return {@code true} to enable outline processing
     */
    default boolean hasCustomOutlineRendering(Player player) {
        return false;
    }

    /**
     * Notify all listeners that the capabilities at the positions of this block entity might have changed.
     * This includes new capabilities becoming available.
     * <p>
     * This is just a convenience method for {@link Level#invalidateCapabilities(BlockPos)}.
     */
    @ApiStatus.NonExtendable
    default void invalidateCapabilities() {
        BlockEntity be = self();
        Level level = be.getLevel();
        if (level != null)
            level.invalidateCapabilities(be.getBlockPos());
    }
}
