package net.feltmc.neoforge.patches.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ICustomPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientboundCustomQueryPacket.class)
public abstract class ClientboundCustomQueryPacketMixin<T> implements ICustomPacket<ClientboundCustomQueryPacket> {

    @Shadow public abstract FriendlyByteBuf getData();

    @Shadow public abstract ResourceLocation getIdentifier();

    @Override public int getIndex() { return Integer.MAX_VALUE; }
    @Override public ResourceLocation getName() { return getIdentifier(); }
    @Override public FriendlyByteBuf getInternalData() { return getData(); }
}