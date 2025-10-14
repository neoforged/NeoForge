/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.DataMapHooks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.neoforged.neoforge.event.level.BlockEvent.BlockToolModificationEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

/**
 * Holds all {@link DataMapType data maps} provided by NeoForge.
 * <p>
 * These data maps are usually replacements for vanilla in-code maps, and are optionally
 * synced so that mods can use them on the client side.
 */
public class NeoForgeDataMaps {
    /**
     * The {@linkplain EntityType} data map that replaces {@link VillagerHostilesSensor#ACCEPTABLE_DISTANCE_FROM_HOSTILES}.
     * <p>
     * The location of this data map is {@code neoforge/data_maps/entity_type/acceptable_villager_distances.json}, and the values are objects with 1 field:
     * <ul>
     * <li>{@code distance}, a float - the acceptable distance between the hostile mob and a villager</li>
     * </ul>
     *
     * The use of a float as the value is also possible, though discouraged in case more options are added in the future.
     */
    public static final DataMapType<EntityType<?>, AcceptableVillagerDistance> ACCEPTABLE_VILLAGER_DISTANCES = DataMapType.builder(id("acceptable_villager_distances"), Registries.ENTITY_TYPE, AcceptableVillagerDistance.CODEC)
            .synced(AcceptableVillagerDistance.DISTANCE_CODEC, false).build();
    /**
     * The {@linkplain Item} data map that replaces {@link ComposterBlock#COMPOSTABLES}.
     * <p>
     * The location of this data map is {@code neoforge/data_maps/item/compostables.json}, and the values are objects with 1 field:
     * <ul>
     * <li>{@code chance}, a float between 0 and 1 (inclusive) - the chance that the item will add levels to the composter when composted</li>
     * </ul>
     *
     * The use of a float as the value is also possible, though discouraged in case more options are added in the future.
     */
    public static final DataMapType<Item, Compostable> COMPOSTABLES = DataMapType.builder(
            id("compostables"), Registries.ITEM, Compostable.CODEC).synced(Compostable.CHANCE_CODEC, false).build();

    /**
     * The {@linkplain Item} data map that replaces {@link AbstractFurnaceBlockEntity#getFuel()}.
     * <p>
     * The location of this data map is {@code neoforge/data_maps/item/furnace_fuels.json}, and the values are objects with 1 field:
     * <ul>
     * <li>{@code burn_time}, a positive integer - how long the item will burn, in ticks</li>
     * </ul>
     *
     * The use of a integer as the value is also possible, though discouraged in case more options are added in the future.
     *
     * @apiNote Use {@link net.neoforged.neoforge.common.extensions.IItemExtension#getBurnTime(ItemStack, RecipeType)} for NBT-sensitive burn times. That method takes precedence over the data map.
     * @implNote This data map will be empty when connected to a Vanilla server.
     */
    public static final DataMapType<Item, FurnaceFuel> FURNACE_FUELS = DataMapType.builder(
            id("furnace_fuels"), Registries.ITEM, FurnaceFuel.CODEC).synced(FurnaceFuel.BURN_TIME_CODEC, false).build();

    /**
     * The {@linkplain EntityType} data map that replaces {@link MonsterRoomFeature#MOBS}.
     * <p>
     * The location of this data map is {@code neoforge/data_maps/entity_type/monster_room_mobs.json}, and the values are objects with 1 field:
     * <ul>
     * <li>{@code weight}, a positive nonzero integer - the weight of this type that will be used when selecting a type for the spawner.</li>
     * </ul>
     *
     * The use of an integer as the value is also possible, though discouraged in case more options are added in the future.
     */
    public static final DataMapType<EntityType<?>, MonsterRoomMob> MONSTER_ROOM_MOBS = DataMapType.builder(
            id("monster_room_mobs"), Registries.ENTITY_TYPE, MonsterRoomMob.CODEC).synced(MonsterRoomMob.WEIGHT_CODEC, false).build();

    /**
     * The {@linkplain Block} data map that replaces {@link WeatheringCopper#NEXT_BY_BLOCK}.
     * <p>
     * The location of this data map is {@code neoforge/data_maps/block/oxidizables.json}, and the values are objects with 1 field:
     * <ul>
     * <li>{@code next_oxidized_stage}, a block that the object should convert into once it changes oxidizing states</li>
     * </ul>
     *
     * The inverted map of this can be found at {@link DataMapHooks#getInverseOxidizablesMap()}
     */
    public static final DataMapType<Block, Oxidizable> OXIDIZABLES = DataMapType.builder(
            id("oxidizables"), Registries.BLOCK, Oxidizable.CODEC).synced(Oxidizable.OXIDIZABLE_CODEC, false).build();

    /**
     * The {@linkplain EntityType} data map that replaces {@link Parrot#MOB_SOUND_MAP}.
     * <p>
     * The location of this data map is {@code neoforge/data_maps/entity_type/parrot_imitations.json}, and the values are objects with 1 field:
     * <ul>
     * <li>{@code sound}, sound event ID - the sound that the parrot will emit when imitating the mob</li>
     * </ul>
     *
     * The use of a string as the value is also possible, though discouraged in case more options are added in the future.
     */
    public static final DataMapType<EntityType<?>, ParrotImitation> PARROT_IMITATIONS = DataMapType.builder(
            id("parrot_imitations"), Registries.ENTITY_TYPE, ParrotImitation.CODEC).synced(ParrotImitation.SOUND_CODEC, false).build();

