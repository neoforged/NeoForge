/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.flag.Flag;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(groups = "modded_feature_flags")
public interface FlagTests {
    @TestHolder(description = "Tests modded feature flags")
    static void test(DynamicTest test) {
        var namespace = test.createModId();
        var registration = test.registrationHelper();
        var items = registration.items();

        var testFlag = Flag.of(namespace, "test_flag");

        items.registerSimpleItem("flagged_item", new Item.Properties().requiredFlags(testFlag));

        // block disabled via matching block item
        var flaggedBlock = registration.blocks().registerSimpleBlock("flagged_block", BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).requiredFlags(testFlag));
        items.registerSimpleBlockItem(flaggedBlock);

        // spawn egg disabled via matching entity type
        var flaggedEntity = registration.entityTypes().registerType("flagged_entity", () -> EntityType.Builder
                .of(DummyEntity::new, MobCategory.MISC)
                .requiredFlags(testFlag)).withRenderer(() -> NoopRenderer::new).withAttributes(Mob::createMobAttributes);

        items.registerItem("flagged_entity_egg", properties -> new DeferredSpawnEggItem(flaggedEntity, 0, 0, properties));
    }

    final class DummyEntity extends Mob {
        DummyEntity(EntityType<? extends Mob> entityType, Level level) {
            super(entityType, level);
        }
    }
}
