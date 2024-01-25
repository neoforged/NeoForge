/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueMerger;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover.Default;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
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
        final AdvancedDataMapType<List<SomeObject>, Item, Default<List<SomeObject>, Item>> someData = AdvancedDataMapType.builder(
                new ResourceLocation(reg.modId(), "some_list"),
                Registries.ITEM, SomeObject.CODEC.listOf())
                .merger(DataMapValueMerger.listMerger())
                .build();

        test.framework().modEventBus().addListener((final RegisterDataMapTypesEvent event) -> event.register(someData));

        final String subpackName = reg.registerSubpack("second_layer");

        reg.addProvider(event -> new DataMapProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void gather() {
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
            protected void gather() {
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
        record CustomRemover(List<String> keys) implements DataMapValueRemover<Map<String, SomeObject>, Item> {
            @Override
            public Optional<Map<String, SomeObject>> remove(Map<String, SomeObject> value, Registry<Item> registry, Either<TagKey<Item>, ResourceKey<Item>> source, Item object) {
                final var newMap = new HashMap<>(value);
                keys.forEach(newMap::remove);
                return Optional.of(newMap);
            }
        }

        final AdvancedDataMapType<Map<String, SomeObject>, Item, CustomRemover> someData = AdvancedDataMapType.builder(
                new ResourceLocation(reg.modId(), "some_map"),
                Registries.ITEM, ExtraCodecs.strictUnboundedMap(Codec.STRING, SomeObject.CODEC))
                .merger(DataMapValueMerger.mapMerger())
                .remover(Codec.STRING.listOf().xmap(CustomRemover::new, CustomRemover::keys))
                .build();

        test.framework().modEventBus().addListener((final RegisterDataMapTypesEvent event) -> event.register(someData));

        final String subpackName = reg.registerSubpack("second_layer");

        reg.addProvider(event -> new DataMapProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void gather() {
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
            protected void gather() {
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
        final DataMapType<SomeObject, Item> someData = DataMapType.builder(
                new ResourceLocation(reg.modId(), "some_data"),
                Registries.ITEM, SomeObject.CODEC)
                .synced(SomeObject.CODEC, true)
                .build();

        test.framework().modEventBus().addListener((final RegisterDataMapTypesEvent event) -> event.register(someData));

        reg.addProvider(event -> new DataMapProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void gather() {
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
                event.getEntity().displayClientMessage(Component.literal("Attachment value: " + event.getItemStack().getItemHolder()
                        .getData(someData)), true);
            }
        });

        test.onGameTest(helper -> {
            final Registry<Item> registry = helper.getLevel().registryAccess()
                    .registryOrThrow(Registries.ITEM);
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

        final DataMapType<ExperienceGrant, DamageType> xpGrant = DataMapType.builder(
                new ResourceLocation(reg.modId(), "xp_grant"),
                Registries.DAMAGE_TYPE, ExperienceGrant.CODEC)
                .build();

        test.framework().modEventBus().addListener((final RegisterDataMapTypesEvent event) -> event.register(xpGrant));

        reg.addProvider(event -> new DataMapProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void gather() {
                builder(xpGrant)
                        .add(DamageTypes.FELL_OUT_OF_WORLD, new ExperienceGrant(130), false);
            }
        });

        test.eventListeners().forge().addListener((final LivingDamageEvent event) -> {
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

    public record SomeObject(
            int intValue,
            String stringValue) {
        public static final Codec<SomeObject> CODEC = RecordCodecBuilder.create(in -> in.group(
                Codec.INT.fieldOf("intValue").forGetter(SomeObject::intValue),
                Codec.STRING.fieldOf("stringValue").forGetter(SomeObject::stringValue)).apply(in, SomeObject::new));
    }
}
