package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public record Oxidizable(Optional<Block> before, Block after) {
    public static final Codec<Oxidizable> CODEC = RecordCodecBuilder.create(in -> in.group(
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("before").forGetter(Oxidizable::before),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("after").forGetter(Oxidizable::after)
    ).apply(in, Oxidizable::new));
}
