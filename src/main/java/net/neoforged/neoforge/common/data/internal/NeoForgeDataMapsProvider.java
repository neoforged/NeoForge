/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.random.Weight;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.ai.behavior.WorkAtComposter;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.MonsterRoomMob;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.Oxidizable;
import net.neoforged.neoforge.registries.datamaps.builtin.ParrotImitation;
import net.neoforged.neoforge.registries.datamaps.builtin.RaidHeroGift;
import net.neoforged.neoforge.registries.datamaps.builtin.VibrationFrequency;
import net.neoforged.neoforge.registries.datamaps.builtin.Waxable;

public class NeoForgeDataMapsProvider extends DataMapProvider {
    public NeoForgeDataMapsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather() {
        final var compostables = builder(NeoForgeDataMaps.COMPOSTABLES);
        final List<Item> villagerCompostables = ObfuscationReflectionHelper.getPrivateValue(WorkAtComposter.class, null, "COMPOSTABLE_ITEMS");
        ComposterBlock.COMPOSTABLES.forEach((item, chance) -> compostables.add(item.asItem().builtInRegistryHolder(), new Compostable(chance, villagerCompostables.contains(item.asItem())), false));

        final var fuels = builder(NeoForgeDataMaps.FURNACE_FUELS);
        AbstractFurnaceBlockEntity.buildFuels((value, time) -> value.ifLeft(item -> fuels.add(item.builtInRegistryHolder(), new FurnaceFuel(time), false))
                .ifRight(tag -> fuels.add(tag, new FurnaceFuel(time), false)));
        // Mojang decided to use an exclusion tag for nether wood
        fuels.remove(ItemTags.NON_FLAMMABLE_WOOD);

        final var vibrationFrequencies = builder(NeoForgeDataMaps.VIBRATION_FREQUENCIES);
        ((Reference2IntMap<ResourceKey<GameEvent>>) VibrationSystem.VIBRATION_FREQUENCY_FOR_EVENT)
                .forEach((event, frequency) -> vibrationFrequencies.add(event, new VibrationFrequency(frequency), false));

        final var imitations = builder(NeoForgeDataMaps.PARROT_IMITATIONS);
        ObfuscationReflectionHelper.<Map<EntityType<?>, SoundEvent>, Parrot>getPrivateValue(Parrot.class, null, "MOB_SOUND_MAP")
                .forEach((type, sound) -> imitations.add(type.builtInRegistryHolder(), new ParrotImitation(sound), false));

        final var raidHeroGifts = builder(NeoForgeDataMaps.RAID_HERO_GIFTS);
        ObfuscationReflectionHelper.<Map<VillagerProfession, ResourceKey<LootTable>>, GiveGiftToHero>getPrivateValue(GiveGiftToHero.class, null, "GIFTS")
                .forEach((type, lootTable) -> raidHeroGifts.add(BuiltInRegistries.VILLAGER_PROFESSION.wrapAsHolder(type), new RaidHeroGift(lootTable), false));

        final var monsterRoomMobs = builder(NeoForgeDataMaps.MONSTER_ROOM_MOBS);
        Arrays.stream(ObfuscationReflectionHelper.<EntityType<?>[], MonsterRoomFeature>getPrivateValue(MonsterRoomFeature.class, null, "MOBS"))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .forEach((type, weight) -> monsterRoomMobs.add(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type), new MonsterRoomMob(Weight.of((int) (weight * 100))), false));

        final var oxidizables = builder(NeoForgeDataMaps.OXIDIZING_BLOCKS);
        WeatheringCopper.NEXT_BY_BLOCK.get().forEach((now, after) -> {
            Block before = WeatheringCopper.PREVIOUS_BY_BLOCK.get().get(now);
            oxidizables.add(now.builtInRegistryHolder(), new Oxidizable(Optional.ofNullable(before), after), false);
        });

        final var waxables = builder(NeoForgeDataMaps.WAXABLE_BLOCKS);
        HoneycombItem.WAXABLES.get().forEach((now, after) -> {
            waxables.add(now.builtInRegistryHolder(), new Waxable(after), false);
        });
    }
}
