package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.OptionalInt;

public record ModdedNetworkComponent(ResourceLocation id, Optional<String> version) {

    public ModdedNetworkComponent(FriendlyByteBuf buf) {
        this(buf.readResourceLocation(), buf.readOptional(FriendlyByteBuf::readUtf));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        buf.writeOptional(version, FriendlyByteBuf::writeUtf);
    }
}
