/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.resources;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NeoForgeConditions;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(groups = LoadingConditionsTest.GROUP)
public final class LoadingConditionsTest {
    public static final String GROUP = "resources";

    @TestHolder(description = "Tests that datapack registry loading conditions have access to static registry contents and associated tags", enabledByDefault = true)
    static void datapackRegistryConditions(DynamicTest test) {
        String modId = test.createModId();
        ResourceKey<TrimPattern> absentStaticObject = ResourceKey.create(Registries.TRIM_PATTERN, Identifier.fromNamespaceAndPath(modId, "absent_static_object"));
        ResourceKey<TrimPattern> presentStaticObject = ResourceKey.create(Registries.TRIM_PATTERN, Identifier.fromNamespaceAndPath(modId, "present_static_object"));
        ResourceKey<TrimPattern> absentStaticTag = ResourceKey.create(Registries.TRIM_PATTERN, Identifier.fromNamespaceAndPath(modId, "absent_static_tag"));
        ResourceKey<TrimPattern> presentEmptyStaticTag = ResourceKey.create(Registries.TRIM_PATTERN, Identifier.fromNamespaceAndPath(modId, "present_empty_static_tag"));
        ResourceKey<TrimPattern> presentNonEmptyStaticTag = ResourceKey.create(Registries.TRIM_PATTERN, Identifier.fromNamespaceAndPath(modId, "present_non_empty_static_tag"));

        test.registrationHelper().addClientProvider(event -> {
            RegistrySetBuilder entries = new RegistrySetBuilder().add(Registries.TRIM_PATTERN, registry -> {
                registry.register(absentStaticObject, new TrimPattern(absentStaticObject.identifier(), Component.empty(), false));
                registry.register(presentStaticObject, new TrimPattern(presentStaticObject.identifier(), Component.empty(), false));
                registry.register(absentStaticTag, new TrimPattern(absentStaticTag.identifier(), Component.empty(), false));
                registry.register(presentEmptyStaticTag, new TrimPattern(presentEmptyStaticTag.identifier(), Component.empty(), false));
                registry.register(presentNonEmptyStaticTag, new TrimPattern(presentNonEmptyStaticTag.identifier(), Component.empty(), false));
            });
            Map<ResourceKey<?>, List<ICondition>> conditions = Map.of(
                    absentStaticObject,
                    List.of(NeoForgeConditions.not(NeoForgeConditions.itemRegistered("nope:no_such_item"))),
                    presentStaticObject,
                    List.of(NeoForgeConditions.itemRegistered("redstone")),
                    absentStaticTag,
                    List.of(NeoForgeConditions.itemTagEmpty("nope:no_such_item_tag")),
                    presentEmptyStaticTag,
                    List.of(NeoForgeConditions.itemTagEmpty("c:drinks/juice")),
                    presentNonEmptyStaticTag,
                    List.of(NeoForgeConditions.not(NeoForgeConditions.itemTagEmpty("c:eggs"))));
            return new DatapackBuiltinEntriesProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(), entries, conditions, Set.of(modId));
        });

