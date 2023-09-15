package net.feltmc.neoforge.patches.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ICustomPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientboundCustomPayloadPacket.class)
public abstract class ClientboundCustomPayloadPacketMixin implements ICustomPacket<ClientboundCustomPayloadPacket> {
    @Shadow public abstract ResourceLocation getIdentifier();

    @Shadow public abstract FriendlyByteBuf getData();

    @Override public int getIndex() { return Integer.MAX_VALUE; }
    @Override public ResourceLocation getName() { return getIdentifier(); }
    @Override public FriendlyByteBuf getInternalData() { return getData(); }
}
