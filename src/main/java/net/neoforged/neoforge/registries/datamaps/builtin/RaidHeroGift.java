package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

/**
 * Data map value for {@link NeoForgeDataMaps#PARROT_IMITATIONS parrot imitations}.
 *
 * @param lootTable the loot table that the villager will hand out after a raid
 */
public record RaidHeroGift(ResourceLocation lootTable) {
    public static final Codec<RaidHeroGift> LOOT_TABLE_CODEC = ResourceLocation.CODEC
            .xmap(RaidHeroGift::new, RaidHeroGift::lootTable);

    public static final Codec<RaidHeroGift> CODEC = ExtraCodecs.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    ResourceLocation.CODEC.fieldOf("loot_table").forGetter(RaidHeroGift::lootTable))
                    .apply(in, RaidHeroGift::new)), LOOT_TABLE_CODEC);
}
