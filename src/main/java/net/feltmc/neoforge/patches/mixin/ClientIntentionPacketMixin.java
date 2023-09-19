package net.feltmc.neoforge.patches.mixin;

import net.feltmc.neoforge.patches.interfaces.ClientIntentionPacketInterface;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientIntentionPacket.class)
public class ClientIntentionPacketMixin implements ClientIntentionPacketInterface {
    private String fmlVersion = net.minecraftforge.network.NetworkConstants.NETVERSION;

    @Override
    public String getFMLVersion() {
        return this.fmlVersion;
    }
}
