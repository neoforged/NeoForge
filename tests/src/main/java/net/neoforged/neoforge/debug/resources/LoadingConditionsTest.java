/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NeoForgeConditions;
import net.neoforged.neoforge.common.data.JsonCodecProvider;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
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

        test.registrationHelper().generateWorldRegistries(new RegistrySetBuilder().add(Registries.TRIM_PATTERN, registry -> {
            registry.register(
                    absentStaticObject,
                    new TrimPattern(absentStaticObject.identifier(), Component.empty(), false),
                    NeoForgeConditions.not(NeoForgeConditions.itemRegistered("nope:no_such_item")));
            registry.register(
                    presentStaticObject,
                    new TrimPattern(presentStaticObject.identifier(), Component.empty(), false),
                    NeoForgeConditions.itemRegistered("redstone"));
            registry.register(
                    absentStaticTag,
                    new TrimPattern(absentStaticTag.identifier(), Component.empty(), false),
                    NeoForgeConditions.itemTagEmpty("nope:no_such_item_tag"));
            registry.register(
                    presentEmptyStaticTag,
                    new TrimPattern(presentEmptyStaticTag.identifier(), Component.empty(), false),
                    NeoForgeConditions.itemTagEmpty("c:drinks/juice"));
            registry.register(
                    presentNonEmptyStaticTag,
                    new TrimPattern(presentNonEmptyStaticTag.identifier(), Component.empty(), false),
                    NeoForgeConditions.not(NeoForgeConditions.itemTagEmpty("c:eggs")));
        }));

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

        LootTableProvider.SubProviderEntry entry = new LootTableProvider.SubProviderEntry(context -> () -> {
            context.accept(absentStaticObject, LootTable.lootTable()
                    .withCondition(NeoForgeConditions.not(NeoForgeConditions.itemRegistered("nope:no_such_item"))));
            context.accept(presentStaticObject, LootTable.lootTable()
                    .withCondition(NeoForgeConditions.itemRegistered("redstone")));
            context.accept(absentStaticTag, LootTable.lootTable()
                    .withCondition(NeoForgeConditions.itemTagEmpty("nope:no_such_item_tag")));
            context.accept(presentEmptyStaticTag, LootTable.lootTable()
                    .withCondition(NeoForgeConditions.itemTagEmpty("c:drinks/juice")));
            context.accept(presentNonEmptyStaticTag, LootTable.lootTable()
                    .withCondition(NeoForgeConditions.not(NeoForgeConditions.itemTagEmpty("c:eggs"))));
            context.accept(absentDatapackRegistryObject, LootTable.lootTable()
                    .withCondition(NeoForgeConditions.not(NeoForgeConditions.registered(Registries.BIOME, Identifier.parse("nope:no_such_biome")))));
            context.accept(presentDatapackRegistryObject, LootTable.lootTable()
                    .withCondition(NeoForgeConditions.registered(Registries.BIOME, Identifier.parse("plains"))));
            context.accept(absentDatapackRegistryTag, LootTable.lootTable()
                    .withCondition(NeoForgeConditions.tagEmpty(Registries.BIOME, Identifier.parse("nope:no_such_biome_tag"))));
            context.accept(presentEmptyDatapackRegistryTag, LootTable.lootTable()
                    .withCondition(NeoForgeConditions.tagEmpty(Tags.Biomes.HIDDEN_FROM_LOCATOR_SELECTION)));
            context.accept(presentNonEmptyDatapackRegistryTag, LootTable.lootTable()
                    .withCondition(NeoForgeConditions.not(NeoForgeConditions.tagEmpty(Tags.Biomes.IS_PLAINS))));
        }, LootContextParamSets.EMPTY);
        LootTableProvider lootTableProvider = new LootTableProvider(Set.of(), List.of(entry));
        test.registrationHelper().generateReloadableRegistries(new RegistrySetBuilder().add(Registries.LOOT_TABLE, lootTableProvider));

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
        String directory = "reload_listener_condition_test_dummys";
        Codec<Object> codec = MapCodec.unitCodec(Object::new);
        ResourceKey<Registry<Object>> dummyRegKey = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(modId, "reload_listener_condition_test_dummy"));
        ResourceKey<Object> absentStaticObject = ResourceKey.create(dummyRegKey, Identifier.fromNamespaceAndPath(modId, "absent_static_object"));
        ResourceKey<Object> presentStaticObject = ResourceKey.create(dummyRegKey, Identifier.fromNamespaceAndPath(modId, "present_static_object"));
        ResourceKey<Object> absentStaticTag = ResourceKey.create(dummyRegKey, Identifier.fromNamespaceAndPath(modId, "absent_static_tag"));
        ResourceKey<Object> presentEmptyStaticTag = ResourceKey.create(dummyRegKey, Identifier.fromNamespaceAndPath(modId, "present_empty_static_tag"));
        ResourceKey<Object> presentNonEmptyStaticTag = ResourceKey.create(dummyRegKey, Identifier.fromNamespaceAndPath(modId, "present_non_empty_static_tag"));
        ResourceKey<Object> absentDatapackRegistryObject = ResourceKey.create(dummyRegKey, Identifier.fromNamespaceAndPath(modId, "absent_datapack_registry_object"));
        ResourceKey<Object> presentDatapackRegistryObject = ResourceKey.create(dummyRegKey, Identifier.fromNamespaceAndPath(modId, "present_datapack_registry_object"));
        ResourceKey<Object> absentDatapackRegistryTag = ResourceKey.create(dummyRegKey, Identifier.fromNamespaceAndPath(modId, "absent_datapack_registry_tag"));
        ResourceKey<Object> presentEmptyDatapackRegistryTag = ResourceKey.create(dummyRegKey, Identifier.fromNamespaceAndPath(modId, "present_empty_datapack_registry_tag"));
        ResourceKey<Object> presentNonEmptyDatapackRegistryTag = ResourceKey.create(dummyRegKey, Identifier.fromNamespaceAndPath(modId, "present_non_empty_datapack_registry_tag"));
        ResourceKey<Object> absentReloadableRegistryObject = ResourceKey.create(dummyRegKey, Identifier.fromNamespaceAndPath(modId, "absent_reloadable_registry_object"));
        ResourceKey<Object> presentReloadableRegistryObject = ResourceKey.create(dummyRegKey, Identifier.fromNamespaceAndPath(modId, "present_reloadable_registry_object"));
        ResourceKey<Object> absentReloadableRegistryTag = ResourceKey.create(dummyRegKey, Identifier.fromNamespaceAndPath(modId, "absent_reloadable_registry_tag"));
        ResourceKey<Object> presentEmptyReloadableRegistryTag = ResourceKey.create(dummyRegKey, Identifier.fromNamespaceAndPath(modId, "present_empty_reloadable_registry_tag"));
        ResourceKey<Object> presentNonEmptyReloadableRegistryTag = ResourceKey.create(dummyRegKey, Identifier.fromNamespaceAndPath(modId, "present_non_empty_reloadable_registry_tag"));

        TagKey<LootTable> emptyLootTableTag = TagKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(modId, "empty"));
        TagKey<LootTable> nonEmptyLootTableTag = TagKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(modId, "not_empty"));

        test.registrationHelper().addClientProvider(event -> {
            return new TagsProvider<>(event.getGenerator().getPackOutput(), Registries.LOOT_TABLE, event.getReloadableLookupProvider(), modId) {
                @Override
                protected void addTags(HolderLookup.Provider registries) {
                    tag(emptyLootTableTag);
                    tag(nonEmptyLootTableTag).add(BuiltInLootTables.ANCIENT_CITY);
                }
            };
        });
        test.registrationHelper().addClientProvider(event -> new JsonCodecProvider<>(event.getGenerator().getPackOutput(), PackOutput.Target.DATA_PACK, directory, codec, event.getReloadableLookupProvider(), modId) {
            @Override
            protected void gather() {
                add(absentStaticObject, NeoForgeConditions.not(NeoForgeConditions.itemRegistered("nope:no_such_item")));
                add(presentStaticObject, NeoForgeConditions.itemRegistered("redstone"));
                add(absentStaticTag, NeoForgeConditions.itemTagEmpty("nope:no_such_item_tag"));
                add(presentEmptyStaticTag, NeoForgeConditions.itemTagEmpty("c:drinks/juice"));
                add(presentNonEmptyStaticTag, NeoForgeConditions.not(NeoForgeConditions.itemTagEmpty("c:eggs")));
                add(absentDatapackRegistryObject, NeoForgeConditions.not(NeoForgeConditions.registered(Registries.BIOME, Identifier.parse("nope:no_such_biome"))));
                add(presentDatapackRegistryObject, NeoForgeConditions.registered(Registries.BIOME, Identifier.parse("plains")));
                add(absentDatapackRegistryTag, NeoForgeConditions.tagEmpty(Registries.BIOME, Identifier.parse("nope:no_such_biome_tag")));
                add(presentEmptyDatapackRegistryTag, NeoForgeConditions.tagEmpty(Tags.Biomes.HIDDEN_FROM_LOCATOR_SELECTION));
                add(presentNonEmptyDatapackRegistryTag, NeoForgeConditions.not(NeoForgeConditions.tagEmpty(Tags.Biomes.IS_PLAINS)));
                add(absentReloadableRegistryObject, NeoForgeConditions.not(NeoForgeConditions.registered(Registries.LOOT_TABLE, Identifier.parse("nope:no_such_loot_table"))));
                add(presentReloadableRegistryObject, NeoForgeConditions.registered(BuiltInLootTables.ANCIENT_CITY));
                add(absentReloadableRegistryTag, NeoForgeConditions.tagEmpty(Registries.LOOT_TABLE, Identifier.parse("nope:no_such_loot_table_tag")));
                add(presentEmptyReloadableRegistryTag, NeoForgeConditions.tagEmpty(emptyLootTableTag));
                add(presentNonEmptyReloadableRegistryTag, NeoForgeConditions.not(NeoForgeConditions.tagEmpty(nonEmptyLootTableTag)));
            }

            private void add(ResourceKey<Object> key, ICondition condition) {
                conditionally(key.identifier(), b -> b.withCarrier(new Object()).addCondition(condition));
            }
        });

        test.eventListeners().forge().addListener((AddServerReloadListenersEvent event) -> {
            Identifier listenerKey = Identifier.fromNamespaceAndPath(modId, "reload_listener_condition_test_dummy_loader");
            FileToIdConverter lister = FileToIdConverter.json(directory);
            event.addListener(listenerKey, new SimpleJsonResourceReloadListener<>(codec, lister) {
                @Override
                protected void apply(Map<Identifier, Object> preparations, ResourceManager manager, ProfilerFiller profiler) {
                    assertEntryPresence(test, preparations, (map, key) -> Optional.ofNullable(map.get(key.identifier())), "reload_listener", Set.of(
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
                            presentEmptyReloadableRegistryTag,
                            presentNonEmptyReloadableRegistryTag));
                }
            });
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
