/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.advancements.critereon.ICustomItemPredicate;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;

/**
 * A class that exposes static references to all vanilla and Forge registries.
 * Created to have a central place to access the registries directly if modders need.
 * It is still advised that if you are registering things to use {@link RegisterEvent} or {@link DeferredRegister}, but queries and iterations can use this.
 */
public class ForgeRegistries {
    // Custom forge registries
    static final DeferredRegister<EntityDataSerializer<?>> DEFERRED_ENTITY_DATA_SERIALIZERS = DeferredRegister.create(Keys.ENTITY_DATA_SERIALIZERS, Keys.ENTITY_DATA_SERIALIZERS.location().getNamespace());
    public static final Registry<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DEFERRED_ENTITY_DATA_SERIALIZERS.makeRegistry(registryBuilder -> registryBuilder.sync(true));
    static final DeferredRegister<Codec<? extends IGlobalLootModifier>> DEFERRED_GLOBAL_LOOT_MODIFIER_SERIALIZERS = DeferredRegister.create(Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS.location().getNamespace());
    public static final Registry<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS = DEFERRED_GLOBAL_LOOT_MODIFIER_SERIALIZERS.makeRegistry(registryBuilder -> {});
    static final DeferredRegister<Codec<? extends BiomeModifier>> DEFERRED_BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(Keys.BIOME_MODIFIER_SERIALIZERS, Keys.BIOME_MODIFIER_SERIALIZERS.location().getNamespace());
    public static final Registry<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DEFERRED_BIOME_MODIFIER_SERIALIZERS.makeRegistry(registryBuilder -> {});
    static final DeferredRegister<Codec<? extends StructureModifier>> DEFERRED_STRUCTURE_MODIFIER_SERIALIZERS = DeferredRegister.create(Keys.STRUCTURE_MODIFIER_SERIALIZERS, Keys.STRUCTURE_MODIFIER_SERIALIZERS.location().getNamespace());
    public static final Registry<Codec<? extends StructureModifier>> STRUCTURE_MODIFIER_SERIALIZERS = DEFERRED_STRUCTURE_MODIFIER_SERIALIZERS.makeRegistry(registryBuilder -> {});
    static final DeferredRegister<FluidType> DEFERRED_FLUID_TYPES = DeferredRegister.create(Keys.FLUID_TYPES, Keys.FLUID_TYPES.location().getNamespace());
    public static final Registry<FluidType> FLUID_TYPES = DEFERRED_FLUID_TYPES.makeRegistry(registryBuilder -> {});
    static final DeferredRegister<HolderSetType> DEFERRED_HOLDER_SET_TYPES = DeferredRegister.create(Keys.HOLDER_SET_TYPES, Keys.HOLDER_SET_TYPES.location().getNamespace());
    public static final Registry<HolderSetType> HOLDER_SET_TYPES = DEFERRED_HOLDER_SET_TYPES.makeRegistry(registryBuilder -> {});
    static final DeferredRegister<ItemDisplayContext> DEFERRED_DISPLAY_CONTEXTS = DeferredRegister.create(Keys.DISPLAY_CONTEXTS, Keys.DISPLAY_CONTEXTS.location().getNamespace());
    public static final Registry<ItemDisplayContext> DISPLAY_CONTEXTS = DEFERRED_DISPLAY_CONTEXTS.makeRegistry(registryBuilder -> registryBuilder.sync(true)
            .maxId(128 * 2) // 0 -> 127 gets positive ID, 128 -> 256 gets negative ID
            .defaultKey(new ResourceLocation("none")));

    static final DeferredRegister<IngredientType<?>> DEFERRED_INGREDIENT_TYPES = DeferredRegister.create(Keys.INGREDIENT_TYPES, Keys.INGREDIENT_TYPES.location().getNamespace());
    /**
     * Calling {@link Supplier#get()} before {@link NewRegistryEvent} is fired will result in a null registry returned.
     * Use {@link Keys#INGREDIENT_TYPES} to create a {@link DeferredRegister}.
     */
    public static final Registry<IngredientType<?>> INGREDIENT_TYPES = DEFERRED_INGREDIENT_TYPES.makeRegistry(b -> {});

    static final DeferredRegister<Codec<? extends ICondition>> DEFERRED_CONDITION_CODECS = DeferredRegister.create(Keys.CONDITION_CODECS, Keys.CONDITION_CODECS.location().getNamespace());
    /**
     * Calling {@link Supplier#get()} before {@link NewRegistryEvent} is fired will result in a null registry returned.
     * Use {@link Keys#CONDITION_CODECS} to create a {@link DeferredRegister}.
     */
    public static final Registry<Codec<? extends ICondition>> CONDITION_SERIALIZERS = DEFERRED_CONDITION_CODECS.makeRegistry(b -> {});

    static final DeferredRegister<Codec<? extends ICustomItemPredicate>> DEFERRED_ITEM_PREDICATE_SERIALIZERS = DeferredRegister.create(Keys.ITEM_PREDICATE_SERIALIZERS, Keys.ITEM_PREDICATE_SERIALIZERS.location().getNamespace());
    /**
     * Calling {@link Supplier#get()} before {@link NewRegistryEvent} is fired will result in a null registry returned.
     * Use {@link Keys#ITEM_PREDICATE_SERIALIZERS} to create a {@link DeferredRegister}.
     */
    public static final Registry<Codec<? extends ICustomItemPredicate>> ITEM_PREDICATE_SERIALIZERS = DEFERRED_ITEM_PREDICATE_SERIALIZERS.makeRegistry(b -> {});

