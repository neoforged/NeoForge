/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.common.advancements.critereon.ICustomItemPredicate;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
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
        // Forge
        public static final ResourceKey<Registry<EntityDataSerializer<?>>> ENTITY_DATA_SERIALIZERS = key("entity_data_serializers");
        public static final ResourceKey<Registry<Codec<? extends IGlobalLootModifier>>> GLOBAL_LOOT_MODIFIER_SERIALIZERS = key("global_loot_modifier_serializers");
        public static final ResourceKey<Registry<Codec<? extends BiomeModifier>>> BIOME_MODIFIER_SERIALIZERS = key("biome_modifier_serializers");
        public static final ResourceKey<Registry<Codec<? extends StructureModifier>>> STRUCTURE_MODIFIER_SERIALIZERS = key("structure_modifier_serializers");
        public static final ResourceKey<Registry<FluidType>> FLUID_TYPES = key("fluid_type");
        public static final ResourceKey<Registry<HolderSetType>> HOLDER_SET_TYPES = key("holder_set_type");
        public static final ResourceKey<Registry<ItemDisplayContext>> DISPLAY_CONTEXTS = key("display_contexts");
        public static final ResourceKey<Registry<IngredientType<?>>> INGREDIENT_TYPES = key("ingredient_serializer");
        public static final ResourceKey<Registry<Codec<? extends ICondition>>> CONDITION_CODECS = key("condition_codecs");
        public static final ResourceKey<Registry<Codec<? extends ICustomItemPredicate>>> ITEM_PREDICATE_SERIALIZERS = key("item_predicates");

        // Forge Dynamic
        public static final ResourceKey<Registry<BiomeModifier>> BIOME_MODIFIERS = key("biome_modifier");
        public static final ResourceKey<Registry<StructureModifier>> STRUCTURE_MODIFIERS = key("structure_modifier");

        private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(new ResourceLocation(NeoForgeVersion.MOD_ID, name));
        }
    }
}
