/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.DataFixer;
import io.netty.channel.ChannelFutureListener;
import java.nio.file.Path;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.Stat;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * A basic fake server player implementation that can be used to simulate player actions.
 */
public class FakePlayer extends ServerPlayer {
    public FakePlayer(ServerLevel level, GameProfile name) {
        super(level.getServer(), level, name, ClientInformation.createDefault());
        this.connection = new FakePlayerNetHandler(level.getServer(), this);
        this.setPermanentlyInvulnerable(true);
    }

    @Override
    public void sendSystemMessage(Component chatComponent, boolean actionBar) {}

    @Override
    public void awardStat(Stat<?> stat, int amount) {}

    @Override
    public boolean canHarmPlayer(Player player) {
        return false;
    }

    @Override
    public void die(DamageSource source) {}

    @Override
    public void tick() {}

    @Override
    public void updateOptions(ClientInformation information) {}

    @Override
    public OptionalInt openMenu(@Nullable MenuProvider menuProvider, @Nullable Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        return OptionalInt.empty();
    }

    @Override
    public void openHorseInventory(AbstractHorse horse, Container container) {}

    @Override
    public boolean startRiding(Entity entityToRide, boolean force, boolean sendEventAndTriggers) {
        return false;
    }

    @Override
    public boolean isFakePlayer() {
        return true;
    }

    public static class FakePlayerAdvancements extends PlayerAdvancements {
        public FakePlayerAdvancements(DataFixer fixer, PlayerList playerList, ServerAdvancementManager manager, Path path, ServerPlayer player) {
            super(fixer, playerList, manager, path, player);
        }

        @Override
        protected void load(ServerAdvancementManager manager) {}

        @Override
        public void setPlayer(ServerPlayer player) {}

        @Override
        public void clearTriggers() {}

        @Override
        public void reload(ServerAdvancementManager manager) {}

        @Override
        public void save() {}

        @Override
        public boolean award(AdvancementHolder advancement, String criterion) {
            return false;
        }

        @Override
        public boolean revoke(AdvancementHolder advancement, String criterion) {
            return false;
        }

        @Override
        public void flushDirty(ServerPlayer player, boolean showAdvancements) {}

        @Override
        public void setSelectedTab(@Nullable AdvancementHolder advancement) {}

        @Override
        public AdvancementProgress getOrStartProgress(AdvancementHolder advancement) {
            return new AdvancementProgress();
        }
    }

    private static class FakePlayerNetHandler extends ServerGamePacketListenerImpl {
        private static final net.minecraft.network.Connection DUMMY_CONNECTION = new FakeConnection();

        public FakePlayerNetHandler(MinecraftServer server, ServerPlayer player) {
            super(server, DUMMY_CONNECTION, player, CommonListenerCookie.createInitial(player.getGameProfile(), false));
        }

        @Override
        public void tick() {}

        @Override
        public void resetPosition() {}

        @Override
        public void disconnect(Component message) {}

        @Override
        public void handlePlayerInput(ServerboundPlayerInputPacket packet) {}

        @Override
        public void handleMoveVehicle(ServerboundMoveVehiclePacket packet) {}

        @Override
        public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket packet) {}

        @Override
        public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket packet) {}

        @Override
        public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket packet) {}

        @Override
        public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket packet) {}

        @Override
        public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket packet) {}

        @Override
        public void handleSetCommandBlock(ServerboundSetCommandBlockPacket packet) {}

        @Override
        public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket packet) {}

        @Override
        public void handleRenameItem(ServerboundRenameItemPacket packet) {}

        @Override
        public void handleSetBeaconPacket(ServerboundSetBeaconPacket packet) {}

        @Override
        public void handleSetStructureBlock(ServerboundSetStructureBlockPacket packet) {}

        @Override
        public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket packet) {}

        @Override
        public void handleJigsawGenerate(ServerboundJigsawGeneratePacket packet) {}

        @Override
        public void handleSelectTrade(ServerboundSelectTradePacket packet) {}

        @Override
        public void handleEditBook(ServerboundEditBookPacket packet) {}

        @Override
        public void handleEntityTagQuery(ServerboundEntityTagQueryPacket packet) {}

        @Override
        public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket packet) {}

        @Override
        public void handleMovePlayer(ServerboundMovePlayerPacket packet) {}

        @Override
        public void teleport(double x, double y, double z, float yaw, float pitch) {}

        @Override
        public void handlePlayerAction(ServerboundPlayerActionPacket packet) {}

        @Override
        public void handleUseItemOn(ServerboundUseItemOnPacket packet) {}

        @Override
        public void handleUseItem(ServerboundUseItemPacket packet) {}

        @Override
        public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket packet) {}

        @Override
        public void handleResourcePackResponse(ServerboundResourcePackPacket packet) {}

        @Override
        public void handlePaddleBoat(ServerboundPaddleBoatPacket packet) {}

        @Override
        public void onDisconnect(DisconnectionDetails details) {}

        @Override
        public void send(Packet<?> packet) {}

        @Override
        public void send(Packet<?> packet, @Nullable ChannelFutureListener sendListener) {}

        @Override
        public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packet) {}

        @Override
        public void handleChat(ServerboundChatPacket packet) {}

        @Override
        public void handlePunch(ServerboundPunchPacket packet) { }

        @Override
        public void handlePlayerCommand(ServerboundPlayerCommandPacket packet) {}

        @Override
        public void handleInteract(ServerboundInteractPacket packet) {}

        @Override
        public void handleClientCommand(ServerboundClientCommandPacket packet) {}

        @Override
        public void handleContainerClose(ServerboundContainerClosePacket packet) {}

        @Override
        public void handleContainerClick(ServerboundContainerClickPacket packet) {}

        @Override
        public void handlePlaceRecipe(ServerboundPlaceRecipePacket packet) {}

        @Override
        public void handleContainerButtonClick(ServerboundContainerButtonClickPacket packet) {}

        @Override
        public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket packet) {}

        @Override
        public void handleSignUpdate(ServerboundSignUpdatePacket packet) {}

        @Override
        public void handleKeepAlive(ServerboundKeepAlivePacket packet) {}

        @Override
        public void handleCustomPayload(ServerboundCustomPayloadPacket packet) {}

        @Override
        public void handleClientInformation(ServerboundClientInformationPacket packet) {}

        @Override
        public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket packet) {}

        @Override
        public void handleChangeDifficulty(ServerboundChangeDifficultyPacket packet) {}

        @Override
        public void handleLockDifficulty(ServerboundLockDifficultyPacket packet) {}

        @Override
        public void teleport(PositionMoveRotation posMoveRot, Set<Relative> relatives) {}

        @Override
        public void ackBlockChangesUpTo(int sequence) {}

        @Override
        public void handleChatCommand(ServerboundChatCommandPacket packet) {}

        @Override
        public void handleChatAck(ServerboundChatAckPacket packet) {}

        @Override
        public void sendPlayerChatMessage(PlayerChatMessage message, ChatType.Bound boundChatType) {}

        @Override
        public void sendDisguisedChatMessage(Component content, ChatType.Bound boundChatType) {}

        @Override
        public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket packet) {}

        @Override
        public boolean hasChannel(Identifier payloadId) {
            return false;
        }
    }

    private static final class FakeConnection extends net.minecraft.network.Connection {
        public FakeConnection() {
            super(PacketFlow.SERVERBOUND);
        }

        @Override
        public void setListenerForServerboundHandshake(PacketListener listener) {}
    }
}