    public static final class Keys {
        //Vanilla
        public static final ResourceKey<Registry<Block>> BLOCKS = key("block");
        public static final ResourceKey<Registry<Fluid>> FLUIDS = key("fluid");
        public static final ResourceKey<Registry<Item>> ITEMS = key("item");
        public static final ResourceKey<Registry<MobEffect>> MOB_EFFECTS = key("mob_effect");
        public static final ResourceKey<Registry<Potion>> POTIONS = key("potion");
        public static final ResourceKey<Registry<Attribute>> ATTRIBUTES = key("attribute");
        public static final ResourceKey<Registry<StatType<?>>> STAT_TYPES = key("stat_type");
        public static final ResourceKey<Registry<ArgumentTypeInfo<?, ?>>> COMMAND_ARGUMENT_TYPES = key("command_argument_type");
        public static final ResourceKey<Registry<SoundEvent>> SOUND_EVENTS = key("sound_event");
        public static final ResourceKey<Registry<Enchantment>> ENCHANTMENTS = key("enchantment");
        public static final ResourceKey<Registry<EntityType<?>>> ENTITY_TYPES = key("entity_type");
        public static final ResourceKey<Registry<PaintingVariant>> PAINTING_VARIANTS = key("painting_variant");
        public static final ResourceKey<Registry<ParticleType<?>>> PARTICLE_TYPES = key("particle_type");
        public static final ResourceKey<Registry<MenuType<?>>> MENU_TYPES = key("menu");
        public static final ResourceKey<Registry<BlockEntityType<?>>> BLOCK_ENTITY_TYPES = key("block_entity_type");
        public static final ResourceKey<Registry<RecipeType<?>>> RECIPE_TYPES = key("recipe_type");
        public static final ResourceKey<Registry<RecipeSerializer<?>>> RECIPE_SERIALIZERS = key("recipe_serializer");
        public static final ResourceKey<Registry<VillagerProfession>> VILLAGER_PROFESSIONS = key("villager_profession");
        public static final ResourceKey<Registry<PoiType>> POI_TYPES = key("point_of_interest_type");
        public static final ResourceKey<Registry<MemoryModuleType<?>>> MEMORY_MODULE_TYPES = key("memory_module_type");
        public static final ResourceKey<Registry<SensorType<?>>> SENSOR_TYPES = key("sensor_type");
        public static final ResourceKey<Registry<Schedule>> SCHEDULES = key("schedule");
        public static final ResourceKey<Registry<Activity>> ACTIVITIES = key("activity");
        public static final ResourceKey<Registry<WorldCarver<?>>> WORLD_CARVERS = key("worldgen/carver");
        public static final ResourceKey<Registry<Feature<?>>> FEATURES = key("worldgen/feature");
        public static final ResourceKey<Registry<ChunkStatus>> CHUNK_STATUS = key("chunk_status");
        public static final ResourceKey<Registry<BlockStateProviderType<?>>> BLOCK_STATE_PROVIDER_TYPES = key("worldgen/block_state_provider_type");
        public static final ResourceKey<Registry<FoliagePlacerType<?>>> FOLIAGE_PLACER_TYPES = key("worldgen/foliage_placer_type");
        public static final ResourceKey<Registry<TreeDecoratorType<?>>> TREE_DECORATOR_TYPES = key("worldgen/tree_decorator_type");

        // Vanilla Dynamic
        public static final ResourceKey<Registry<Biome>> BIOMES = key("worldgen/biome");

        // Forge
        public static final ResourceKey<Registry<EntityDataSerializer<?>>> ENTITY_DATA_SERIALIZERS = key("neoforge:entity_data_serializers");
        public static final ResourceKey<Registry<Codec<? extends IGlobalLootModifier>>> GLOBAL_LOOT_MODIFIER_SERIALIZERS = key("neoforge:global_loot_modifier_serializers");
        public static final ResourceKey<Registry<Codec<? extends BiomeModifier>>> BIOME_MODIFIER_SERIALIZERS = key("neoforge:biome_modifier_serializers");
        public static final ResourceKey<Registry<Codec<? extends StructureModifier>>> STRUCTURE_MODIFIER_SERIALIZERS = key("neoforge:structure_modifier_serializers");
        public static final ResourceKey<Registry<FluidType>> FLUID_TYPES = key("neoforge:fluid_type");
        public static final ResourceKey<Registry<HolderSetType>> HOLDER_SET_TYPES = key("neoforge:holder_set_type");
        public static final ResourceKey<Registry<ItemDisplayContext>> DISPLAY_CONTEXTS = key("neoforge:display_contexts");
        public static final ResourceKey<Registry<IngredientType<?>>> INGREDIENT_TYPES = key("neoforge:ingredient_serializer");
        public static final ResourceKey<Registry<Codec<? extends ICondition>>> CONDITION_CODECS = key("neoforge:condition_codecs");
        public static final ResourceKey<Registry<Codec<? extends ICustomItemPredicate>>> ITEM_PREDICATE_SERIALIZERS = key("neoforge:item_predicates");

        // Forge Dynamic
        public static final ResourceKey<Registry<BiomeModifier>> BIOME_MODIFIERS = key("neoforge:biome_modifier");
        public static final ResourceKey<Registry<StructureModifier>> STRUCTURE_MODIFIERS = key("neoforge:structure_modifier");

        private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(new ResourceLocation(name));
        }
    }
}
