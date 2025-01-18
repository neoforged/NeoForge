package net.neoforged.neoforge.client.internal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.internal.NeoForgeProxy;

import java.util.Objects;

public class NeoForgeClientProxy extends NeoForgeProxy {
    @Override
    public boolean isBlockInSolidLayer(BlockState state) {
        return ClientHooks.isBlockInSolidLayer(state);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        ClientPacketListener listener = Objects.requireNonNull(Minecraft.getInstance().getConnection());
        listener.send(payload);
        for (CustomPacketPayload otherPayload : payloads) {
            listener.send(otherPayload);
        }
    }
}