    /**
     * The {@linkplain VillagerProfession} data map that replaces {@link GiveGiftToHero#GIFTS}.
     * <p>
     * The location of this data map is {@code neoforge/data_maps/villager_profession/raid_hero_gifts.json}, and the values are objects with 1 field:
     * <ul>
     * <li>{@code loot_table}, the path to the loot table that the villager gives to the player after a raid finishes</li>
     * </ul>
     */
    public static final DataMapType<VillagerProfession, RaidHeroGift> RAID_HERO_GIFTS = DataMapType.builder(
            id("raid_hero_gifts"), Registries.VILLAGER_PROFESSION, RaidHeroGift.CODEC).synced(RaidHeroGift.LOOT_TABLE_CODEC, false).build();

    /**
     * The {@linkplain Block} data map that replaces {@link AxeItem#STRIPPABLES}.
     * <p>
     * The location of this data map is {@code neoforge/data_maps/block/strippables.json}, and the values are objects with 1 field:
     * <ul>
     * <li>{@code stripped_block} - the stripped equivalent of the block after being right-clicked on by an axe.</li>
     * </ul>
     *
     * Note that, upon stripping, all common properties will be copied from the unstripped state to the stripped state.
     * If you want more advanced behavior, see {@link IBlockExtension#getToolModifiedState(BlockState, UseOnContext, ItemAbility, boolean)} and {@link BlockToolModificationEvent}.
     */
    public static final DataMapType<Block, Strippable> STRIPPABLES = DataMapType.builder(
            id("strippables"), Registries.BLOCK, Strippable.CODEC).synced(Strippable.STRIPPED_BLOCK_CODEC, false).build();

    /**
     * The {@linkplain GameEvent} data map that replaces {@link VibrationSystem#VIBRATION_FREQUENCY_FOR_EVENT}.
     * <p>
     * The location of this data map is {@code neoforge/data_maps/game_event/vibration_frequencies.json}, and the values are objects with 1 field:
     * <ul>
     * <li>{@code frequency}, an integer between {@code 1} and {@code 15} (inclusive) - the vibration frequency of the game event</li>
     * </ul>
     *
     * The use of an integer as the value is also possible, though discouraged in case more options are added in the future.
     */
    public static final DataMapType<GameEvent, VibrationFrequency> VIBRATION_FREQUENCIES = DataMapType.builder(
            id("vibration_frequencies"), Registries.GAME_EVENT, VibrationFrequency.CODEC).synced(VibrationFrequency.FREQUENCY_CODEC, false).build();

    /**
     * The {@linkplain Biome} data map that replaces {@link VillagerType#BY_BIOME}.
     * <p>
     * The location of this data map is {@code neoforge/data_maps/worldgen/biome/villager_types.json}, and the values are objects with 1 field:
     * <ul>
     * <li>{@code villager_type}, villager type ID - the type of the villagers present in the biome</li>
     * </ul>
     *
     * The use of a string as the value is also possible, though discouraged in case more options are added in the future.
     */
    public static final DataMapType<Biome, BiomeVillagerType> VILLAGER_TYPES = DataMapType.builder(
            id("villager_types"), Registries.BIOME, BiomeVillagerType.CODEC).synced(BiomeVillagerType.TYPE_CODEC, false).build();

    /**
     * The {@linkplain Block} data map that replaces {@link HoneycombItem#WAXABLES}.
     * <p>
     * The location of this data map is {@code neoforge/data_maps/block/waxables.json}, and the values are objects with 1 field:
     * <ul>
     * <li>{@code waxed}, a block that the object should convert into once it is right clicked with a {@link ItemAbilities#AXE_WAX_OFF} ability</li>
     * </ul>
     *
     * The inverted map of this can be found at {@link DataMapHooks#INVERSE_WAXABLES_DATAMAP}
     */
    public static final DataMapType<Block, Waxable> WAXABLES = DataMapType.builder(
            id("waxables"), Registries.BLOCK, Waxable.CODEC).synced(Waxable.WAXABLE_CODEC, false).build();

    private static ResourceLocation id(final String name) {
        return ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, name);
    }

    @SubscribeEvent
    private static void register(final RegisterDataMapTypesEvent event) {
        event.register(ACCEPTABLE_VILLAGER_DISTANCES);
        event.register(COMPOSTABLES);
        event.register(FURNACE_FUELS);
        event.register(MONSTER_ROOM_MOBS);
        event.register(OXIDIZABLES);
        event.register(PARROT_IMITATIONS);
        event.register(RAID_HERO_GIFTS);
        event.register(STRIPPABLES);
        event.register(VIBRATION_FREQUENCIES);
        event.register(VILLAGER_TYPES);
        event.register(WAXABLES);
    }
}
