package net.neoforged.neoforge.network.negotiation;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.OptionalInt;

public record NegotiatedNetworkComponent(
        ResourceLocation id,
        Optional<String> version
) {
}