        test.eventListeners().forge().addListener((TagsUpdatedEvent.ServerDataLoad event) -> {
            assertEntryPresence(test, event.getRegistries(), RegistryAccess::get, "datapack registry", Set.of(
                    absentStaticObject,
                    presentStaticObject,
                    absentStaticTag,
                    presentEmptyStaticTag,
                    presentNonEmptyStaticTag));
        });
    }

    @TestHolder(description = "Tests that reloadable registry loading conditions have access to static and datapack registry contents and associated tags", enabledByDefault = true)
    static void reloadableRegistryConditions(DynamicTest test) {
        String modId = test.createModId();
        ResourceKey<LootTable> absentStaticObject = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(modId, "absent_static_object"));
        ResourceKey<LootTable> presentStaticObject = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(modId, "present_static_object"));
        ResourceKey<LootTable> absentStaticTag = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(modId, "absent_static_tag"));
        ResourceKey<LootTable> presentEmptyStaticTag = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(modId, "present_empty_static_tag"));
        ResourceKey<LootTable> presentNonEmptyStaticTag = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(modId, "present_non_empty_static_tag"));
        ResourceKey<LootTable> absentDatapackRegistryObject = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(modId, "absent_datapack_registry_object"));
        ResourceKey<LootTable> presentDatapackRegistryObject = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(modId, "present_datapack_registry_object"));
        ResourceKey<LootTable> absentDatapackRegistryTag = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(modId, "absent_datapack_registry_tag"));
        ResourceKey<LootTable> presentEmptyDatapackRegistryTag = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(modId, "present_empty_datapack_registry_tag"));
        ResourceKey<LootTable> presentNonEmptyDatapackRegistryTag = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(modId, "present_non_empty_datapack_registry_tag"));

        test.registrationHelper().addClientProvider(event -> {
            LootTableProvider.SubProviderEntry entry = new LootTableProvider.SubProviderEntry(_ -> consumer -> {
                consumer.accept(absentStaticObject, LootTable.lootTable()
                        .withCondition(NeoForgeConditions.not(NeoForgeConditions.itemRegistered("nope:no_such_item"))));
                consumer.accept(presentStaticObject, LootTable.lootTable()
                        .withCondition(NeoForgeConditions.itemRegistered("redstone")));
                consumer.accept(absentStaticTag, LootTable.lootTable()
                        .withCondition(NeoForgeConditions.itemTagEmpty("nope:no_such_item_tag")));
                consumer.accept(presentEmptyStaticTag, LootTable.lootTable()
                        .withCondition(NeoForgeConditions.itemTagEmpty("c:drinks/juice")));
                consumer.accept(presentNonEmptyStaticTag, LootTable.lootTable()
                        .withCondition(NeoForgeConditions.not(NeoForgeConditions.itemTagEmpty("c:eggs"))));
                consumer.accept(absentDatapackRegistryObject, LootTable.lootTable()
                        .withCondition(NeoForgeConditions.not(NeoForgeConditions.registered(Registries.BIOME, Identifier.parse("nope:no_such_biome")))));
                consumer.accept(presentDatapackRegistryObject, LootTable.lootTable()
                        .withCondition(NeoForgeConditions.registered(Registries.BIOME, Identifier.parse("plains"))));
                consumer.accept(absentDatapackRegistryTag, LootTable.lootTable()
                        .withCondition(NeoForgeConditions.tagEmpty(Registries.BIOME, Identifier.parse("nope:no_such_biome_tag"))));
                consumer.accept(presentEmptyDatapackRegistryTag, LootTable.lootTable()
                        .withCondition(NeoForgeConditions.tagEmpty(Tags.Biomes.HIDDEN_FROM_LOCATOR_SELECTION)));
                consumer.accept(presentNonEmptyDatapackRegistryTag, LootTable.lootTable()
                        .withCondition(NeoForgeConditions.not(NeoForgeConditions.tagEmpty(Tags.Biomes.IS_PLAINS))));
            }, LootContextParamSets.EMPTY);
            return new LootTableProvider(event.getGenerator().getPackOutput(), Set.of(), List.of(entry), event.getLookupProvider());
        });

        test.eventListeners().forge().addListener((TagsUpdatedEvent.ServerDataLoad event) -> {
            assertEntryPresence(test, event.getRegistries(), RegistryAccess::get, "reloadable registry", Set.of(
                    absentStaticObject,
                    presentStaticObject,
                    absentStaticTag,
                    presentEmptyStaticTag,
                    presentNonEmptyStaticTag,
                    absentDatapackRegistryObject,
                    presentDatapackRegistryObject,
                    absentDatapackRegistryTag,
                    presentEmptyDatapackRegistryTag,
                    presentNonEmptyDatapackRegistryTag));
        });
    }

    @TestHolder(description = "Tests that reload listener loading conditions have access to static, datapack and reloadable registry contents and associated tags", enabledByDefault = true)
    static void reloadListenerConditions(DynamicTest test) {
        String modId = test.createModId();
        ResourceKey<Recipe<?>> absentStaticObject = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(modId, "absent_static_object"));
        ResourceKey<Recipe<?>> presentStaticObject = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(modId, "present_static_object"));
        ResourceKey<Recipe<?>> absentStaticTag = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(modId, "absent_static_tag"));
        ResourceKey<Recipe<?>> presentEmptyStaticTag = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(modId, "present_empty_static_tag"));
        ResourceKey<Recipe<?>> presentNonEmptyStaticTag = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(modId, "present_non_empty_static_tag"));
        ResourceKey<Recipe<?>> absentDatapackRegistryObject = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(modId, "absent_datapack_registry_object"));
        ResourceKey<Recipe<?>> presentDatapackRegistryObject = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(modId, "present_datapack_registry_object"));
        ResourceKey<Recipe<?>> absentDatapackRegistryTag = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(modId, "absent_datapack_registry_tag"));
        ResourceKey<Recipe<?>> presentEmptyDatapackRegistryTag = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(modId, "present_empty_datapack_registry_tag"));
        ResourceKey<Recipe<?>> presentNonEmptyDatapackRegistryTag = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(modId, "present_non_empty_datapack_registry_tag"));
        ResourceKey<Recipe<?>> absentReloadableRegistryObject = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(modId, "absent_reloadable_registry_object"));
        ResourceKey<Recipe<?>> presentReloadableRegistryObject = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(modId, "present_reloadable_registry_object"));
        ResourceKey<Recipe<?>> absentReloadableRegistryTag = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(modId, "absent_reloadable_registry_tag"));
        ResourceKey<Recipe<?>> presentEmptyReloadableRegistryTag = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(modId, "present_empty_reloadable_registry_tag"));
        ResourceKey<Recipe<?>> presentNonEmptyReloadableRegistryTag = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(modId, "present_non_empty_reloadable_registry_tag"));

        TagKey<LootTable> emptyLootTableTag = TagKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(modId, "empty"));
        TagKey<LootTable> nonEmptyLootTableTag = TagKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(modId, "not_empty"));

        /*test.registrationHelper().addClientProvider(event -> {
            return new TagsProvider<>(event.getGenerator().getPackOutput(), Registries.LOOT_TABLE, event.getLookupProvider(), modId) {
                @Override
                protected void addTags(HolderLookup.Provider registries) {
                    tag(emptyLootTableTag);
                    tag(nonEmptyLootTableTag).add(BuiltInLootTables.ANCIENT_CITY);
                }
            };
        });*/
        test.registrationHelper().addClientProvider(event -> {
            class TestRecipes extends RecipeProvider {
                protected TestRecipes(HolderLookup.Provider registries, RecipeOutput output) {
                    super(registries, output);
                }

                @Override
                protected void buildRecipes() {
                    output.accept(absentStaticObject, new RepairItemRecipe(), null, NeoForgeConditions.not(NeoForgeConditions.itemRegistered("nope:no_such_item")));
                    output.accept(presentStaticObject, new RepairItemRecipe(), null, NeoForgeConditions.itemRegistered("redstone"));
                    output.accept(absentStaticTag, new RepairItemRecipe(), null, NeoForgeConditions.itemTagEmpty("nope:no_such_item_tag"));
                    output.accept(presentEmptyStaticTag, new RepairItemRecipe(), null, NeoForgeConditions.itemTagEmpty("c:drinks/juice"));
                    output.accept(presentNonEmptyStaticTag, new RepairItemRecipe(), null, NeoForgeConditions.not(NeoForgeConditions.itemTagEmpty("c:eggs")));
                    output.accept(absentDatapackRegistryObject, new RepairItemRecipe(), null, NeoForgeConditions.not(NeoForgeConditions.registered(Registries.BIOME, Identifier.parse("nope:no_such_biome"))));
                    output.accept(presentDatapackRegistryObject, new RepairItemRecipe(), null, NeoForgeConditions.registered(Registries.BIOME, Identifier.parse("plains")));
                    output.accept(absentDatapackRegistryTag, new RepairItemRecipe(), null, NeoForgeConditions.tagEmpty(Registries.BIOME, Identifier.parse("nope:no_such_biome_tag")));
                    output.accept(presentEmptyDatapackRegistryTag, new RepairItemRecipe(), null, NeoForgeConditions.tagEmpty(Tags.Biomes.HIDDEN_FROM_LOCATOR_SELECTION));
                    output.accept(presentNonEmptyDatapackRegistryTag, new RepairItemRecipe(), null, NeoForgeConditions.not(NeoForgeConditions.tagEmpty(Tags.Biomes.IS_PLAINS)));
                    output.accept(absentReloadableRegistryObject, new RepairItemRecipe(), null, NeoForgeConditions.not(NeoForgeConditions.registered(Registries.LOOT_TABLE, Identifier.parse("nope:no_such_loot_table"))));
                    output.accept(presentReloadableRegistryObject, new RepairItemRecipe(), null, NeoForgeConditions.registered(BuiltInLootTables.ANCIENT_CITY));
                    output.accept(absentReloadableRegistryTag, new RepairItemRecipe(), null, NeoForgeConditions.tagEmpty(Registries.LOOT_TABLE, Identifier.parse("nope:no_such_loot_table_tag")));
                    output.accept(presentEmptyReloadableRegistryTag, new RepairItemRecipe(), null, NeoForgeConditions.tagEmpty(emptyLootTableTag));
                    output.accept(presentNonEmptyReloadableRegistryTag, new RepairItemRecipe(), null, NeoForgeConditions.not(NeoForgeConditions.tagEmpty(nonEmptyLootTableTag)));
                }
            }
            return new RecipeProvider.Runner(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
                @Override
                protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
                    return new TestRecipes(registries, output);
                }

                @Override
                public String getName() {
                    return "LoadingConditionsTest - Recipes";
                }
            };
        });

        test.eventListeners().forge().addListener((TagsUpdatedEvent.ServerDataLoad event) -> {
            // TODO 26.3: enable non-empty reloadable registry tag test (presentNonEmptyReloadableRegistryTag) and move the tested tags to datagen above
            assertEntryPresence(test, event.getServerResources().getRecipeManager(), RecipeManager::byKey, "reload listener", Set.of(
                    absentStaticObject,
                    presentStaticObject,
                    absentStaticTag,
                    presentEmptyStaticTag,
                    presentNonEmptyStaticTag,
                    absentDatapackRegistryObject,
                    presentDatapackRegistryObject,
                    absentDatapackRegistryTag,
                    presentEmptyDatapackRegistryTag,
                    presentNonEmptyDatapackRegistryTag,
                    absentReloadableRegistryObject,
                    presentReloadableRegistryObject,
                    absentReloadableRegistryTag,
                    presentEmptyReloadableRegistryTag));
        });
    }

    private static <S, T> void assertEntryPresence(DynamicTest test, S source, BiFunction<S, ResourceKey<T>, Optional<?>> lookup, String type, Set<ResourceKey<T>> entries) {
        Set<ResourceKey<T>> missing = new HashSet<>();
        for (ResourceKey<T> key : entries) {
            if (lookup.apply(source, key).isEmpty()) {
                missing.add(key);
            }
        }
        if (!missing.isEmpty()) {
            StringBuilder message = new StringBuilder("The following ").append(type).append(" objects failed to load:\n");
            for (ResourceKey<T> key : missing) {
                message.append("\t")
                        .append(key.identifier())
                        .append("\n");
            }
            test.fail(message.toString());
        } else {
            test.pass();
        }
    }

    private LoadingConditionsTest() {}
}
