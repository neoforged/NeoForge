/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("potion_event_test")
@EventBusSubscriber
public class PotionEventTest {
    private static final Logger LOGGER = LogManager.getLogger(PotionEventTest.class);

    @SubscribeEvent
    public static void onBrewingSetup(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addRecipe(Ingredient.of(Items.ICE), Ingredient.of(Items.LAVA_BUCKET), new ItemStack(Items.OBSIDIAN));
    }

    @SubscribeEvent
    public static void onPotionAdded(MobEffectEvent.Added event) {
        if (!event.getEntity().getCommandSenderWorld().isClientSide)
            LOGGER.info("{} has a new PotionEffect {} from {}, the old one was {}", event.getEntity(), event.getEffectInstance(), event.getEffectSource(), event.getOldEffectInstance());
    }

    @SubscribeEvent
    public static void isPotionApplicable(MobEffectEvent.Applicable event) {
        if (!event.getEntity().getCommandSenderWorld().isClientSide) {
            event.setResult(Applicable.Result.APPLY);
            LOGGER.info("Allowed Potion {} for Entity {}", event.getEffectInstance(), event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPotionRemove(MobEffectEvent.Remove event) {
        if (!event.getEntity().getCommandSenderWorld().isClientSide)
            LOGGER.info("Effect {} got Removed from {}", event.getEffectInstance(), event.getEntity());
    }

    @SubscribeEvent
    public static void onPotionExpiry(MobEffectEvent.Expired event) {
        if (!event.getEntity().getCommandSenderWorld().isClientSide)
            LOGGER.info("Effect {} expired from {}", event.getEffectInstance(), event.getEntity());
    }
}
