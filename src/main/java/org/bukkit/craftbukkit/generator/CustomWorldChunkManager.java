package org.bukkit.craftbukkit.generator;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

public class CustomWorldChunkManager extends BiomeSource {

    private final WorldInfo worldInfo;
    private final BiomeProvider biomeProvider;
    private final Registry<Biome> registry;

    private static List<Holder<Biome>> biomeListToBiomeBaseList(List<org.bukkit.block.Biome> biomes, Registry<Biome> registry) {
        List<Holder<Biome>> biomeBases = new ArrayList<>();

        for (org.bukkit.block.Biome biome : biomes) {
            Preconditions.checkArgument(biome != org.bukkit.block.Biome.CUSTOM, "Cannot use the biome %s", biome);
            biomeBases.add(CraftBiome.bukkitToMinecraftHolder(biome));
        }

        return biomeBases;
    }

    public CustomWorldChunkManager(WorldInfo worldInfo, BiomeProvider biomeProvider, Registry<Biome> registry) {
        this.worldInfo = worldInfo;
        this.biomeProvider = biomeProvider;
        this.registry = registry;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        throw new UnsupportedOperationException("Cannot serialize CustomWorldChunkManager");
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        org.bukkit.block.Biome biome = biomeProvider.getBiome(worldInfo, x << 2, y << 2, z << 2, CraftBiomeParameterPoint.createBiomeParameterPoint(sampler, sampler.sample(x, y, z)));
        Preconditions.checkArgument(biome != org.bukkit.block.Biome.CUSTOM, "Cannot set the biome to %s", biome);

        return CraftBiome.bukkitToMinecraftHolder(biome);
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return biomeListToBiomeBaseList(biomeProvider.getBiomes(worldInfo), registry).stream();
    }
}
