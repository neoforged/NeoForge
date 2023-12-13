package net.neoforged.neoforge.network.reading;

import net.minecraft.resources.ResourceLocation;

import java.util.OptionalInt;

public record PayloadReadingContext(
        ResourceLocation id,
        OptionalInt version
) {
}
