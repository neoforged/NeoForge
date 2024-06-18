/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;

/**
 * A class that exposes static references to NeoForge registries.
 * It is still advised that you register things with {@link RegisterEvent} or {@link DeferredRegister}, but queries and iterations can use this.
 *
 * <p>Vanilla's registries can be found in {@link BuiltInRegistries}, and their keys in {@link Registries}.
 */
public class NeoForgeRegistries {
    // Custom NeoForge registries
    public static final Registry<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = new RegistryBuilder<>(Keys.ENTITY_DATA_SERIALIZERS).sync(true).create();
    public static final Registry<MapCodec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS = new RegistryBuilder<>(Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS).create();
    public static final Registry<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = new RegistryBuilder<>(Keys.BIOME_MODIFIER_SERIALIZERS).create();
    public static final Registry<MapCodec<? extends StructureModifier>> STRUCTURE_MODIFIER_SERIALIZERS = new RegistryBuilder<>(Keys.STRUCTURE_MODIFIER_SERIALIZERS).create();
    public static final Registry<FluidType> FLUID_TYPES = new RegistryBuilder<>(Keys.FLUID_TYPES).sync(true).create();
    public static final Registry<HolderSetType> HOLDER_SET_TYPES = new RegistryBuilder<>(Keys.HOLDER_SET_TYPES).create();
    public static final Registry<IngredientType<?>> INGREDIENT_TYPES = new RegistryBuilder<>(Keys.INGREDIENT_TYPES).sync(true).create();
    public static final Registry<FluidIngredientType<?>> FLUID_INGREDIENT_TYPES = new RegistryBuilder<>(Keys.FLUID_INGREDIENT_TYPES).sync(true).create();
    public static final Registry<MapCodec<? extends ICondition>> CONDITION_SERIALIZERS = new RegistryBuilder<>(Keys.CONDITION_CODECS).create();
    public static final Registry<AttachmentType<?>> ATTACHMENT_TYPES = new RegistryBuilder<>(Keys.ATTACHMENT_TYPES).create();

    // Reminder: If you add a registry to NeoForge itself, remember to add it to NeoForgeRegistriesSetup#registerRegistries.

    public static final class Keys {
        // NeoForge
        public static final ResourceKey<Registry<EntityDataSerializer<?>>> ENTITY_DATA_SERIALIZERS = key("entity_data_serializers");
        public static final ResourceKey<Registry<MapCodec<? extends IGlobalLootModifier>>> GLOBAL_LOOT_MODIFIER_SERIALIZERS = key("global_loot_modifier_serializers");
        public static final ResourceKey<Registry<MapCodec<? extends BiomeModifier>>> BIOME_MODIFIER_SERIALIZERS = key("biome_modifier_serializers");
        public static final ResourceKey<Registry<MapCodec<? extends StructureModifier>>> STRUCTURE_MODIFIER_SERIALIZERS = key("structure_modifier_serializers");
        public static final ResourceKey<Registry<FluidType>> FLUID_TYPES = key("fluid_type");
        public static final ResourceKey<Registry<HolderSetType>> HOLDER_SET_TYPES = key("holder_set_type");
        public static final ResourceKey<Registry<IngredientType<?>>> INGREDIENT_TYPES = key("ingredient_serializer");
        public static final ResourceKey<Registry<FluidIngredientType<?>>> FLUID_INGREDIENT_TYPES = key("fluid_ingredient_type");
        public static final ResourceKey<Registry<MapCodec<? extends ICondition>>> CONDITION_CODECS = key("condition_codecs");
        public static final ResourceKey<Registry<AttachmentType<?>>> ATTACHMENT_TYPES = key("attachment_types");

        // NeoForge Dynamic
        public static final ResourceKey<Registry<BiomeModifier>> BIOME_MODIFIERS = key("biome_modifier");
        public static final ResourceKey<Registry<StructureModifier>> STRUCTURE_MODIFIERS = key("structure_modifier");

        private static <T> ResourceKey<Registry<T>> key(String name) {
            return ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, name));
        }
    }
}
