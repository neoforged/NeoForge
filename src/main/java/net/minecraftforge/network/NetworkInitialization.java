/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.network;

import net.minecraftforge.network.event.EventNetworkChannel;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.RegistryManager;

import java.util.Arrays;
import java.util.List;

class NetworkInitialization {

    public static SimpleChannel getHandshakeChannel() {
        SimpleChannel handshakeChannel = NetworkRegistry.ChannelBuilder.
                named(NetworkConstants.FML_HANDSHAKE_RESOURCE).
                clientAcceptedVersions(a -> true).
                serverAcceptedVersions(a -> true).
                networkProtocolVersion(() -> NetworkConstants.NETVERSION).
                simpleChannel();

        handshakeChannel.simpleLoginMessageBuilder(HandshakeMessages.C2SAcknowledge.class, 99, LoginNetworkDirection.LOGIN_TO_SERVER).
                decoder(HandshakeMessages.C2SAcknowledge::decode).
                consumerNetworkThread(HandshakeHandler.indexFirst(HandshakeHandler::handleClientAck)).
                add();

        handshakeChannel.simpleLoginMessageBuilder(HandshakeMessages.S2CModData.class, 5, LoginNetworkDirection.LOGIN_TO_CLIENT).
                decoder(HandshakeMessages.S2CModData::decode).
                markAsLoginPacket().
                noResponse().
                consumerNetworkThread(HandshakeHandler.consumerFor(HandshakeHandler::handleModData)).
                add();

        handshakeChannel.simpleLoginMessageBuilder(HandshakeMessages.S2CModList.class, 1, LoginNetworkDirection.LOGIN_TO_CLIENT).
                decoder(HandshakeMessages.S2CModList::decode).
                markAsLoginPacket().
                consumerNetworkThread(HandshakeHandler.consumerFor(HandshakeHandler::handleServerModListOnClient)).
                add();

        handshakeChannel.simpleLoginMessageBuilder(HandshakeMessages.C2SModListReply.class, 2, LoginNetworkDirection.LOGIN_TO_SERVER).
                decoder(HandshakeMessages.C2SModListReply::decode).
                consumerNetworkThread(HandshakeHandler.indexFirst(HandshakeHandler::handleClientModListOnServer)).
                add();

        handshakeChannel.simpleLoginMessageBuilder(HandshakeMessages.S2CRegistry.class, 3, LoginNetworkDirection.LOGIN_TO_CLIENT).
                decoder(HandshakeMessages.S2CRegistry::decode).
                buildLoginPacketList(RegistryManager::generateRegistryPackets). //TODO: Make this non-static, and store a cache on the client.
                consumerNetworkThread(HandshakeHandler.consumerFor(HandshakeHandler::handleRegistryMessage)).
                add();

        handshakeChannel.simpleLoginMessageBuilder(HandshakeMessages.S2CConfigData.class, 4, LoginNetworkDirection.LOGIN_TO_CLIENT).
                decoder(HandshakeMessages.S2CConfigData::decode).
                buildLoginPacketList(ConfigSync.INSTANCE::syncConfigs).
                consumerNetworkThread(HandshakeHandler.consumerFor(HandshakeHandler::handleConfigSync)).
                add();

        handshakeChannel.simpleLoginMessageBuilder(HandshakeMessages.S2CChannelMismatchData.class, 6, LoginNetworkDirection.LOGIN_TO_CLIENT).
                decoder(HandshakeMessages.S2CChannelMismatchData::decode).
                consumerNetworkThread(HandshakeHandler.consumerFor(HandshakeHandler::handleModMismatchData)).
                add();

        return handshakeChannel;
    }

    public static SimpleChannel getPlayChannel() {
         SimpleChannel playChannel = NetworkRegistry.ChannelBuilder.
                named(NetworkConstants.FML_PLAY_RESOURCE).
                clientAcceptedVersions(a -> true).
                serverAcceptedVersions(a -> true).
                networkProtocolVersion(() -> NetworkConstants.NETVERSION).
                simpleChannel();

        playChannel.messageBuilder(PlayMessages.SpawnEntity.class, 0).
                decoder(PlayMessages.SpawnEntity::decode).
                encoder(PlayMessages.SpawnEntity::encode).
                consumerMainThread(PlayMessages.SpawnEntity::handle).
                add();

        playChannel.messageBuilder(PlayMessages.OpenContainer.class,1).
                decoder(PlayMessages.OpenContainer::decode).
                encoder(PlayMessages.OpenContainer::encode).
                consumerMainThread(PlayMessages.OpenContainer::handle).
                add();

        return playChannel;
    }

    public static List<EventNetworkChannel> buildMCRegistrationChannels() {
        final EventNetworkChannel mcRegChannel = NetworkRegistry.ChannelBuilder.
                named(NetworkConstants.MC_REGISTER_RESOURCE).
                clientAcceptedVersions(a -> true).
                serverAcceptedVersions(a -> true).
                networkProtocolVersion(() -> NetworkConstants.NETVERSION).
                eventNetworkChannel();
        mcRegChannel.addListener(MCRegisterPacketHandler.INSTANCE::registerListener);
        final EventNetworkChannel mcUnregChannel = NetworkRegistry.ChannelBuilder.
                named(NetworkConstants.MC_UNREGISTER_RESOURCE).
                clientAcceptedVersions(a -> true).
                serverAcceptedVersions(a -> true).
                networkProtocolVersion(() -> NetworkConstants.NETVERSION).
                eventNetworkChannel();
        mcUnregChannel.addListener(MCRegisterPacketHandler.INSTANCE::unregisterListener);
        return Arrays.asList(mcRegChannel, mcUnregChannel);
    }
}
