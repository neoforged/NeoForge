/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopperFullBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.common.DataMapHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.debug.EventTests;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueMerger;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover.Default;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.Oxidizable;
import net.neoforged.neoforge.registries.datamaps.builtin.Waxable;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "data.data_map")
public class DataMapTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if data map mergers function properly")
    static void dataMapMerger(final DynamicTest test, final RegistrationHelper reg) {
        final AdvancedDataMapType<Item, List<SomeObject>, Default<List<SomeObject>, Item>> someData = AdvancedDataMapType.builder(
                ResourceLocation.fromNamespaceAndPath(reg.modId(), "some_list"),
                Registries.ITEM, SomeObject.CODEC.listOf())
                .merger(DataMapValueMerger.listMerger())
                .build();

        test.framework().modEventBus().addListener((final RegisterDataMapTypesEvent event) -> event.register(someData));

        final String subpackName = reg.registerSubpack("second_layer");

        reg.addProvider(event -> new DataMapProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void gather(HolderLookup.Provider provider) {
                builder(someData)
                        .add(Items.BLUE_ORCHID.builtInRegistryHolder(), List.of(
                                new SomeObject(1, "a")), false);

                builder(someData)
                        .add(Items.DANDELION.builtInRegistryHolder(), List.of(
                                new SomeObject(14, "abc")), false);
            }

            @Override
            public String getName() {
                return "generator 1";
            }
        });

        reg.addProvider(event -> new DataMapProvider(event.getGenerator().getPackOutput(subpackName), event.getLookupProvider()) {
            @Override
            protected void gather(HolderLookup.Provider provider) {
                builder(someData)
                        .add(Items.BLUE_ORCHID.builtInRegistryHolder(), List.of(
                                new SomeObject(2, "b"),
                                new SomeObject(3, "c")), false);

                builder(someData)
                        .add(Items.DANDELION.builtInRegistryHolder(), List.of(
                                new SomeObject(99, "override")), true);
            }

            @Override
            public String getName() {
                return "generator 2";
            }
        });

        test.onGameTest(helper -> {
            helper.assertTrue(Objects.equals(Items.BLUE_ORCHID.builtInRegistryHolder()
                    .getData(someData),
                    List.of(
                            new SomeObject(1, "a"),
                            new SomeObject(2, "b"),
                            new SomeObject(3, "c"))),
                    "Merging didn't merge the lists!");

            helper.assertTrue(Objects.equals(Items.DANDELION.builtInRegistryHolder()
                    .getData(someData),
                    List.of(
                            new SomeObject(99, "override"))),
                    "Replace still merged!");

            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if data map removers function properly")
    static void dataMapRemover(final DynamicTest test, final RegistrationHelper reg) {
        record CustomRemover(List<String> keys) implements DataMapValueRemover<Item, Map<String, SomeObject>> {
            @Override
            public Optional<Map<String, SomeObject>> remove(Map<String, SomeObject> value, Registry<Item> registry, Either<TagKey<Item>, ResourceKey<Item>> source, Item object) {
                final var newMap = new HashMap<>(value);
                keys.forEach(newMap::remove);
                return Optional.of(newMap);
            }
        }

        final AdvancedDataMapType<Item, Map<String, SomeObject>, CustomRemover> someData = AdvancedDataMapType.builder(
                ResourceLocation.fromNamespaceAndPath(reg.modId(), "some_map"),
                Registries.ITEM, ExtraCodecs.strictUnboundedMap(Codec.STRING, SomeObject.CODEC))
                .merger(DataMapValueMerger.mapMerger())
                .remover(Codec.STRING.listOf().xmap(CustomRemover::new, CustomRemover::keys))
                .build();

        test.framework().modEventBus().addListener((final RegisterDataMapTypesEvent event) -> event.register(someData));

        final String subpackName = reg.registerSubpack("second_layer");

        reg.addProvider(event -> new DataMapProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void gather(HolderLookup.Provider provider) {
                builder(someData)
                        .add(Items.POTATO.builtInRegistryHolder(), Map.of(
                                "value1", new SomeObject(1, "a"),
                                "value2", new SomeObject(2, "b"),
                                "value3", new SomeObject(3, "c")), false);

                builder(someData)
                        .add(Items.SHEARS.builtInRegistryHolder(), Map.of(
                                "akey", new SomeObject(14, "abc")), false);
            }

            @Override
            public String getName() {
                return "generator 1";
            }
        });

        reg.addProvider(event -> new DataMapProvider(event.getGenerator().getPackOutput(subpackName), event.getLookupProvider()) {
            @Override
            protected void gather(HolderLookup.Provider provider) {
                builder(someData)
                        .remove(Items.POTATO.builtInRegistryHolder(), new CustomRemover(
                                List.of("value2")));

                builder(someData)
                        .remove(Items.SHEARS.builtInRegistryHolder());
            }

            @Override
            public String getName() {
                return "generator 2";
            }
        });

        test.onGameTest(helper -> {
            helper.assertTrue(Objects.equals(Items.POTATO.builtInRegistryHolder()
                    .getData(someData),
                    Map.of(
                            "value1", new SomeObject(1, "a"),
                            "value3", new SomeObject(3, "c"))),
                    "Remover didn't remove the map entries!");

            helper.assertTrue(Items.SHEARS.builtInRegistryHolder()
                    .getData(someData) == null, "Remove entry didn't remove the data!");

            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if registry data maps work")
    static void dataMapTest(final DynamicTest test, final RegistrationHelper reg) {
        final DataMapType<Item, SomeObject> someData = DataMapType.builder(
                ResourceLocation.fromNamespaceAndPath(reg.modId(), "some_data"),
                Registries.ITEM, SomeObject.CODEC)
                .synced(SomeObject.CODEC, true)
                .build();

        test.framework().modEventBus().addListener((final RegisterDataMapTypesEvent event) -> event.register(someData));

        reg.addProvider(event -> new DataMapProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void gather(HolderLookup.Provider provider) {
                builder(someData)
                        // Add to carrot and logs
                        .add(Items.CARROT.builtInRegistryHolder(), new SomeObject(14, "some_string"), false)
                        .add(ItemTags.LOGS, new SomeObject(156, "some other string"), false)

                        // But explicitly remove birch logs
                        .remove(Items.BIRCH_LOG.builtInRegistryHolder());
            }
        });

        // This is to make sure that sync work
        test.eventListeners().forge().addListener((final UseItemOnBlockEvent event) -> {
            if (event.getLevel().isClientSide() && event.getHand() == InteractionHand.MAIN_HAND) {
                event.getPlayer().displayClientMessage(Component.literal("Attachment value: " + event.getItemStack().getItemHolder()
                        .getData(someData)), true);
            }
        });

        test.onGameTest(helper -> {
            final Registry<Item> registry = helper.getLevel().registryAccess()
                    .lookupOrThrow(Registries.ITEM);
            helper.assertTrue(Objects.equals(registry.wrapAsHolder(Items.CARROT).getData(someData), new SomeObject(14, "some_string")), "Data wasn't attached to carrot!");

            // All logs but birch should have the value
            registry.getTagOrEmpty(ItemTags.LOGS)
                    .forEach(item -> {
                        if (item.value() != Items.BIRCH_LOG) {
                            helper.assertTrue(Objects.equals(item.getData(someData), new SomeObject(156, "some other string")), "Data wasn't attached to logs!");
                        }
                    });

            helper.assertTrue(registry.wrapAsHolder(Items.BIRCH_LOG).getData(someData) == null, "Data was attached to birch!");

            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if data maps can be successfully attached to dynamic registries")
    static void dynamicRegDataMaps(final DynamicTest test, final RegistrationHelper reg) {
        record ExperienceGrant(int amount) {
            static final Codec<ExperienceGrant> CODEC = Codec.INT.xmap(ExperienceGrant::new, ExperienceGrant::amount);
        }

        final DataMapType<DamageType, ExperienceGrant> xpGrant = reg.registerDataMap(DataMapType.builder(
                ResourceLocation.fromNamespaceAndPath(reg.modId(), "xp_grant"),
                Registries.DAMAGE_TYPE, ExperienceGrant.CODEC)
                .build());

        reg.addProvider(event -> new DataMapProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void gather(HolderLookup.Provider provider) {
                builder(xpGrant)
                        .add(DamageTypes.FELL_OUT_OF_WORLD, new ExperienceGrant(130), false);
            }
        });

        test.eventListeners().forge().addListener((final LivingDamageEvent.Post event) -> {
            final ExperienceGrant grant = event.getSource().typeHolder().getData(xpGrant);
            if (grant != null && event.getEntity() instanceof Player player) {
                player.giveExperiencePoints(grant.amount());
            }
        });

        test.onGameTest(helper -> {
            final Player player = helper.makeMockPlayer();
            player.hurt(helper.getLevel().damageSources().fellOutOfWorld(), 1f);
            helper.assertTrue(player.totalExperience == 130, "Player didn't receive experience");
            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if custom compostables work")
    static void compostablesMapTest(final DynamicTest test, final RegistrationHelper reg) {
        reg.addProvider(event -> new DataMapProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void gather(HolderLookup.Provider provider) {
                builder(NeoForgeDataMaps.COMPOSTABLES)
                        .add(ItemTags.COMPASSES, new Compostable(1f), false);
            }
        });
        test.onGameTest(helper -> helper.startSequence(helper::makeMockPlayer)
                .thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.COMPOSTER))
                .thenExecute(player -> helper.useBlock(
                        new BlockPos(1, 1, 1), player, Items.COMPASS.getDefaultInstance()))
                .thenExecute(() -> helper.assertBlockProperty(new BlockPos(1, 1, 1), ComposterBlock.LEVEL, 1))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the data map update event works", groups = EventTests.GROUP)
    static void dataMapUpdateEventTest(final DynamicTest test, final RegistrationHelper reg) {
        final DataMapType<Item, Integer> dataMap = reg.registerDataMap(DataMapType.builder(
                ResourceLocation.fromNamespaceAndPath(reg.modId(), "weight"),
                Registries.ITEM, Codec.INT)
                .build());
        reg.addProvider(event -> new DataMapProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void gather(HolderLookup.Provider provider) {
                builder(dataMap)
                        .add(Items.BLUE_ORCHID.builtInRegistryHolder(), 5, false)
                        .add(Items.OMINOUS_TRIAL_KEY.builtInRegistryHolder(), 10, false);
            }
        });

        final AtomicReference<List<WeightedEntry.Wrapper<Item>>> entries = new AtomicReference<>();
        NeoForge.EVENT_BUS.addListener((final DataMapsUpdatedEvent event) -> {
            if (event.getCause() == DataMapsUpdatedEvent.UpdateCause.SERVER_RELOAD) {
                event.ifRegistry(Registries.ITEM, items -> {
                    entries.set(items.getDataMap(dataMap).entrySet().stream()
                            .map(entry -> WeightedEntry.wrap(items.getValue(entry.getKey()), entry.getValue()))
                            .toList());
                });
            }
        });

        test.onGameTest(helper -> {
            helper.assertTrue(new HashSet<>(entries.get()).equals(Set.of(
                    WeightedEntry.wrap(Items.BLUE_ORCHID, 5),
                    WeightedEntry.wrap(Items.OMINOUS_TRIAL_KEY, 10))),
                    "Cached entries are not as expected");
            helper.succeed();
        });
    }

    /*
     * 1. Lightly Oxidized Iron should oxidize into More Oxidized Iron
     * 2. Lightly Oxidized Iron should wax into Lightly Oxidized Waxed Iron
     * 3. Lightly Oxidized Waxed Iron should scrape off into Lightly Oxidized Iron
     */
    @SuppressWarnings("DataFlowIssue")
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if existing and custom oxidizables and waxables work")
    static void oxidizablesAndWaxablesMapTest(final DynamicTest test, final RegistrationHelper reg) {
        BlockPos blockPos = new BlockPos(1, 1, 1);

        Holder<Block> lightlyOxidizedIron = reg.blocks().registerBlock("lightly_oxidized_iron", props -> new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.EXPOSED, props), BlockBehaviour.Properties.of());
        Holder<Block> moreOxidizedIron = reg.blocks().registerBlock("more_oxidized_iron", props -> new WeatheringCopperFullBlock(WeatheringCopper.WeatherState.WEATHERED, props), BlockBehaviour.Properties.of());

        Holder<Block> lightlyOxidizedWaxedIron = reg.blocks().registerBlock("lightly_oxidized_waxed_iron", Block::new, BlockBehaviour.Properties.of());

        reg.addProvider(event -> new DataMapProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void gather(HolderLookup.Provider provider) {
                builder(NeoForgeDataMaps.OXIDIZABLES)
                        .add(lightlyOxidizedIron, new Oxidizable(moreOxidizedIron.value()), false);

                builder(NeoForgeDataMaps.WAXABLES)
                        .add(lightlyOxidizedIron, new Waxable(lightlyOxidizedWaxedIron.value()), false);
            }
        });
        test.onGameTest(helper -> {
            helper.assertFalse(
                    DataMapHooks.didHaveToFallbackToVanillaMaps,
                    "The Oxidizable and Waxable Data Map's should not have to fallback to vanilla maps in this gametest, something is very wrong!");

            // -------------- Test added blocks -------------- \\
            // Test Lightly Oxidized Iron -> More Oxidized Iron
            helper.setBlock(blockPos, lightlyOxidizedIron.value());
            if (DataMapHooks.getNextOxidizedStage(lightlyOxidizedIron.value()) == null)
                helper.fail("Next oxidization state for lightly oxidized iron was null!");
            helper.setBlock(blockPos, DataMapHooks.getNextOxidizedStage(lightlyOxidizedIron.value()));
            helper.assertBlock(blockPos, block -> moreOxidizedIron.value().equals(block), "Wanted: More Oxidized Iron but found something else!");

            // Test Lightly Oxidized Iron -> Lightly Oxidized Waxed Iron
            helper.setBlock(blockPos, lightlyOxidizedIron.value());
            if (DataMapHooks.getBlockWaxed(lightlyOxidizedIron.value()) == null)
                helper.fail("Waxed state for lightly oxidized iron was null!");
            helper.setBlock(blockPos, DataMapHooks.getBlockWaxed(lightlyOxidizedIron.value()));
            helper.assertBlock(blockPos, block -> lightlyOxidizedWaxedIron.value().equals(block), "Wanted: Lightly Oxidized Waxed Iron but found something else!");

            // Test Lightly Oxidized Waxed Iron -> Lightly Oxidized Iron
            helper.useOn(blockPos, Items.IRON_AXE.getDefaultInstance(), helper.makeMockPlayer(), Direction.NORTH);
            helper.assertBlock(blockPos, block -> lightlyOxidizedIron.value().equals(block), "Wanted: Lightly Oxidized Iron but found something else!");

            // -------------- Test vanilla blocks -------------- \\
            // Test Block of Copper -> Exposed Copper
            helper.setBlock(blockPos, Blocks.COPPER_BLOCK);
            if (DataMapHooks.getNextOxidizedStage(Blocks.COPPER_BLOCK) == null)
                helper.fail("Next oxidization state for copper block was null!");
            helper.setBlock(blockPos, DataMapHooks.getNextOxidizedStage(Blocks.COPPER_BLOCK));
            helper.assertBlock(blockPos, Blocks.EXPOSED_COPPER::equals, "Wanted: Exposed Copper but found something else!");

            // Test Block of Copper -> Waxed Block of Copper
            helper.setBlock(blockPos, Blocks.COPPER_BLOCK);
            if (DataMapHooks.getBlockWaxed(Blocks.COPPER_BLOCK) == null)
                helper.fail("Waxed state for block of copper was null!");
            helper.setBlock(blockPos, DataMapHooks.getBlockWaxed(Blocks.COPPER_BLOCK));
            helper.assertBlock(blockPos, Blocks.WAXED_COPPER_BLOCK::equals, "Wanted: Waxed Copper of Block but found something else!");

            // Test Waxed Block of Copper -> Block of Copper
            helper.useOn(blockPos, Items.IRON_AXE.getDefaultInstance(), helper.makeMockPlayer(), Direction.NORTH);
            helper.assertBlock(blockPos, Blocks.COPPER_BLOCK::equals, "Wanted: Block of Copper but found something else!");

            // Test vanilla stuff
            WeatheringCopper.NEXT_BY_BLOCK.get().forEach((before, after) -> {
                helper.assertValueEqual(DataMapHooks.getNextOxidizedStage(before), after, "next oxidized stage of " + before.getName());
            });

            WeatheringCopper.PREVIOUS_BY_BLOCK.get().forEach((after, before) -> {
                helper.assertValueEqual(DataMapHooks.getPreviousOxidizedStage(after), before, "previous oxidized stage of " + before.getName());
            });

            HoneycombItem.WAXABLES.get().forEach((before, after) -> {
                helper.assertValueEqual(DataMapHooks.getBlockWaxed(before), after, "waxed version of " + before.getName());
            });

            HoneycombItem.WAX_OFF_BY_BLOCK.get().forEach((after, before) -> {
                helper.assertValueEqual(DataMapHooks.getBlockUnwaxed(after), before, "unwaxed version of " + before.getName());
            });

            helper.succeed();
        });
    }

    public record SomeObject(
            int intValue,
            String stringValue) {
        public static final Codec<SomeObject> CODEC = RecordCodecBuilder.create(in -> in.group(
                Codec.INT.fieldOf("intValue").forGetter(SomeObject::intValue),
                Codec.STRING.fieldOf("stringValue").forGetter(SomeObject::stringValue)).apply(in, SomeObject::new));
    }
}
