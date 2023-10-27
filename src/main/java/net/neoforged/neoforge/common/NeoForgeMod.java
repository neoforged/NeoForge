/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import net.minecraft.DetectedVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.ClientCommandHandler;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.conditions.AndCondition;
import net.neoforged.neoforge.common.conditions.FalseCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ItemExistsCondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.OrCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.neoforged.neoforge.common.conditions.TrueCondition;
import net.neoforged.neoforge.common.crafting.*;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.internal.NeoForgeBiomeTagsProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeFluidTagsProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeSpriteSourceProvider;
import net.neoforged.neoforge.common.data.internal.VanillaSoundDefinitionsProvider;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;
import net.neoforged.neoforge.common.loot.CanToolPerformAction;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers.AddFeaturesBiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers.AddSpawnsBiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers.RemoveFeaturesBiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers.RemoveSpawnsBiomeModifier;
import net.neoforged.neoforge.common.world.NoneBiomeModifier;
import net.neoforged.neoforge.common.world.NoneStructureModifier;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.fml.*;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.*;
import net.neoforged.neoforge.forge.snapshots.ForgeSnapshotsMod;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.internal.versions.neoform.NeoFormVersion;
import net.neoforged.neoforge.network.DualStackUtils;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.registries.holdersets.AndHolderSet;
import net.neoforged.neoforge.registries.holdersets.AnyHolderSet;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;
import net.neoforged.neoforge.registries.holdersets.NotHolderSet;
import net.neoforged.neoforge.registries.holdersets.OrHolderSet;
import net.neoforged.neoforge.network.NetworkConstants;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.network.filters.VanillaPacketSplitter;
import net.neoforged.neoforge.server.command.EnumArgument;
import net.neoforged.neoforge.server.command.ModIdArgument;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.internal.NeoForgeBlockTagsProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeEntityTypeTagsProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeItemTagsProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeLootTableProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeRecipeProvider;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mod(NeoForgeVersion.MOD_ID)
public class NeoForgeMod
{
    public static final String VERSION_CHECK_CAT = "version_checking";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker NEOFORGEMOD = MarkerManager.getMarker("NEOFORGE-MOD");

    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.Keys.ATTRIBUTES, "neoforge");
    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, "neoforge");
    private static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, "neoforge");
    private static final DeferredRegister<Codec<? extends StructureModifier>> STRUCTURE_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.STRUCTURE_MODIFIER_SERIALIZERS, "neoforge");
    private static final DeferredRegister<HolderSetType> HOLDER_SET_TYPES = DeferredRegister.create(ForgeRegistries.Keys.HOLDER_SET_TYPES, "neoforge");

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final RegistryObject<EnumArgument.Info> ENUM_COMMAND_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register("enum", () ->
            ArgumentTypeInfos.registerByClass(EnumArgument.class, new EnumArgument.Info()));
    private static final RegistryObject<SingletonArgumentInfo<ModIdArgument>> MODID_COMMAND_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register("modid", () ->
            ArgumentTypeInfos.registerByClass(ModIdArgument.class,
                    SingletonArgumentInfo.contextFree(ModIdArgument::modIdArgument)));

    public static final RegistryObject<Attribute> SWIM_SPEED = ATTRIBUTES.register("swim_speed", () -> new RangedAttribute("neoforge.swim_speed", 1.0D, 0.0D, 1024.0D).setSyncable(true));
    public static final RegistryObject<Attribute> NAMETAG_DISTANCE = ATTRIBUTES.register("nametag_distance", () -> new RangedAttribute("neoforge.name_tag_distance", 64.0D, 0.0D, 64.0).setSyncable(true));
    public static final RegistryObject<Attribute> ENTITY_GRAVITY = ATTRIBUTES.register("entity_gravity", () -> new RangedAttribute("neoforge.entity_gravity", 0.08D, -8.0D, 8.0D).setSyncable(true));

    /**
     * Reach Distance represents the distance at which a player may interact with the world.  The default is 4.5 blocks.  Players in creative mode have an additional 0.5 blocks of block reach.
     * @see IPlayerExtension#getBlockReach()
     * @see IPlayerExtension#canReach(BlockPos, double)
     */
    public static final RegistryObject<Attribute> BLOCK_REACH = ATTRIBUTES.register("block_reach", () -> new RangedAttribute("neoforge.block_reach", 4.5D, 0.0D, 1024.0D).setSyncable(true));

    /**
     * Attack Range represents the distance at which a player may attack an entity.  The default is 3 blocks.  Players in creative mode have an additional 3 blocks of entity reach.
     * The default of 3.0 is technically considered a bug by Mojang - see MC-172289 and MC-92484. However, updating this value would allow for longer-range attacks on vanilla servers, which makes some people mad.
     * @see IPlayerExtension#getEntityReach()
     * @see IPlayerExtension#canReach(Entity, double)
     * @see IPlayerExtension#canReach(Vec3, double)
     */
    public static final RegistryObject<Attribute> ENTITY_REACH = ATTRIBUTES.register("entity_reach", () -> new RangedAttribute("neoforge.entity_reach", 3.0D, 0.0D, 1024.0D).setSyncable(true));

    /**
     * Step Height Addition modifies the amount of blocks an entity may walk up without jumping.
     * @see IEntityExtension#getStepHeight()
     */
    public static final RegistryObject<Attribute> STEP_HEIGHT = ATTRIBUTES.register("step_height", () -> new RangedAttribute("neoforge.step_height", 0.0D, -512.0D, 512.0D).setSyncable(true));

    /**
     * Noop biome modifier. Can be used in a biome modifier json with "type": "neoforge:none".
     */
    public static final RegistryObject<Codec<NoneBiomeModifier>> NONE_BIOME_MODIFIER_TYPE = BIOME_MODIFIER_SERIALIZERS.register("none", () -> Codec.unit(NoneBiomeModifier.INSTANCE));

    /**
     * Stock biome modifier for adding features to biomes.
     */
    public static final RegistryObject<Codec<AddFeaturesBiomeModifier>> ADD_FEATURES_BIOME_MODIFIER_TYPE = BIOME_MODIFIER_SERIALIZERS.register("add_features", () ->
        RecordCodecBuilder.create(builder -> builder.group(
                Biome.LIST_CODEC.fieldOf("biomes").forGetter(AddFeaturesBiomeModifier::biomes),
                PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(AddFeaturesBiomeModifier::features),
                Decoration.CODEC.fieldOf("step").forGetter(AddFeaturesBiomeModifier::step)
            ).apply(builder, AddFeaturesBiomeModifier::new))
        );

    /**
     * Stock biome modifier for removing features from biomes.
     */
    public static final RegistryObject<Codec<RemoveFeaturesBiomeModifier>> REMOVE_FEATURES_BIOME_MODIFIER_TYPE = BIOME_MODIFIER_SERIALIZERS.register("remove_features", () ->
        RecordCodecBuilder.create(builder -> builder.group(
                Biome.LIST_CODEC.fieldOf("biomes").forGetter(RemoveFeaturesBiomeModifier::biomes),
                PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(RemoveFeaturesBiomeModifier::features),
                new ExtraCodecs.EitherCodec<List<Decoration>, Decoration>(Decoration.CODEC.listOf(), Decoration.CODEC).<Set<Decoration>>xmap(
                        either -> either.map(Set::copyOf, Set::of), // convert list/singleton to set when decoding
                        set -> set.size() == 1 ? Either.right(set.toArray(Decoration[]::new)[0]) : Either.left(List.copyOf(set))
                    ).optionalFieldOf("steps", EnumSet.allOf(Decoration.class)).forGetter(RemoveFeaturesBiomeModifier::steps)
            ).apply(builder, RemoveFeaturesBiomeModifier::new))
        );

    /**
     * Stock biome modifier for adding mob spawns to biomes.
     */
    public static final RegistryObject<Codec<AddSpawnsBiomeModifier>> ADD_SPAWNS_BIOME_MODIFIER_TYPE = BIOME_MODIFIER_SERIALIZERS.register("add_spawns", () ->
        RecordCodecBuilder.create(builder -> builder.group(
                Biome.LIST_CODEC.fieldOf("biomes").forGetter(AddSpawnsBiomeModifier::biomes),
                // Allow either a list or single spawner, attempting to decode the list format first.
                // Uses the better EitherCodec that logs both errors if both formats fail to parse.
                new ExtraCodecs.EitherCodec<>(SpawnerData.CODEC.listOf(), SpawnerData.CODEC).xmap(
                        either -> either.map(Function.identity(), List::of), // convert list/singleton to list when decoding
                        list -> list.size() == 1 ? Either.right(list.get(0)) : Either.left(list) // convert list to singleton/list when encoding
                    ).fieldOf("spawners").forGetter(AddSpawnsBiomeModifier::spawners)
            ).apply(builder, AddSpawnsBiomeModifier::new))
        );

    /**
     * Stock biome modifier for removing mob spawns from biomes.
     */
    public static final RegistryObject<Codec<RemoveSpawnsBiomeModifier>> REMOVE_SPAWNS_BIOME_MODIFIER_TYPE = BIOME_MODIFIER_SERIALIZERS.register("remove_spawns", () ->
        RecordCodecBuilder.create(builder -> builder.group(
                Biome.LIST_CODEC.fieldOf("biomes").forGetter(RemoveSpawnsBiomeModifier::biomes),
                RegistryCodecs.homogeneousList(ForgeRegistries.Keys.ENTITY_TYPES).fieldOf("entity_types").forGetter(RemoveSpawnsBiomeModifier::entityTypes)
            ).apply(builder, RemoveSpawnsBiomeModifier::new))
        );
    /**
     * Noop structure modifier. Can be used in a structure modifier json with "type": "neoforge:none".
     */
    public static final RegistryObject<Codec<NoneStructureModifier>> NONE_STRUCTURE_MODIFIER_TYPE = STRUCTURE_MODIFIER_SERIALIZERS.register("none", () -> Codec.unit(NoneStructureModifier.INSTANCE));

    /**
     * Stock holder set type that represents any/all values in a registry. Can be used in a holderset object with {@code { "type": "neoforge:any" }}
     */
    public static final RegistryObject<HolderSetType> ANY_HOLDER_SET = HOLDER_SET_TYPES.register("any", () -> AnyHolderSet::codec);

    /**
     * Stock holder set type that represents an intersection of other holdersets. Can be used in a holderset object with {@code { "type": "neoforge:and", "values": [list of holdersets] }}
     */
    public static final RegistryObject<HolderSetType> AND_HOLDER_SET = HOLDER_SET_TYPES.register("and", () -> AndHolderSet::codec);

    /**
     * Stock holder set type that represents a union of other holdersets. Can be used in a holderset object with {@code { "type": "neoforge:or", "values": [list of holdersets] }}
     */
    public static final RegistryObject<HolderSetType> OR_HOLDER_SET = HOLDER_SET_TYPES.register("or", () -> OrHolderSet::codec);

    /**
     * <p>Stock holder set type that represents all values in a registry except those in another given set.
     * Can be used in a holderset object with {@code { "type": "neoforge:not", "value": holderset }}</p>
     */
    public static final RegistryObject<HolderSetType> NOT_HOLDER_SET = HOLDER_SET_TYPES.register("not", () -> NotHolderSet::codec);
    
    private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(ForgeRegistries.Keys.INGREDIENT_TYPES, "neoforge");
    
    public static final RegistryObject<IngredientType<CompoundIngredient>> COMPOUND_INGREDIENT_TYPE = INGREDIENT_TYPES.register("compound", () -> new IngredientType<>(CompoundIngredient.CODEC, CompoundIngredient.CODEC_NONEMPTY));
    public static final RegistryObject<IngredientType<StrictNBTIngredient>> STRICT_NBT_INGREDIENT_TYPE = INGREDIENT_TYPES.register("nbt", () -> new IngredientType<>(StrictNBTIngredient.CODEC));
    public static final RegistryObject<IngredientType<PartialNBTIngredient>> PARTIAL_NBT_INGREDIENT_TYPE = INGREDIENT_TYPES.register("partial_nbt", () -> new IngredientType<>(PartialNBTIngredient.CODEC, PartialNBTIngredient.CODEC_NONEMPTY));
    public static final RegistryObject<IngredientType<DifferenceIngredient>> DIFFERENCE_INGREDIENT_TYPE = INGREDIENT_TYPES.register("difference", () -> new IngredientType<>(DifferenceIngredient.CODEC, DifferenceIngredient.CODEC_NONEMPTY));
    public static final RegistryObject<IngredientType<IntersectionIngredient>> INTERSECTION_INGREDIENT_TYPE = INGREDIENT_TYPES.register("intersection", () -> new IngredientType<>(IntersectionIngredient.CODEC, IntersectionIngredient.CODEC_NONEMPTY));
    
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "neoforge");
    public static final RegistryObject<RecipeSerializer<?>> CONDITIONAL_RECIPE = RECIPE_SERIALIZERS.register("conditional", ConditionalRecipe::new);

    private static final DeferredRegister<Codec<? extends ICondition>> CONDITION_CODECS = DeferredRegister.create(ForgeRegistries.Keys.CONDITION_CODECS, "neoforge");
    public static final RegistryObject<Codec<AndCondition>> AND_CONDITION = CONDITION_CODECS.register("and", () -> AndCondition.CODEC);
    public static final RegistryObject<Codec<FalseCondition>> FALSE_CONDITION = CONDITION_CODECS.register("false", () -> FalseCondition.CODEC);
    public static final RegistryObject<Codec<ItemExistsCondition>> ITEM_EXISTS_CONDITION = CONDITION_CODECS.register("item_exists", () -> ItemExistsCondition.CODEC);
    public static final RegistryObject<Codec<ModLoadedCondition>> MOD_LOADED_CONDITION = CONDITION_CODECS.register("mod_loaded", () -> ModLoadedCondition.CODEC);
    public static final RegistryObject<Codec<NotCondition>> NOT_CONDITION = CONDITION_CODECS.register("not", () -> NotCondition.CODEC);
    public static final RegistryObject<Codec<OrCondition>> OR_CONDITION = CONDITION_CODECS.register("or", () -> OrCondition.CODEC);
    public static final RegistryObject<Codec<TagEmptyCondition>> TAG_EMPTY_CONDITION = CONDITION_CODECS.register("tag_empty", () -> TagEmptyCondition.CODEC);
    public static final RegistryObject<Codec<TrueCondition>> TRUE_CONDITION = CONDITION_CODECS.register("true", () -> TrueCondition.CODEC);
    private static final DeferredRegister<IngredientType<?>> VANILLA_INGREDIENT_TYPES = DeferredRegister.create(ForgeRegistries.Keys.INGREDIENT_TYPES, "minecraft");
    
    public static final RegistryObject<IngredientType<Ingredient>> VANILLA_INGREDIENT_TYPE = VANILLA_INGREDIENT_TYPES.register("item", () -> new IngredientType<>(Ingredient.VANILLA_CODEC, Ingredient.VANILLA_CODEC_NONEMPTY));

    private static final DeferredRegister<FluidType> VANILLA_FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, "minecraft");

    public static final RegistryObject<FluidType> EMPTY_TYPE = VANILLA_FLUID_TYPES.register("empty", () ->
            new FluidType(FluidType.Properties.create()
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
                    .viscosity(0))
            {
                @Override
                public void setItemMovement(ItemEntity entity)
                {
                    if (!entity.isNoGravity()) entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
                }
            });
    public static final RegistryObject<FluidType> WATER_TYPE = VANILLA_FLUID_TYPES.register("water", () ->
            new FluidType(FluidType.Properties.create()
                    .descriptionId("block.minecraft.water")
                    .fallDistanceModifier(0F)
                    .canExtinguish(true)
                    .canConvertToSource(true)
                    .supportsBoating(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
                    .canHydrate(true))
            {
                @Override
                public @Nullable BlockPathTypes getBlockPathType(FluidState state, BlockGetter level, BlockPos pos, @Nullable Mob mob, boolean canFluidLog)
                {
                    return canFluidLog ? super.getBlockPathType(state, level, pos, mob, true) : null;
                }

                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
                {
                    consumer.accept(new IClientFluidTypeExtensions()
                    {
                        private static final ResourceLocation UNDERWATER_LOCATION = new ResourceLocation("textures/misc/underwater.png"),
                                WATER_STILL = new ResourceLocation("block/water_still"),
                                WATER_FLOW = new ResourceLocation("block/water_flow"),
                                WATER_OVERLAY = new ResourceLocation("block/water_overlay");

                        @Override
                        public ResourceLocation getStillTexture()
                        {
                            return WATER_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture()
                        {
                            return WATER_FLOW;
                        }

                        @Nullable
                        @Override
                        public ResourceLocation getOverlayTexture()
                        {
                            return WATER_OVERLAY;
                        }

                        @Override
                        public ResourceLocation getRenderOverlayTexture(Minecraft mc)
                        {
                            return UNDERWATER_LOCATION;
                        }

                        @Override
                        public int getTintColor()
                        {
                            return 0xFF3F76E4;
                        }

                        @Override
                        public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos)
                        {
                            return BiomeColors.getAverageWaterColor(getter, pos) | 0xFF000000;
                        }
                    });
                }
            });
    public static final RegistryObject<FluidType> LAVA_TYPE = VANILLA_FLUID_TYPES.register("lava", () ->
            new FluidType(FluidType.Properties.create()
                    .descriptionId("block.minecraft.lava")
                    .canSwim(false)
                    .canDrown(false)
                    .pathType(BlockPathTypes.LAVA)
                    .adjacentPathType(null)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                    .lightLevel(15)
                    .density(3000)
                    .viscosity(6000)
                    .temperature(1300))
            {
                @Override
                public double motionScale(Entity entity)
                {
                    return entity.level().dimensionType().ultraWarm() ? 0.007D : 0.0023333333333333335D;
                }

                @Override
                public void setItemMovement(ItemEntity entity)
                {
                    Vec3 vec3 = entity.getDeltaMovement();
                    entity.setDeltaMovement(vec3.x * (double)0.95F, vec3.y + (double)(vec3.y < (double)0.06F ? 5.0E-4F : 0.0F), vec3.z * (double)0.95F);
                }

                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
                {
                    consumer.accept(new IClientFluidTypeExtensions()
                    {
                        private static final ResourceLocation LAVA_STILL = new ResourceLocation("block/lava_still"),
                                LAVA_FLOW = new ResourceLocation("block/lava_flow");

                        @Override
                        public ResourceLocation getStillTexture()
                        {
                            return LAVA_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture()
                        {
                            return LAVA_FLOW;
                        }
                    });
                }
            });

    private static boolean enableMilkFluid = false;
    public static final RegistryObject<SoundEvent> BUCKET_EMPTY_MILK = RegistryObject.create(new ResourceLocation("item.bucket.empty_milk"), ForgeRegistries.SOUND_EVENTS);
    public static final RegistryObject<SoundEvent> BUCKET_FILL_MILK = RegistryObject.create(new ResourceLocation("item.bucket.fill_milk"), ForgeRegistries.SOUND_EVENTS);
    public static final RegistryObject<FluidType> MILK_TYPE = RegistryObject.createOptional(new ResourceLocation("milk"), ForgeRegistries.Keys.FLUID_TYPES.location(), "minecraft");
    public static final RegistryObject<Fluid> MILK = RegistryObject.create(new ResourceLocation("milk"), ForgeRegistries.FLUIDS);
    public static final RegistryObject<Fluid> FLOWING_MILK = RegistryObject.create(new ResourceLocation("flowing_milk"), ForgeRegistries.FLUIDS);

    /**
     * Run this method during mod constructor to enable milk and add it to the Minecraft milk bucket
     */
    public static void enableMilkFluid()
    {
        enableMilkFluid = true;
    }

    public NeoForgeMod()
    {
        LOGGER.info(NEOFORGEMOD,"NeoForge mod loading, version {}, for MC {} with MCP {}", NeoForgeVersion.getVersion(), NeoFormVersion.getMCVersion(), NeoFormVersion.getMCPVersion());
        ForgeSnapshotsMod.logStartupWarning();

        CrashReportCallables.registerCrashCallable("Crash Report UUID", ()-> {
            final UUID uuid = UUID.randomUUID();
            LOGGER.fatal("Preparing crash report with UUID {}", uuid);
            return uuid.toString();
        });

        LOGGER.debug(NEOFORGEMOD, "Loading Network data for FML net version: {}", NetworkConstants.init());
        CrashReportCallables.registerCrashCallable("FML", NeoForgeVersion::getSpec);
        CrashReportCallables.registerCrashCallable("NeoForge", ()-> NeoForgeVersion.getGroup()+":"+ NeoForgeVersion.getVersion());

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Forge-provided datapack registries
        modEventBus.addListener((DataPackRegistryEvent.NewRegistry event) -> {
            event.dataPackRegistry(ForgeRegistries.Keys.BIOME_MODIFIERS, BiomeModifier.DIRECT_CODEC);
            event.dataPackRegistry(ForgeRegistries.Keys.STRUCTURE_MODIFIERS, StructureModifier.DIRECT_CODEC);
        });
        modEventBus.addListener(this::preInit);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::loadComplete);
        modEventBus.addListener(this::registerFluids);
        modEventBus.addListener(this::registerVanillaDisplayContexts);
        modEventBus.addListener(this::registerLootData);
        ATTRIBUTES.register(modEventBus);
        COMMAND_ARGUMENT_TYPES.register(modEventBus);
        BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
        STRUCTURE_MODIFIER_SERIALIZERS.register(modEventBus);
        HOLDER_SET_TYPES.register(modEventBus);
        VANILLA_FLUID_TYPES.register(modEventBus);
        VANILLA_INGREDIENT_TYPES.register(modEventBus);
        INGREDIENT_TYPES.register(modEventBus);
        CONDITION_CODECS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(this::serverStopping);
        NeoForge.EVENT_BUS.addListener(this::missingSoundMapping);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, NeoForgeConfig.clientSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, NeoForgeConfig.serverSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, NeoForgeConfig.commonSpec);
        modEventBus.register(NeoForgeConfig.class);
        ForgeDeferredRegistriesSetup.setup(modEventBus);
        // Forge does not display problems when the remote is not matching.
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, ()->new IExtensionPoint.DisplayTest(()->"ANY", (remote, isServer)-> true));
        StartupMessageManager.addModMessage("NeoForge version "+ NeoForgeVersion.getVersion());

        NeoForge.EVENT_BUS.addListener(VillagerTradingManager::loadTrades);
        NeoForge.EVENT_BUS.register(new NeoForgeEventHandler());
        NeoForge.EVENT_BUS.addListener(this::registerPermissionNodes);

        UsernameCache.load();
        TierSortingRegistry.init();
        if (FMLEnvironment.dist == Dist.CLIENT) ClientCommandHandler.init();
        DualStackUtils.initialise();

        ForgeRegistries.ITEMS.tags().addOptionalTagDefaults(Tags.Items.ENCHANTING_FUELS, Set.of(ForgeRegistries.ITEMS.getDelegateOrThrow(Items.LAPIS_LAZULI)));
    }

    public void preInit(FMLCommonSetupEvent evt)
    {
        VersionChecker.startVersionCheck();
        VanillaPacketSplitter.register();
    }

    public void loadComplete(FMLLoadCompleteEvent event)
    {
    }

    public void serverStopping(ServerStoppingEvent evt)
    {
        WorldWorkerManager.clear();
    }

    public void gatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        gen.addProvider(true, new PackMetadataGenerator(packOutput)
                .add(PackMetadataSection.TYPE, new PackMetadataSection(
                        Component.translatable("pack.forge.description"),
                        DetectedVersion.BUILT_IN.getPackVersion(PackType.CLIENT_RESOURCES),
                        Optional.empty(),
                        Optional.of(new PackMetadataSection.NeoForgeData(Optional.of(Arrays.stream(PackType.values()).collect(Collectors.toMap(Function.identity(), DetectedVersion.BUILT_IN::getPackVersion))))
                )))
        );
        NeoForgeBlockTagsProvider blockTags = new NeoForgeBlockTagsProvider(packOutput, lookupProvider, existingFileHelper);
        gen.addProvider(event.includeServer(), blockTags);
        gen.addProvider(event.includeServer(), new NeoForgeItemTagsProvider(packOutput, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
        gen.addProvider(event.includeServer(), new NeoForgeEntityTypeTagsProvider(packOutput, lookupProvider, existingFileHelper));
        gen.addProvider(event.includeServer(), new NeoForgeFluidTagsProvider(packOutput, lookupProvider, existingFileHelper));
        gen.addProvider(event.includeServer(), new NeoForgeRecipeProvider(packOutput, lookupProvider));
        gen.addProvider(event.includeServer(), new NeoForgeLootTableProvider(packOutput));
        gen.addProvider(event.includeServer(), new NeoForgeBiomeTagsProvider(packOutput, lookupProvider, existingFileHelper));

        gen.addProvider(event.includeClient(), new NeoForgeSpriteSourceProvider(packOutput, lookupProvider, existingFileHelper));
        gen.addProvider(event.includeClient(), new VanillaSoundDefinitionsProvider(packOutput, existingFileHelper));
    }

    public void missingSoundMapping(MissingMappingsEvent event)
    {
        if (event.getKey() != ForgeRegistries.Keys.SOUND_EVENTS)
            return;

        //Removed in 1.15, see https://minecraft.gamepedia.com/Parrot#History
        List<String> removedSounds = Arrays.asList("entity.parrot.imitate.panda", "entity.parrot.imitate.zombie_pigman", "entity.parrot.imitate.enderman", "entity.parrot.imitate.polar_bear", "entity.parrot.imitate.wolf");
        for (MissingMappingsEvent.Mapping<SoundEvent> mapping : event.getAllMappings(ForgeRegistries.Keys.SOUND_EVENTS))
        {
            ResourceLocation regName = mapping.getKey();
            if (regName != null && regName.getNamespace().equals("minecraft"))
            {
                String path = regName.getPath();
                if (removedSounds.stream().anyMatch(s -> s.equals(path)))
                {
                    LOGGER.info("Ignoring removed minecraft sound {}", regName);
                    mapping.ignore();
                }
            }
        }
    }

    // done in an event instead of deferred to only enable if a mod requests it
    public void registerFluids(RegisterEvent event)
    {
        if (enableMilkFluid)
        {
            // register milk fill, empty sounds (delegates to water fill, empty sounds)
            event.register(ForgeRegistries.Keys.SOUND_EVENTS, helper -> {
                helper.register(BUCKET_EMPTY_MILK.getId(), SoundEvent.createVariableRangeEvent(BUCKET_EMPTY_MILK.getId()));
                helper.register(BUCKET_FILL_MILK.getId(), SoundEvent.createVariableRangeEvent(BUCKET_FILL_MILK.getId()));
            });

            // register fluid type
            event.register(ForgeRegistries.Keys.FLUID_TYPES, helper -> helper.register(MILK_TYPE.getId(), new FluidType(
                    FluidType.Properties.create().density(1024).viscosity(1024)
                            .sound(SoundActions.BUCKET_FILL, BUCKET_FILL_MILK.get())
                            .sound(SoundActions.BUCKET_EMPTY, BUCKET_EMPTY_MILK.get())
            )
            {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
                {
                    consumer.accept(new IClientFluidTypeExtensions()
                    {
                        private static final ResourceLocation MILK_STILL = new ResourceLocation("neoforge", "block/milk_still"),
                                MILK_FLOW = new ResourceLocation("neoforge", "block/milk_flowing");

                        @Override
                        public ResourceLocation getStillTexture()
                        {
                            return MILK_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture()
                        {
                            return MILK_FLOW;
                        }
                    });
                }
            }));

            // register fluids
            event.register(ForgeRegistries.Keys.FLUIDS, helper -> {
                // set up properties
                BaseFlowingFluid.Properties properties = new BaseFlowingFluid.Properties(MILK_TYPE, MILK, FLOWING_MILK).bucket(() -> Items.MILK_BUCKET);

                helper.register(MILK.getId(), new BaseFlowingFluid.Source(properties));
                helper.register(FLOWING_MILK.getId(), new BaseFlowingFluid.Flowing(properties));
            });
        }
    }

    public void registerVanillaDisplayContexts(RegisterEvent event)
    {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.DISPLAY_CONTEXTS))
        {
            IForgeRegistryInternal<ItemDisplayContext> forgeRegistry = (IForgeRegistryInternal<ItemDisplayContext>) event.<ItemDisplayContext>getForgeRegistry();
            if (forgeRegistry == null)
                throw new IllegalStateException("Item display context was not a NeoForge registry, wtf???");

            Arrays.stream(ItemDisplayContext.values())
                    .filter(Predicate.not(ItemDisplayContext::isModded))
                    .forEach(ctx -> forgeRegistry.register(ctx.getId(), new ResourceLocation("minecraft", ctx.getSerializedName()), ctx));
        }
    }

    public void registerLootData(RegisterEvent event)
    {
        if (!event.getRegistryKey().equals(Registries.LOOT_CONDITION_TYPE))
            return;

        event.register(Registries.LOOT_CONDITION_TYPE, new ResourceLocation("neoforge:loot_table_id"), () -> LootTableIdCondition.LOOT_TABLE_ID);
        event.register(Registries.LOOT_CONDITION_TYPE, new ResourceLocation("neoforge:can_tool_perform_action"), () -> CanToolPerformAction.LOOT_CONDITION_TYPE);
    }

    public static final PermissionNode<Boolean> USE_SELECTORS_PERMISSION = new PermissionNode<>("neoforge", "use_entity_selectors",
            PermissionTypes.BOOLEAN, (player, uuid, contexts) -> player != null && player.hasPermissions(Commands.LEVEL_GAMEMASTERS));

    public void registerPermissionNodes(PermissionGatherEvent.Nodes event)
    {
        event.addNodes(USE_SELECTORS_PERMISSION);
    }
}
