package net.neoforged.neoforge.network.negotiation;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.Range;

import java.util.Optional;
import java.util.OptionalInt;

public record NegotiableNetworkComponent(
        ResourceLocation id,
        Optional<String> version,
        Optional<PacketFlow> flow,
        boolean optional
) {
}
