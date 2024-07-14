/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = PlayerTests.GROUP + ".advancement")
public class AdvancementTests {
    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the advancement earn event is fired", groups = "event")
    static void playerAdvancementEarn(final DynamicTest test) {
        test.eventListeners().forge().addListener((final AdvancementEvent.AdvancementEarnEvent event) -> {
            if (event.getAdvancement().id().equals(ResourceLocation.withDefaultNamespace("story/root")) && event.getEntity() instanceof ServerPlayer player) {
                player.getAdvancements().award(
                        Objects.requireNonNull(player.server.getAdvancements().get(ResourceLocation.withDefaultNamespace("story/mine_stone"))),
                        "get_stone");
            }
            test.pass();
        });

        test.onGameTest(helper -> {
            final ServerPlayer player = helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL);
            helper.startSequence()
                    .thenExecute(() -> player.getInventory().add(Items.CRAFTING_TABLE.getDefaultInstance()))
                    .thenExecuteAfter(5, () -> helper.assertTrue(
                            player.getAdvancements().getOrStartProgress(player.server.getAdvancements().get(ResourceLocation.withDefaultNamespace("story/mine_stone"))).isDone(),
                            "Player did not receive advancement"))
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the advancement progress event is fired", groups = "event")
    static void playerAdvancementProgress(final DynamicTest test) {
        test.eventListeners().forge().addListener((final AdvancementEvent.AdvancementProgressEvent event) -> {
            if (event.getAdvancement().id().equals(ResourceLocation.withDefaultNamespace("story/obtain_armor")) && event.getProgressType() == AdvancementEvent.AdvancementProgressEvent.ProgressType.GRANT && event.getEntity() instanceof ServerPlayer player) {
                player.getAdvancements().getOrStartProgress(event.getAdvancement())
                        .getRemainingCriteria().forEach(criteria -> player.getAdvancements().award(event.getAdvancement(), criteria));
            }
            test.pass();
        });

        test.onGameTest(helper -> {
            final ServerPlayer player = helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL);
            helper.startSequence()
                    .thenExecute(() -> player.getInventory().add(Items.IRON_HELMET.getDefaultInstance()))
                    .thenExecuteAfter(5, () -> helper.assertTrue(
                            player.getAdvancements().getOrStartProgress(player.server.getAdvancements().get(ResourceLocation.withDefaultNamespace("story/obtain_armor"))).isDone(),
                            "Player did not complete advancement"))
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if custom advancement predicates work")
    static void customPredicateTest(final DynamicTest test, final RegistrationHelper reg) {
        ItemSubPredicate.Type<CustomNamePredicate> type = new ItemSubPredicate.Type<>(RecordCodecBuilder.create(g -> g.group(
                Codec.INT.fieldOf("data1").forGetter(CustomNamePredicate::data1),
                Codec.INT.fieldOf("data2").forGetter(CustomNamePredicate::data2))
                .apply(g, CustomNamePredicate::new)));

        reg.registrar(Registries.ITEM_SUB_PREDICATE_TYPE)
                .register("custom_name", () -> type);

        reg.addProvider(event -> new AdvancementProvider(
                event.getGenerator().getPackOutput(),
                event.getLookupProvider(),
                event.getExistingFileHelper(),
                List.of((registries, saver, existingFileHelper) -> {
                    Advancement.Builder.advancement()
                            .parent(ResourceLocation.withDefaultNamespace("story/root"))
                            .display(Items.ANVIL, Component.literal("Named!"), Component.literal("Get a named item"), null, AdvancementType.TASK, true, true, false)
                            .addCriterion("has_named_item", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().withSubPredicate(type, new CustomNamePredicate(1, 2))))
                            .save(saver, ResourceLocation.fromNamespaceAndPath(reg.modId(), "named_item"), existingFileHelper);
                })));

        test.onGameTest(helper -> {
            final ServerPlayer player = helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL);
            helper.startSequence()
                    .thenExecute(() -> {
                        ItemStack stack = Items.IRON_HELMET.getDefaultInstance();
                        stack.set(DataComponents.CUSTOM_NAME, Component.literal("abcd"));
                        player.getInventory().add(stack);
                    })
                    .thenExecuteAfter(5, () -> helper.assertTrue(
                            player.getAdvancements().getOrStartProgress(player.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath(reg.modId(), "named_item"))).isDone(),
                            "Player did not complete advancement"))
                    .thenSucceed();
        });
    }

    public record CustomNamePredicate(int data1, int data2) implements ItemSubPredicate {
        @Override
        public boolean matches(ItemStack itemStack) {
            return itemStack.has(DataComponents.CUSTOM_NAME);
        }
    }
}
