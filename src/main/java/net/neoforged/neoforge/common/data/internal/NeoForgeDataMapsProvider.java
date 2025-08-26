/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.ai.behavior.WorkAtComposter;
import net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.AcceptableVillagerDistance;
import net.neoforged.neoforge.registries.datamaps.builtin.BiomeVillagerType;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.MonsterRoomMob;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.Oxidizable;
import net.neoforged.neoforge.registries.datamaps.builtin.ParrotImitation;
import net.neoforged.neoforge.registries.datamaps.builtin.RaidHeroGift;
import net.neoforged.neoforge.registries.datamaps.builtin.Strippable;
import net.neoforged.neoforge.registries.datamaps.builtin.VibrationFrequency;
import net.neoforged.neoforge.registries.datamaps.builtin.Waxable;

public class NeoForgeDataMapsProvider extends DataMapProvider {
    public NeoForgeDataMapsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        final var biomeVillagers = builder(NeoForgeDataMaps.VILLAGER_TYPES);
        ObfuscationReflectionHelper.<Map<ResourceKey<Biome>, ResourceKey<VillagerType>>, VillagerType>getPrivateValue(VillagerType.class, null, "BY_BIOME")
                .forEach((biome, type) -> biomeVillagers.add(biome, new BiomeVillagerType(type), false));

        final var compostables = builder(NeoForgeDataMaps.COMPOSTABLES);
        final List<Item> villagerCompostables = ObfuscationReflectionHelper.getPrivateValue(WorkAtComposter.class, null, "COMPOSTABLE_ITEMS");
        ComposterBlock.COMPOSTABLES.forEach((item, chance) -> compostables.add(item.asItem().builtInRegistryHolder(), new Compostable(chance, villagerCompostables.contains(item.asItem())), false));

        final var acceptableVillagerDistances = builder(NeoForgeDataMaps.ACCEPTABLE_VILLAGER_DISTANCES);
        ObfuscationReflectionHelper.<ImmutableMap<EntityType<?>, Float>, VillagerHostilesSensor>getPrivateValue(VillagerHostilesSensor.class, null, "ACCEPTABLE_DISTANCE_FROM_HOSTILES")
                .forEach((entityType, distance) -> acceptableVillagerDistances.add(BuiltInRegistries.ENTITY_TYPE.getKey(entityType), new AcceptableVillagerDistance(distance), false));

        final var fuels = builder(NeoForgeDataMaps.FURNACE_FUELS);
        FuelValues.vanillaBurnTimes(new FuelValuesDataMapBuilder(provider, fuels), AbstractFurnaceBlockEntity.BURN_TIME_STANDARD);

        final var vibrationFrequencies = builder(NeoForgeDataMaps.VIBRATION_FREQUENCIES);
        ((Reference2IntMap<ResourceKey<GameEvent>>) VibrationSystem.VIBRATION_FREQUENCY_FOR_EVENT)
                .forEach((event, frequency) -> vibrationFrequencies.add(event, new VibrationFrequency(frequency), false));

        final var imitations = builder(NeoForgeDataMaps.PARROT_IMITATIONS);
        ObfuscationReflectionHelper.<Map<EntityType<?>, SoundEvent>, Parrot>getPrivateValue(Parrot.class, null, "MOB_SOUND_MAP")
                .forEach((type, sound) -> imitations.add(type.builtInRegistryHolder(), new ParrotImitation(sound), false));

        final var raidHeroGifts = builder(NeoForgeDataMaps.RAID_HERO_GIFTS);
        ObfuscationReflectionHelper.<Map<ResourceKey<VillagerProfession>, ResourceKey<LootTable>>, GiveGiftToHero>getPrivateValue(GiveGiftToHero.class, null, "GIFTS")
                .forEach((type, lootTable) -> raidHeroGifts.add(BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(type), new RaidHeroGift(lootTable), false));

        final var strippables = builder(NeoForgeDataMaps.STRIPPABLES);
        StrippablesAccess.getStrippables().forEach((block, stripped) -> strippables.add(block.builtInRegistryHolder(), new Strippable(stripped), false));

        final var monsterRoomMobs = builder(NeoForgeDataMaps.MONSTER_ROOM_MOBS);
        Arrays.stream(ObfuscationReflectionHelper.<EntityType<?>[], MonsterRoomFeature>getPrivateValue(MonsterRoomFeature.class, null, "MOBS"))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .forEach((type, weight) -> monsterRoomMobs.add(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type), new MonsterRoomMob((int) (weight * 100)), false));

        final var oxidizables = builder(NeoForgeDataMaps.OXIDIZABLES);
        WeatheringCopper.NEXT_BY_BLOCK.get().forEach((now, after) -> {
            oxidizables.add(now.builtInRegistryHolder(), new Oxidizable(after), false);
        });

        final var waxables = builder(NeoForgeDataMaps.WAXABLES);
        HoneycombItem.WAXABLES.get().forEach((now, after) -> {
            waxables.add(now.builtInRegistryHolder(), new Waxable(after), false);
        });
    }

    private static class StrippablesAccess extends AxeItem {
        private StrippablesAccess(ToolMaterial material, float attackDamage, float attackSpeed, Properties properties) {
            super(material, attackDamage, attackSpeed, properties);
        }

        public static Map<Block, Block> getStrippables() {
            return STRIPPABLES;
        }
    }

    private static class FuelValuesDataMapBuilder extends FuelValues.Builder {
        private final Builder<FurnaceFuel, Item> builder;

        public FuelValuesDataMapBuilder(HolderLookup.Provider lookupProvider, DataMapProvider.Builder<FurnaceFuel, Item> builder) {
            super(lookupProvider, FeatureFlags.DEFAULT_FLAGS);
            this.builder = builder;
        }

        @Override
        public FuelValuesDataMapBuilder add(TagKey<Item> tagKey, int burnTime) {
            this.builder.add(tagKey, new FurnaceFuel(burnTime), false);
            return this;
        }

        @Override
        public FuelValuesDataMapBuilder add(ItemLike item, int burnTime) {
            this.builder.add(item.asItem().builtInRegistryHolder(), new FurnaceFuel(burnTime), false);
            return this;
        }

        @Override
        public FuelValuesDataMapBuilder remove(TagKey<Item> tagKey) {
            this.builder.remove(tagKey);
            return this;
        }
    }
}
