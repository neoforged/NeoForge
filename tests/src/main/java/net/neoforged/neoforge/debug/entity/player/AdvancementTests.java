/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.advancements.critereon.ICustomItemPredicate;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
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
            if (event.getAdvancement().id().equals(new ResourceLocation("story/root")) && event.getEntity() instanceof ServerPlayer player) {
                player.getAdvancements().award(
                        Objects.requireNonNull(player.server.getAdvancements().get(new ResourceLocation("story/mine_stone"))),
                        "get_stone");
            }
            test.pass();
        });

        test.onGameTest(helper -> {
            final ServerPlayer player = helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL);
            helper.startSequence()
                    .thenExecute(() -> player.getInventory().add(Items.CRAFTING_TABLE.getDefaultInstance()))
                    .thenExecuteAfter(5, () -> helper.assertTrue(
                            player.getAdvancements().getOrStartProgress(player.server.getAdvancements().get(new ResourceLocation("story/mine_stone"))).isDone(),
                            "Player did not receive advancement"))
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the advancement progress event is fired", groups = "event")
    static void playerAdvancementProgress(final DynamicTest test) {
        test.eventListeners().forge().addListener((final AdvancementEvent.AdvancementProgressEvent event) -> {
            if (event.getAdvancement().id().equals(new ResourceLocation("story/obtain_armor")) && event.getProgressType() == AdvancementEvent.AdvancementProgressEvent.ProgressType.GRANT && event.getEntity() instanceof ServerPlayer player) {
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
                            player.getAdvancements().getOrStartProgress(player.server.getAdvancements().get(new ResourceLocation("story/obtain_armor"))).isDone(),
                            "Player did not complete advancement"))
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if custom advancement predicates work")
    static void customPredicateTest(final DynamicTest test, final RegistrationHelper reg) {
        final AtomicReference<Supplier<Codec<? extends ICustomItemPredicate>>> serializer = new AtomicReference<>();
        serializer.set(reg.registrar(NeoForgeRegistries.Keys.ITEM_PREDICATE_SERIALIZERS)
                .register("custom_name", () -> RecordCodecBuilder.<CustomNamePredicate>create(g -> g.group(
                        Codec.INT.fieldOf("data1").forGetter(CustomNamePredicate::data1),
                        Codec.INT.fieldOf("data2").forGetter(CustomNamePredicate::data2))
                        .apply(g, (d1, d2) -> new CustomNamePredicate(d1, d2, serializer.get())))));

        reg.addProvider(event -> new AdvancementProvider(
                event.getGenerator().getPackOutput(),
                event.getLookupProvider(),
                event.getExistingFileHelper(),
                List.of((registries, saver, existingFileHelper) -> {
                    Advancement.Builder.advancement()
                            .parent(new ResourceLocation("story/root"))
                            .display(Items.ANVIL, Component.literal("Named!"), Component.literal("Get a named item"), null, FrameType.TASK, true, true, false)
                            .addCriterion("has_named_item", InventoryChangeTrigger.TriggerInstance.hasItems(new CustomNamePredicate(1, 2, serializer.get()).toVanilla()))
                            .save(saver, new ResourceLocation(reg.modId(), "named_item"), existingFileHelper);
                })));

        test.onGameTest(helper -> {
            final ServerPlayer player = helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL);
            helper.startSequence()
                    .thenExecute(() -> player.getInventory().add(Items.IRON_HELMET.getDefaultInstance().setHoverName(Component.literal("abcd"))))
                    .thenExecuteAfter(5, () -> helper.assertTrue(
                            player.getAdvancements().getOrStartProgress(player.server.getAdvancements().get(new ResourceLocation(reg.modId(), "named_item"))).isDone(),
                            "Player did not complete advancement"))
                    .thenSucceed();
        });
    }

    public record CustomNamePredicate(int data1, int data2, Supplier<Codec<? extends ICustomItemPredicate>> pred) implements ICustomItemPredicate {
        @Override
        public Codec<? extends ICustomItemPredicate> codec() {
            return pred.get();
        }

        @Override
        public boolean test(ItemStack itemStack) {
            return itemStack.hasCustomHoverName();
        }
    }
}
