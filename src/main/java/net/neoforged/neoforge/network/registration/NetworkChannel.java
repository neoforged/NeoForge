package net.neoforged.neoforge.network.registration;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.OptionalInt;

public record NetworkChannel(
        ResourceLocation id,
        Optional<String> chosenVersion
) {
}
