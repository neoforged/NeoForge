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

        // register various elements which require our flag
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

        // generate recipe which requires our flag
        // TODO: Figure out why this causes duplicate provider exceptions
        // Seems to clash with recipe provider in DataGeneratorTest -> gen.addProvider(event.includeServer(), new Recipes(packOutput, lookupProvider));
        // IngredientTests does the same thing using addProvider which works fine, but here causes dupe providers, why?!?
        // Things I have tried
        // - Renaming method to change mod id
        // - Adding @GameTest and @EmptyTemplate to match IngredientTests
        // - Making groups in @ForEachTests dotted
        // - Passing RegistrationHelper as parameter
        // - Creating new RegistrationHelper staticly (and registering with @OnInit) and using that rather than one from DynamicTest
        // - Passing new mod id to test.registrationHelper()
        // - Changing value in @TestHolder for new mod id
        // - Setting idPrefix in @ForEachTest
        /*registration.addProvider(event -> event.getGenerator().addProvider(event.includeServer(), new RecipeProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void buildRecipes(RecipeOutput output, HolderLookup.Provider provider) {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.DIAMOND, 64)
                        .requires(ItemTags.DIRT)
                        .unlockedBy("has_dirt", has(ItemTags.DIRT))
                        .save(output.withConditions(new RequiredFlagsCondition(testFlag)), ResourceLocation.fromNamespaceAndPath(namespace, "flagged/diamonds_from_dirt"));
            }
        }));*/
    }

    final class DummyEntity extends Mob {
        DummyEntity(EntityType<? extends Mob> entityType, Level level) {
            super(entityType, level);
        }
    }
}
