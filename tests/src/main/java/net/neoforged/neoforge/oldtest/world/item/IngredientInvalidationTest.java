/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.world.item;

import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * This class validates that {@link Ingredient#invalidate()} is called correctly.<br>
 * To verify, join a world, then leave it. Then join a world again in the same game session. If invalidation is not working,
 * the 2nd world join will trigger an exception.
 */
@Mod(IngredientInvalidationTest.MOD_ID)
public class IngredientInvalidationTest {
    public static final String MOD_ID = "ingredient_invalidation";

    private static final boolean ENABLED = true;

    private static boolean invalidateExpected = false;
    private static boolean gotInvalidate = false;

    private static final Ingredient TEST_INGREDIENT = new Ingredient(Stream.of(new Ingredient.ItemValue(new ItemStack(Items.WHEAT)))) {
        // TODO: 
        /*@Override
        protected void invalidate()
        {
            super.invalidate();
            gotInvalidate = true;
        }*/
    };

    public IngredientInvalidationTest() {
        if (!ENABLED)
            return;

        NeoForge.EVENT_BUS.addListener(IngredientInvalidationTest::worldLoad);
    }

    private static void worldLoad(LevelEvent.Load event) {
        /*if (event.getLevel() instanceof ServerLevel level && level.dimension().equals(Level.OVERWORLD))
        {
            TEST_INGREDIENT.getStackingIds(); // force invalidation if necessary
            if (!invalidateExpected)
            {
                invalidateExpected = true;
            }
            else if (!gotInvalidate)
            {
                throw new IllegalStateException("Ingredient.invalidate was not called.");
            }
            else
            {
                gotInvalidate = false;
            }
        }*/
    }
}
