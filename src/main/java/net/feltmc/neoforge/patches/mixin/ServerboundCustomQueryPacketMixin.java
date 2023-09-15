package net.feltmc.neoforge.patches.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ICustomPacket;
import net.minecraftforge.network.LoginWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerboundCustomQueryPacket.class)
public abstract class ServerboundCustomQueryPacketMixin<T> implements ICustomPacket<ServerboundCustomQueryPacket> {

    @Shadow public abstract FriendlyByteBuf getData();

    @Override public int getIndex() { return Integer.MAX_VALUE; }
    @Override public ResourceLocation getName() { return LoginWrapper.WRAPPER; }
    @Override public FriendlyByteBuf getInternalData() { return getData(); }
}