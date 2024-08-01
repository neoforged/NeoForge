package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public record Waxable(Block after) {
    public static final Codec<Waxable> CODEC = RecordCodecBuilder.create(in -> in.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("after").forGetter(Waxable::after)).apply(in, Waxable::new));
}
