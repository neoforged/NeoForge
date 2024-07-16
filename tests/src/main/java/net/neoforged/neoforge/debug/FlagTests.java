/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.flag.FlagElement;
import net.neoforged.neoforge.flag.FlagProvider;
import net.neoforged.neoforge.flag.Flags;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

@ForEachTest(groups = "modded_feature_flags")
public interface FlagTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Registers custom modded flag and items which requires it")
    static void test(DynamicTest test) {
        var registration = test.registrationHelper();
        var modId = test.createModId();
        var testFlag = ResourceLocation.fromNamespaceAndPath(modId, "test_flag");

        registration.addProvider(event -> new FlagProvider(event.getGenerator().getPackOutput(), modId, event.getLookupProvider()) {
            @Override
            protected void generate() {
                flag(testFlag);
            }
        });

        var items = registration.items();

        var item = items.registerSimpleItem("flagged_item", new Item.Properties()
                .requiredFlags(testFlag));

        var block = registration.blocks().registerSimpleBlock("flagged_block", BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                .requiredFlags(testFlag));
        // should be disabled with block
        var blockItem = items.registerSimpleBlockItem(block);

        class DummyEntity extends Mob {
            private DummyEntity(EntityType<DummyEntity> entityType, Level level) {
                super(entityType, level);
            }
        }

        var entityType = registration.entityTypes().registerType("flagged_entity", () -> EntityType.Builder
                .of(DummyEntity::new, MobCategory.MISC)
                .sized(1F, 1F)
                .requiredFlags(testFlag))
                .withAttributes(Mob::createMobAttributes)
                .withRenderer(() -> NoopRenderer::new);
        // should be disabled with entity type
        var spawnEgg = items.registerItem("flagged_entity_egg", properties -> new DeferredSpawnEggItem(
                entityType,
                0,
                0,
                properties));

        BiConsumer<ExtendedGameTestHelper, Boolean> testFlagState = (helper, enabled) -> {
            var wasEnabled = Flags.isEnabled(testFlag);
            // set flag to expected state
            setFlag(helper, testFlag, enabled);

            // validate flag state matches expected
            var isEnabled = Flags.isEnabled(testFlag);
            helper.assertValueEqual(
                    isEnabled,
                    enabled,
                    "Flag[" + testFlag + "] is " +
                            state(isEnabled) + " when it should be " +
                            state(enabled));

            // validate all flagged elements match expected state
            testFlagElement(helper, item, testFlag, enabled);

            testFlagElement(helper, block, testFlag, enabled);
            testFlagElement(helper, blockItem, testFlag, enabled);

            testFlagElement(helper, entityType, testFlag, enabled);
            testFlagElement(helper, spawnEgg, testFlag, enabled);

            // reset flag back to what it was
            setFlag(helper, testFlag, wasEnabled);
        };

        test.onGameTest(helper -> {
            testFlagState.accept(helper, true);
            testFlagState.accept(helper, false);
            helper.succeed();
        });
    }

    private static <T extends FlagElement, R extends T> void testFlagElement(ExtendedGameTestHelper test, DeferredHolder<T, R> holder, ResourceLocation flag, boolean enabled) {
        var elementEnabled = holder.value().isEnabled();
        test.assertValueEqual(
                elementEnabled,
                enabled,
                "Element[" + holder.getId() + "] is " +
                        state(elementEnabled) + " when it should be " +
                        state(enabled));
    }

    private static void setFlag(ExtendedGameTestHelper test, ResourceLocation flag, boolean enabled) {
        if (Flags.isEnabled(flag) == enabled)
            return;

        var type = enabled ? "enable" : "disable";
        var command = "neoforge flag " + type + " " + flag;

        try {
            var server = test.getLevel().getServer();
            var sender = server.createCommandSourceStack();
            var dispatcher = server.getCommands().getDispatcher();
            var result = dispatcher.execute(command, sender);

            if (result != Command.SINGLE_SUCCESS)
                throw new GameTestAssertException("Failed execute command '" + command + "' (Error code: " + result + ")");
        } catch (CommandSyntaxException e) {
            throw new GameTestAssertException("Failed execute command '" + command + "' (" + e.getMessage() + ")");
        }
    }

    private static String state(boolean enabled) {
        return enabled ? "enabled" : "disabled";
    }
}
