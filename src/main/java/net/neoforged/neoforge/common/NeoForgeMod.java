/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.DetectedVersion;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attribute.Sentiment;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.CrashReportCallables;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.VersionChecker;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import net.neoforged.neoforge.capabilities.CapabilityHooks;
import net.neoforged.neoforge.common.advancements.critereon.ItemAbilityPredicate;
import net.neoforged.neoforge.common.advancements.critereon.PiglinCurrencyItemPredicate;
import net.neoforged.neoforge.common.advancements.critereon.PiglinNeutralArmorEntityPredicate;
import net.neoforged.neoforge.common.advancements.critereon.SnowBootsEntityPredicate;
import net.neoforged.neoforge.common.conditions.AndCondition;
import net.neoforged.neoforge.common.conditions.FalseCondition;
import net.neoforged.neoforge.common.conditions.FeatureFlagsEnabledCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ItemExistsCondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.OrCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.neoforged.neoforge.common.conditions.TrueCondition;
import net.neoforged.neoforge.common.crafting.BlockTagIngredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.CustomDisplayIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.CanItemPerformAbility;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import net.neoforged.neoforge.common.util.SelfTest;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.common.world.BiomeModifiers.AddFeaturesBiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers.AddSpawnsBiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers.RemoveFeaturesBiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers.RemoveSpawnsBiomeModifier;
import net.neoforged.neoforge.common.world.NoneBiomeModifier;
import net.neoforged.neoforge.common.world.NoneStructureModifier;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.neoforge.common.world.StructureModifiers;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.CauldronFluidContent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.CompoundFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.CustomDisplayFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.DifferenceFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientCodecs;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;
import net.neoforged.neoforge.fluids.crafting.IntersectionFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SimpleFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.display.FluidSlotDisplay;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackSlotDisplay;
import net.neoforged.neoforge.fluids.crafting.display.FluidTagSlotDisplay;
import net.neoforged.neoforge.forge.snapshots.ForgeSnapshotsMod;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.DualStackUtils;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.NeoForgeRegistriesSetup;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.holdersets.AndHolderSet;
import net.neoforged.neoforge.registries.holdersets.AnyHolderSet;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;
import net.neoforged.neoforge.registries.holdersets.NotHolderSet;
import net.neoforged.neoforge.registries.holdersets.OrHolderSet;
import net.neoforged.neoforge.server.command.EnumArgument;
import net.neoforged.neoforge.server.command.ModIdArgument;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
@Mod(NeoForgeVersion.MOD_ID)
public class NeoForgeMod {
    public static final String VERSION_CHECK_CAT = "version_checking";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker NEOFORGEMOD = MarkerManager.getMarker("NEOFORGE-MOD");

