/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.ToolAction;
import net.neoforged.neoforge.common.ToolActions;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod("custom_shield_test")
public class CustomShieldTest {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("custom_shield_test");

    private static final DeferredItem<CustomShieldItem> CUSTOM_SHIELD_ITEM = ITEMS.register("custom_shield",
            () -> new CustomShieldItem((new Item.Properties()).durability(336)));

    public CustomShieldTest() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modBus);
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT)
            event.accept(CUSTOM_SHIELD_ITEM);
    }

    private static class CustomShieldItem extends Item {
        public CustomShieldItem(Properties properties) {
            super(properties);
        }

        @Override
        public UseAnim getUseAnimation(ItemStack stack) {
            return UseAnim.BLOCK;
        }

        @Override
        public int getUseDuration(ItemStack stack) {
            return 72000;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
            ItemStack itemstack = player.getItemInHand(hand);
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        }

        @Override
        public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
            return toolAction == ToolActions.SHIELD_BLOCK;
        }
    }
}