    private static boolean isPRBuild;

    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, NeoForgeVersion.MOD_ID);
    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, NeoForgeVersion.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, NeoForgeVersion.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, NeoForgeVersion.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends StructureModifier>> STRUCTURE_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.STRUCTURE_MODIFIER_SERIALIZERS, NeoForgeVersion.MOD_ID);
    private static final DeferredRegister<HolderSetType> HOLDER_SET_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.HOLDER_SET_TYPES, NeoForgeVersion.MOD_ID);

    @SuppressWarnings({ "unchecked", "rawtypes" }) // Uses Holder instead of DeferredHolder as the type due to weirdness between ECJ and javac.
    private static final Holder<ArgumentTypeInfo<?, ?>> ENUM_COMMAND_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register("enum", () -> ArgumentTypeInfos.registerByClass(EnumArgument.class, new EnumArgument.Info()));
    private static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<ModIdArgument>> MODID_COMMAND_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register("modid", () -> ArgumentTypeInfos.registerByClass(ModIdArgument.class,
            SingletonArgumentInfo.contextFree(ModIdArgument::modIdArgument)));

    public static final Holder<Attribute> SWIM_SPEED = ATTRIBUTES.register("swim_speed", () -> new PercentageAttribute("neoforge.swim_speed", 1.0D, 0.0D, 1024.0D).setSyncable(true));
    public static final Holder<Attribute> NAMETAG_DISTANCE = ATTRIBUTES.register("nametag_distance", () -> new RangedAttribute("neoforge.name_tag_distance", 32.0D, 0.0D, 32.0).setSyncable(true).setSentiment(Sentiment.NEUTRAL));

    /**
     * This attribute controls if the player may use creative flight when not in creative mode.
     * <p>
     * This is a {@link BooleanAttribute}, and should only be modified using the standards established by that class.
     * <p>
     * To determine if a player may fly (either via game mode or attribute), use {@link IPlayerExtension#mayFly}
     * <p>
     * Game mode flight cannot be disabled via this attribute.
     */
    public static final Holder<Attribute> CREATIVE_FLIGHT = ATTRIBUTES.register("creative_flight", () -> new BooleanAttribute("neoforge.creative_flight", false).setSyncable(true));

    /**
     * Stock loot modifier type that adds loot from a subtable to the final loot.
     */
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AddTableLootModifier>> ADD_TABLE_LOOT_MODIFIER_TYPE = GLOBAL_LOOT_MODIFIER_SERIALIZERS.register("add_table", () -> AddTableLootModifier.CODEC);

    /**
     * Noop biome modifier. Can be used in a biome modifier json with "type": "neoforge:none".
     */
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<NoneBiomeModifier>> NONE_BIOME_MODIFIER_TYPE = BIOME_MODIFIER_SERIALIZERS.register("none", () -> MapCodec.unit(NoneBiomeModifier.INSTANCE));

    /**
     * Stock biome modifier for adding features to biomes.
     */
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<AddFeaturesBiomeModifier>> ADD_FEATURES_BIOME_MODIFIER_TYPE = BIOME_MODIFIER_SERIALIZERS.register("add_features", () -> RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            Biome.LIST_CODEC.fieldOf("biomes").forGetter(AddFeaturesBiomeModifier::biomes),
                            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(AddFeaturesBiomeModifier::features),
                            Decoration.CODEC.fieldOf("step").forGetter(AddFeaturesBiomeModifier::step))
                    .apply(builder, AddFeaturesBiomeModifier::new)));

    /**
     * Stock biome modifier for removing features from biomes.
     */
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<RemoveFeaturesBiomeModifier>> REMOVE_FEATURES_BIOME_MODIFIER_TYPE = BIOME_MODIFIER_SERIALIZERS.register("remove_features", () -> RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            Biome.LIST_CODEC.fieldOf("biomes").forGetter(RemoveFeaturesBiomeModifier::biomes),
                            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(RemoveFeaturesBiomeModifier::features),
                            Codec.<List<Decoration>, Decoration>either(Decoration.CODEC.listOf(), Decoration.CODEC).<Set<Decoration>>xmap(
                                    either -> either.map(Set::copyOf, Set::of), // convert list/singleton to set when decoding
                                    set -> set.size() == 1 ? Either.right(set.toArray(Decoration[]::new)[0]) : Either.left(List.copyOf(set))).optionalFieldOf("steps", EnumSet.allOf(Decoration.class)).forGetter(RemoveFeaturesBiomeModifier::steps))
                    .apply(builder, RemoveFeaturesBiomeModifier::new)));

    /**
     * Stock biome modifier for adding mob spawns to biomes.
     */
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<AddSpawnsBiomeModifier>> ADD_SPAWNS_BIOME_MODIFIER_TYPE = BIOME_MODIFIER_SERIALIZERS.register("add_spawns", () -> RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            Biome.LIST_CODEC.fieldOf("biomes").forGetter(AddSpawnsBiomeModifier::biomes),
                            // Allow either a list or single spawner, attempting to decode the list format first.
                            // Uses the better EitherCodec that logs both errors if both formats fail to parse.
                            Codec.either(SpawnerData.CODEC.listOf(), SpawnerData.CODEC).xmap(
                                    either -> either.map(Function.identity(), List::of), // convert list/singleton to list when decoding
                                    list -> list.size() == 1 ? Either.right(list.get(0)) : Either.left(list) // convert list to singleton/list when encoding
                            ).fieldOf("spawners").forGetter(AddSpawnsBiomeModifier::spawners))
                    .apply(builder, AddSpawnsBiomeModifier::new)));

    /**
     * Stock biome modifier for removing mob spawns from biomes.
     */
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<RemoveSpawnsBiomeModifier>> REMOVE_SPAWNS_BIOME_MODIFIER_TYPE = BIOME_MODIFIER_SERIALIZERS.register("remove_spawns", () -> RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            Biome.LIST_CODEC.fieldOf("biomes").forGetter(RemoveSpawnsBiomeModifier::biomes),
                            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entity_types").forGetter(RemoveSpawnsBiomeModifier::entityTypes))
                    .apply(builder, RemoveSpawnsBiomeModifier::new)));

    /**
     * Stock biome modifier for adding carvers to biomes.
     */
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<BiomeModifiers.AddCarversBiomeModifier>> ADD_CARVERS_BIOME_MODIFIER_TYPE = BIOME_MODIFIER_SERIALIZERS.register("add_carvers", () -> RecordCodecBuilder.mapCodec(builder -> builder.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(BiomeModifiers.AddCarversBiomeModifier::biomes),
            ConfiguredWorldCarver.LIST_CODEC.fieldOf("carvers").forGetter(BiomeModifiers.AddCarversBiomeModifier::carvers)).apply(builder, BiomeModifiers.AddCarversBiomeModifier::new)));

    /**
     * Stock biome modifier for removing carvers from biomes.
     */
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<BiomeModifiers.RemoveCarversBiomeModifier>> REMOVE_CARVERS_BIOME_MODIFIER_TYPE = BIOME_MODIFIER_SERIALIZERS.register("remove_carvers", () -> RecordCodecBuilder.mapCodec(builder -> builder.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(BiomeModifiers.RemoveCarversBiomeModifier::biomes),
            ConfiguredWorldCarver.LIST_CODEC.fieldOf("carvers").forGetter(BiomeModifiers.RemoveCarversBiomeModifier::carvers))
            .apply(builder, BiomeModifiers.RemoveCarversBiomeModifier::new)));

    /**
     * Stock biome modifier for adding mob spawn costs to biomes.
     */
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<BiomeModifiers.AddSpawnCostsBiomeModifier>> ADD_SPAWN_COSTS_BIOME_MODIFIER_TYPE = BIOME_MODIFIER_SERIALIZERS.register("add_spawn_costs", () -> RecordCodecBuilder.mapCodec(builder -> builder.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(BiomeModifiers.AddSpawnCostsBiomeModifier::biomes),
            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entity_types").forGetter(BiomeModifiers.AddSpawnCostsBiomeModifier::entityTypes),
            MobSpawnSettings.MobSpawnCost.CODEC.fieldOf("spawn_cost").forGetter(BiomeModifiers.AddSpawnCostsBiomeModifier::spawnCost)).apply(builder, BiomeModifiers.AddSpawnCostsBiomeModifier::new)));

    /**
     * Stock biome modifier for removing mob spawn costs from biomes.
     */
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<BiomeModifiers.RemoveSpawnCostsBiomeModifier>> REMOVE_SPAWN_COSTS_BIOME_MODIFIER_TYPE = BIOME_MODIFIER_SERIALIZERS.register("remove_spawn_costs", () -> RecordCodecBuilder.mapCodec(builder -> builder.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(BiomeModifiers.RemoveSpawnCostsBiomeModifier::biomes),
            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entity_types").forGetter(BiomeModifiers.RemoveSpawnCostsBiomeModifier::entityTypes)).apply(builder, BiomeModifiers.RemoveSpawnCostsBiomeModifier::new)));

    /**
     * Noop structure modifier. Can be used in a structure modifier json with "type": "neoforge:none".
     */
    public static final DeferredHolder<MapCodec<? extends StructureModifier>, MapCodec<NoneStructureModifier>> NONE_STRUCTURE_MODIFIER_TYPE = STRUCTURE_MODIFIER_SERIALIZERS.register("none", () -> MapCodec.unit(NoneStructureModifier.INSTANCE));

    /**
     * Stock structure modifier for adding mob spawns to structures.
     */
    public static final DeferredHolder<MapCodec<? extends StructureModifier>, MapCodec<StructureModifiers.AddSpawnsStructureModifier>> ADD_SPAWNS_STRUCTURE_MODIFIER_TYPE = STRUCTURE_MODIFIER_SERIALIZERS.register("add_spawns", () -> RecordCodecBuilder.mapCodec(builder -> builder.group(
            RegistryCodecs.homogeneousList(Registries.STRUCTURE, Structure.DIRECT_CODEC).fieldOf("structures").forGetter(StructureModifiers.AddSpawnsStructureModifier::structures),
            // Allow either a list or single spawner, attempting to decode the list format first.
            // Uses the better EitherCodec that logs both errors if both formats fail to parse.
            Codec.either(SpawnerData.CODEC.listOf(), SpawnerData.CODEC).xmap(
                    either -> either.map(Function.identity(), List::of), // convert list/singleton to list when decoding
                    list -> list.size() == 1 ? Either.right(list.get(0)) : Either.left(list) // convert list to singleton/list when encoding
            ).fieldOf("spawners").forGetter(StructureModifiers.AddSpawnsStructureModifier::spawners)).apply(builder, StructureModifiers.AddSpawnsStructureModifier::new)));

    /**
     * Stock structure modifier for removing mob spawns from structures.
     */
    public static final DeferredHolder<MapCodec<? extends StructureModifier>, MapCodec<StructureModifiers.RemoveSpawnsStructureModifier>> REMOVE_SPAWNS_STRUCTURE_MODIFIER_TYPE = STRUCTURE_MODIFIER_SERIALIZERS.register("remove_spawns", () -> RecordCodecBuilder.mapCodec(builder -> builder.group(
            RegistryCodecs.homogeneousList(Registries.STRUCTURE, Structure.DIRECT_CODEC).fieldOf("structures").forGetter(StructureModifiers.RemoveSpawnsStructureModifier::structures),
            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entity_types").forGetter(StructureModifiers.RemoveSpawnsStructureModifier::entityTypes)).apply(builder, StructureModifiers.RemoveSpawnsStructureModifier::new)));

    /**
     * Stock structure modifier for removing spawn override lists from structures.
     */
    public static final DeferredHolder<MapCodec<? extends StructureModifier>, MapCodec<StructureModifiers.ClearSpawnsStructureModifier>> CLEAR_SPAWNS_STRUCTURE_MODIFIER_TYPE = STRUCTURE_MODIFIER_SERIALIZERS.register("clear_spawns", () -> RecordCodecBuilder.mapCodec(builder -> builder.group(
            RegistryCodecs.homogeneousList(Registries.STRUCTURE, Structure.DIRECT_CODEC).fieldOf("structures").forGetter(StructureModifiers.ClearSpawnsStructureModifier::structures),
            Codec.<List<MobCategory>, MobCategory>either(MobCategory.CODEC.listOf(), MobCategory.CODEC).<Set<MobCategory>>xmap(
                    either -> either.map(Set::copyOf, Set::of), // convert list/singleton to set when decoding
                    set -> set.size() == 1 ? Either.right(set.toArray(MobCategory[]::new)[0]) : Either.left(List.copyOf(set))).optionalFieldOf("categories", EnumSet.allOf(MobCategory.class)).forGetter(StructureModifiers.ClearSpawnsStructureModifier::categories))
            .apply(builder, StructureModifiers.ClearSpawnsStructureModifier::new)));

    /**
     * Stock holder set type that represents any/all values in a registry. Can be used in a holderset object with {@code { "type": "neoforge:any" }}
     */
    public static final Holder<HolderSetType> ANY_HOLDER_SET = HOLDER_SET_TYPES.register("any", AnyHolderSet.Type::new);

    /**
     * Stock holder set type that represents an intersection of other holdersets. Can be used in a holderset object with {@code { "type": "neoforge:and", "values": [list of holdersets] }}
     */
    public static final Holder<HolderSetType> AND_HOLDER_SET = HOLDER_SET_TYPES.register("and", AndHolderSet.Type::new);

    /**
     * Stock holder set type that represents a union of other holdersets. Can be used in a holderset object with {@code { "type": "neoforge:or", "values": [list of holdersets] }}
     */
    public static final Holder<HolderSetType> OR_HOLDER_SET = HOLDER_SET_TYPES.register("or", OrHolderSet.Type::new);

    /**
     * <p>Stock holder set type that represents all values in a registry except those in another given set.
     * Can be used in a holderset object with {@code { "type": "neoforge:not", "value": holderset }}</p>
     */
    public static final Holder<HolderSetType> NOT_HOLDER_SET = HOLDER_SET_TYPES.register("not", NotHolderSet.Type::new);

    private static final DeferredRegister<SlotDisplay.Type<?>> SLOT_DISPLAY_TYPES = DeferredRegister.create(Registries.SLOT_DISPLAY, NeoForgeVersion.MOD_ID);

    public static final DeferredHolder<SlotDisplay.Type<?>, SlotDisplay.Type<FluidSlotDisplay>> FLUID_SLOT_DISPLAY = SLOT_DISPLAY_TYPES.register("fluid", () -> new SlotDisplay.Type<>(FluidSlotDisplay.MAP_CODEC, FluidSlotDisplay.STREAM_CODEC));
    public static final DeferredHolder<SlotDisplay.Type<?>, SlotDisplay.Type<FluidStackSlotDisplay>> FLUID_STACK_SLOT_DISPLAY = SLOT_DISPLAY_TYPES.register("fluid_stack", () -> new SlotDisplay.Type<>(FluidStackSlotDisplay.MAP_CODEC, FluidStackSlotDisplay.STREAM_CODEC));
    public static final DeferredHolder<SlotDisplay.Type<?>, SlotDisplay.Type<FluidTagSlotDisplay>> FLUID_TAG_SLOT_DISPLAY = SLOT_DISPLAY_TYPES.register("fluid_tag", () -> new SlotDisplay.Type<>(FluidTagSlotDisplay.MAP_CODEC, FluidTagSlotDisplay.STREAM_CODEC));

    private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, NeoForgeVersion.MOD_ID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<CompoundIngredient>> COMPOUND_INGREDIENT_TYPE = INGREDIENT_TYPES.register("compound", () -> new IngredientType<>(CompoundIngredient.CODEC));
    public static final DeferredHolder<IngredientType<?>, IngredientType<DataComponentIngredient>> DATA_COMPONENT_INGREDIENT_TYPE = INGREDIENT_TYPES.register("components", () -> new IngredientType<>(DataComponentIngredient.CODEC));
    public static final DeferredHolder<IngredientType<?>, IngredientType<DifferenceIngredient>> DIFFERENCE_INGREDIENT_TYPE = INGREDIENT_TYPES.register("difference", () -> new IngredientType<>(DifferenceIngredient.CODEC));
    public static final DeferredHolder<IngredientType<?>, IngredientType<IntersectionIngredient>> INTERSECTION_INGREDIENT_TYPE = INGREDIENT_TYPES.register("intersection", () -> new IngredientType<>(IntersectionIngredient.CODEC));
    public static final DeferredHolder<IngredientType<?>, IngredientType<BlockTagIngredient>> BLOCK_TAG_INGREDIENT = INGREDIENT_TYPES.register("block_tag", () -> new IngredientType<>(BlockTagIngredient.CODEC));
    public static final DeferredHolder<IngredientType<?>, IngredientType<CustomDisplayIngredient>> CUSTOM_DISPLAY_INGREDIENT = INGREDIENT_TYPES.register("custom_display", () -> new IngredientType<>(CustomDisplayIngredient.CODEC));

    private static final DeferredRegister<FluidIngredientType<?>> FLUID_INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_INGREDIENT_TYPES, NeoForgeVersion.MOD_ID);
    public static final DeferredHolder<FluidIngredientType<?>, FluidIngredientType<SimpleFluidIngredient>> SIMPLE_FLUID_INGREDIENT_TYPE = FLUID_INGREDIENT_TYPES.register("simple", FluidIngredientCodecs::simpleType);
    public static final DeferredHolder<FluidIngredientType<?>, FluidIngredientType<CompoundFluidIngredient>> COMPOUND_FLUID_INGREDIENT_TYPE = FLUID_INGREDIENT_TYPES.register("compound", () -> new FluidIngredientType<>(CompoundFluidIngredient.CODEC));
    public static final DeferredHolder<FluidIngredientType<?>, FluidIngredientType<DataComponentFluidIngredient>> DATA_COMPONENT_FLUID_INGREDIENT_TYPE = FLUID_INGREDIENT_TYPES.register("components", () -> new FluidIngredientType<>(DataComponentFluidIngredient.CODEC));
    public static final DeferredHolder<FluidIngredientType<?>, FluidIngredientType<DifferenceFluidIngredient>> DIFFERENCE_FLUID_INGREDIENT_TYPE = FLUID_INGREDIENT_TYPES.register("difference", () -> new FluidIngredientType<>(DifferenceFluidIngredient.CODEC));
    public static final DeferredHolder<FluidIngredientType<?>, FluidIngredientType<IntersectionFluidIngredient>> INTERSECTION_FLUID_INGREDIENT_TYPE = FLUID_INGREDIENT_TYPES.register("intersection", () -> new FluidIngredientType<>(IntersectionFluidIngredient.CODEC));
    public static final DeferredHolder<FluidIngredientType<?>, FluidIngredientType<CustomDisplayFluidIngredient>> CUSTOM_DISPLAY_FLUID_INGREDIENT = FLUID_INGREDIENT_TYPES.register("custom_display", () -> new FluidIngredientType<>(CustomDisplayFluidIngredient.CODEC, CustomDisplayFluidIngredient.STREAM_CODEC));

    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, NeoForgeVersion.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<AndCondition>> AND_CONDITION = CONDITION_CODECS.register("and", () -> AndCondition.CODEC);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<FalseCondition>> FALSE_CONDITION = CONDITION_CODECS.register("false", () -> FalseCondition.CODEC);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ItemExistsCondition>> ITEM_EXISTS_CONDITION = CONDITION_CODECS.register("item_exists", () -> ItemExistsCondition.CODEC);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ModLoadedCondition>> MOD_LOADED_CONDITION = CONDITION_CODECS.register("mod_loaded", () -> ModLoadedCondition.CODEC);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<NotCondition>> NOT_CONDITION = CONDITION_CODECS.register("not", () -> NotCondition.CODEC);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<OrCondition>> OR_CONDITION = CONDITION_CODECS.register("or", () -> OrCondition.CODEC);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<TagEmptyCondition>> TAG_EMPTY_CONDITION = CONDITION_CODECS.register("tag_empty", () -> TagEmptyCondition.CODEC);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<TrueCondition>> TRUE_CONDITION = CONDITION_CODECS.register("true", () -> TrueCondition.CODEC);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<FeatureFlagsEnabledCondition>> FEATURE_FLAGS_ENABLED_CONDITION = CONDITION_CODECS.register("feature_flags_enabled", () -> FeatureFlagsEnabledCondition.CODEC);

    private static final DeferredRegister<MapCodec<? extends EntitySubPredicate>> ENTITY_PREDICATE_CODECS = DeferredRegister.create(Registries.ENTITY_SUB_PREDICATE_TYPE, NeoForgeVersion.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends EntitySubPredicate>, MapCodec<PiglinNeutralArmorEntityPredicate>> PIGLIN_NEUTRAL_ARMOR_PREDICATE = ENTITY_PREDICATE_CODECS.register("piglin_neutral_armor", () -> PiglinNeutralArmorEntityPredicate.CODEC);
    public static final DeferredHolder<MapCodec<? extends EntitySubPredicate>, MapCodec<SnowBootsEntityPredicate>> SNOW_BOOTS_PREDICATE = ENTITY_PREDICATE_CODECS.register("snow_boots", () -> SnowBootsEntityPredicate.CODEC);

    private static final DeferredRegister<ItemSubPredicate.Type<?>> ITEM_SUB_PREDICATES = DeferredRegister.create(Registries.ITEM_SUB_PREDICATE_TYPE, NeoForgeVersion.MOD_ID);
    public static final DeferredHolder<ItemSubPredicate.Type<?>, ItemSubPredicate.Type<ItemAbilityPredicate>> ITEM_ABILITY_PREDICATE = ITEM_SUB_PREDICATES.register("item_ability", () -> ItemAbilityPredicate.TYPE);
    public static final DeferredHolder<ItemSubPredicate.Type<?>, ItemSubPredicate.Type<PiglinCurrencyItemPredicate>> PIGLIN_CURRENCY_PREDICATE = ITEM_SUB_PREDICATES.register("piglin_currency", () -> PiglinCurrencyItemPredicate.TYPE);

    private static final DeferredRegister<FluidType> VANILLA_FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, "minecraft");

    public static final Holder<FluidType> EMPTY_TYPE = VANILLA_FLUID_TYPES.register("empty", () -> new FluidType(FluidType.Properties.create()
            .descriptionId("block.minecraft.air")
            .motionScale(1D)
            .canPushEntity(false)
            .canSwim(false)
            .canDrown(false)
            .fallDistanceModifier(1F)
            .pathType(null)
            .adjacentPathType(null)
            .density(0)
            .temperature(0)
            .viscosity(0)) {
        @Override
        public void setItemMovement(ItemEntity entity) {
            if (!entity.isNoGravity()) entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }
    });
    public static final Holder<FluidType> WATER_TYPE = VANILLA_FLUID_TYPES.register("water", () -> new FluidType(FluidType.Properties.create()
            .descriptionId("block.minecraft.water")
            .fallDistanceModifier(0F)
            .canExtinguish(true)
            .canConvertToSource(true)
            .supportsBoating(true)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
            .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
            .canHydrate(true)
            .addDripstoneDripping(PointedDripstoneBlock.WATER_TRANSFER_PROBABILITY_PER_RANDOM_TICK, ParticleTypes.DRIPPING_DRIPSTONE_WATER, Blocks.WATER_CAULDRON, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON)) {
        @Override
        public boolean canConvertToSource(FluidState state, LevelReader reader, BlockPos pos) {
            if (reader instanceof ServerLevel level) {
                return level.getGameRules().getBoolean(GameRules.RULE_WATER_SOURCE_CONVERSION);
            }
            //Best guess fallback to default (true)
            return super.canConvertToSource(state, reader, pos);
        }

        @Override
        public @Nullable PathType getBlockPathType(FluidState state, BlockGetter level, BlockPos pos, @Nullable Mob mob, boolean canFluidLog) {
            return canFluidLog ? super.getBlockPathType(state, level, pos, mob, true) : null;
        }
    });
    public static final Holder<FluidType> LAVA_TYPE = VANILLA_FLUID_TYPES.register("lava", () -> new FluidType(FluidType.Properties.create()
            .descriptionId("block.minecraft.lava")
            .canSwim(false)
            .canDrown(false)
            .pathType(PathType.LAVA)
            .adjacentPathType(null)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
            .lightLevel(15)
            .density(3000)
            .viscosity(6000)
            .temperature(1300)
            .addDripstoneDripping(PointedDripstoneBlock.LAVA_TRANSFER_PROBABILITY_PER_RANDOM_TICK, ParticleTypes.DRIPPING_DRIPSTONE_LAVA, Blocks.LAVA_CAULDRON, SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON)) {
        @Override
        public boolean canConvertToSource(FluidState state, LevelReader reader, BlockPos pos) {
            if (reader instanceof ServerLevel level) {
                return level.getGameRules().getBoolean(GameRules.RULE_LAVA_SOURCE_CONVERSION);
            }
            //Best guess fallback to default (false)
            return super.canConvertToSource(state, reader, pos);
        }

        @Override
        public double motionScale(Entity entity) {
            return entity.level().dimensionType().ultraWarm() ? 0.007D : 0.0023333333333333335D;
        }

        @Override
        public void setItemMovement(ItemEntity entity) {
            Vec3 vec3 = entity.getDeltaMovement();
            entity.setDeltaMovement(vec3.x * (double) 0.95F, vec3.y + (double) (vec3.y < (double) 0.06F ? 5.0E-4F : 0.0F), vec3.z * (double) 0.95F);
        }
    });

    private static boolean enableProperFilenameValidation = false;
    private static boolean enableMilkFluid = false;
    private static boolean enableMergedAttributeTooltips = false;

    public static final DeferredHolder<SoundEvent, SoundEvent> BUCKET_EMPTY_MILK = DeferredHolder.create(Registries.SOUND_EVENT, ResourceLocation.withDefaultNamespace("item.bucket.empty_milk"));
    public static final DeferredHolder<SoundEvent, SoundEvent> BUCKET_FILL_MILK = DeferredHolder.create(Registries.SOUND_EVENT, ResourceLocation.withDefaultNamespace("item.bucket.fill_milk"));
    public static final DeferredHolder<FluidType, FluidType> MILK_TYPE = DeferredHolder.create(NeoForgeRegistries.Keys.FLUID_TYPES, ResourceLocation.withDefaultNamespace("milk"));
    public static final DeferredHolder<Fluid, Fluid> MILK = DeferredHolder.create(Registries.FLUID, ResourceLocation.withDefaultNamespace("milk"));
    public static final DeferredHolder<Fluid, Fluid> FLOWING_MILK = DeferredHolder.create(Registries.FLUID, ResourceLocation.withDefaultNamespace("flowing_milk"));

    /**
     * Used in place of {@link DamageSources#magic()} for damage dealt by {@link MobEffects#POISON}.
     * <p>
     * May also be used by mods providing poison-like effects.
     *
     * @see {@link Tags.DamageTypes#IS_POISON}
     */
    public static final ResourceKey<DamageType> POISON_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "poison"));

    /**
     * Run this method during mod constructor to enable milk and add it to the Minecraft milk bucket
     */
    public static void enableMilkFluid() {
        enableMilkFluid = true;
    }

    /**
     * Run this during mod construction to enable merged attribute tooltip functionality.
     */
    public static void enableMergedAttributeTooltips() {
        enableMergedAttributeTooltips = true;
    }

    /**
     * Run this method during mod constructor to enable {@link net.minecraft.FileUtil#RESERVED_WINDOWS_FILENAMES_NEOFORGE} regex being used for filepath validation.
     * Fixes MC-268617 at cost of vanilla incompat edge cases with files generated with this activated and them migrated to vanilla instance - See PR #767
     */
    public static void enableProperFilenameValidation() {
        enableProperFilenameValidation = true;
    }

    public static boolean getProperFilenameValidation() {
        return enableProperFilenameValidation;
    }

    public static boolean shouldMergeAttributeTooltips() {
        return enableMergedAttributeTooltips;
    }

    public NeoForgeMod(IEventBus modEventBus, Dist dist, ModContainer container) {
        LOGGER.info(NEOFORGEMOD, "NeoForge mod loading, version {}, for MC {}", NeoForgeVersion.getVersion(), DetectedVersion.BUILT_IN.getName());
        ForgeSnapshotsMod.logStartupWarning();

        SelfTest.initCommon();

        CrashReportCallables.registerCrashCallable("Crash Report UUID", () -> {
            final UUID uuid = UUID.randomUUID();
            LOGGER.fatal("Preparing crash report with UUID {}", uuid);
            return uuid.toString();
        });

        CrashReportCallables.registerCrashCallable("FML", NeoForgeVersion::getFmlVersion);
        CrashReportCallables.registerCrashCallable("NeoForge", NeoForgeVersion::getVersion);

        // Forge-provided datapack registries
        modEventBus.addListener((DataPackRegistryEvent.NewRegistry event) -> {
            event.dataPackRegistry(NeoForgeRegistries.Keys.BIOME_MODIFIERS, BiomeModifier.DIRECT_CODEC);
            event.dataPackRegistry(NeoForgeRegistries.Keys.STRUCTURE_MODIFIERS, StructureModifier.DIRECT_CODEC);
        });
        modEventBus.addListener(this::preInit);
        modEventBus.addListener(this::loadComplete);
        modEventBus.addListener(this::registerFluids);
        modEventBus.addListener(this::registerLootData);
        ATTRIBUTES.register(modEventBus);
        COMMAND_ARGUMENT_TYPES.register(modEventBus);
        BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
        STRUCTURE_MODIFIER_SERIALIZERS.register(modEventBus);
        HOLDER_SET_TYPES.register(modEventBus);
        VANILLA_FLUID_TYPES.register(modEventBus);
        ENTITY_PREDICATE_CODECS.register(modEventBus);
        ITEM_SUB_PREDICATES.register(modEventBus);
        SLOT_DISPLAY_TYPES.register(modEventBus);
        INGREDIENT_TYPES.register(modEventBus);
        CONDITION_CODECS.register(modEventBus);
        GLOBAL_LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(this::serverStopping);
        container.registerConfig(ModConfig.Type.CLIENT, NeoForgeConfig.clientSpec);
        container.registerConfig(ModConfig.Type.SERVER, NeoForgeConfig.serverSpec);
        container.registerConfig(ModConfig.Type.COMMON, NeoForgeConfig.commonSpec);
        modEventBus.register(NeoForgeConfig.class);
        NeoForgeRegistriesSetup.setup(modEventBus);
        StartupNotificationManager.addModMessage("NeoForge version " + NeoForgeVersion.getVersion());

        NeoForge.EVENT_BUS.addListener(VillagerTradingManager::loadTrades);
        NeoForge.EVENT_BUS.register(new NeoForgeEventHandler());
        NeoForge.EVENT_BUS.addListener(this::registerPermissionNodes);

        UsernameCache.load();
        DualStackUtils.initialise();
        TagConventionLogWarning.init();

        modEventBus.addListener(CapabilityHooks::registerVanillaProviders);
        modEventBus.addListener(EventPriority.LOW, CapabilityHooks::registerFallbackVanillaProviders);
        modEventBus.addListener(CauldronFluidContent::registerCapabilities);
        // These 3 listeners use the default priority for now, can be re-evaluated later.
        NeoForge.EVENT_BUS.addListener(CapabilityHooks::invalidateCapsOnChunkLoad);
        NeoForge.EVENT_BUS.addListener(CapabilityHooks::invalidateCapsOnChunkUnload);
        NeoForge.EVENT_BUS.addListener(CapabilityHooks::cleanCapabilityListenerReferencesOnTick);

        NeoForge.EVENT_BUS.addListener(DataMapHooks::onDataMapsUpdated);

        modEventBus.register(NeoForgeDataMaps.class);

        modEventBus.register(SpawnEggItem.class); // Registers dispenser behaviour for eggs

        if (isPRBuild(container.getModInfo().getVersion().toString())) {
            isPRBuild = true;
            ModLoader.addLoadingIssue(ModLoadingIssue.warning("loadwarning.neoforge.prbuild").withAffectedMod(container.getModInfo()));
        }
    }

    public void preInit(FMLCommonSetupEvent evt) {
        VersionChecker.startVersionCheck();
    }

    public void loadComplete(FMLLoadCompleteEvent event) {}

    public void serverStopping(ServerStoppingEvent evt) {
        WorldWorkerManager.clear();

        // Reset WORLD type config caches
        ModConfigs.getFileMap().values().forEach(config -> {
            if (config.getSpec() instanceof ModConfigSpec spec) {
                spec.resetCaches(ModConfigSpec.RestartType.WORLD);
            }
        });
    }

    // done in an event instead of deferred to only enable if a mod requests it
    public void registerFluids(RegisterEvent event) {
        if (enableMilkFluid) {
            // register milk fill, empty sounds (delegates to water fill, empty sounds)
            event.register(Registries.SOUND_EVENT, helper -> {
                helper.register(BUCKET_EMPTY_MILK.getId(), SoundEvent.createVariableRangeEvent(BUCKET_EMPTY_MILK.getId()));
                helper.register(BUCKET_FILL_MILK.getId(), SoundEvent.createVariableRangeEvent(BUCKET_FILL_MILK.getId()));
            });

            // register fluid type
            event.register(NeoForgeRegistries.Keys.FLUID_TYPES, helper -> helper.register(MILK_TYPE.unwrapKey().orElseThrow(), new FluidType(
                    FluidType.Properties.create().density(1024).viscosity(1024)
                            .sound(SoundActions.BUCKET_FILL, BUCKET_FILL_MILK.value())
                            .sound(SoundActions.BUCKET_EMPTY, BUCKET_EMPTY_MILK.value()))));

            // register fluids
            event.register(Registries.FLUID, helper -> {
                // set up properties
                BaseFlowingFluid.Properties properties = new BaseFlowingFluid.Properties(MILK_TYPE::value, MILK::value, FLOWING_MILK::value).bucket(() -> Items.MILK_BUCKET);

                helper.register(MILK.getId(), new BaseFlowingFluid.Source(properties));
                helper.register(FLOWING_MILK.getId(), new BaseFlowingFluid.Flowing(properties));
            });
        }
    }

    public void registerLootData(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.LOOT_CONDITION_TYPE))
            return;

        event.register(Registries.LOOT_CONDITION_TYPE, ResourceLocation.fromNamespaceAndPath("neoforge", "loot_table_id"), () -> LootTableIdCondition.LOOT_TABLE_ID);
        event.register(Registries.LOOT_CONDITION_TYPE, ResourceLocation.fromNamespaceAndPath("neoforge", "can_item_perform_ability"), () -> CanItemPerformAbility.LOOT_CONDITION_TYPE);
    }

    public static final PermissionNode<Boolean> USE_SELECTORS_PERMISSION = new PermissionNode<>(NeoForgeVersion.MOD_ID, "use_entity_selectors",
            PermissionTypes.BOOLEAN, (player, uuid, contexts) -> player != null && player.hasPermissions(Commands.LEVEL_GAMEMASTERS));

    public void registerPermissionNodes(PermissionGatherEvent.Nodes event) {
        event.addNodes(USE_SELECTORS_PERMISSION);
    }

    private static boolean isPRBuild(String neoVersion) {
        // The -pr- being inside the actual version and a branch name is important.
        // Since we checkout PRs on a branch named `pr-<number>-<headname>`, this assures that
        // the regex will match PR builds published to Packages, but that it will not match local PR branches
        // since those usually have the name `pr|pull/<number>`
        return neoVersion.matches("\\d+\\.\\d+\\.\\d+(-beta)?-pr-\\d+-[\\w-]+");
    }

    public static boolean isPRBuild() {
        return isPRBuild;
    }
}
