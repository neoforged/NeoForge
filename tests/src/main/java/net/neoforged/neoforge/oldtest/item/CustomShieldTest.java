/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod("custom_shield_test")
public class CustomShieldTest {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("custom_shield_test");

    private static final DeferredItem<CustomShieldItem> CUSTOM_SHIELD_ITEM = ITEMS.registerItem("custom_shield",
            props -> new CustomShieldItem(props.durability(336)));

    public CustomShieldTest(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT)
            event.accept(CUSTOM_SHIELD_ITEM);
    }

    private static class CustomShieldItem extends Item {
        public CustomShieldItem(Properties properties) {
            super(properties.component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                    0.25F,
                    1.0F,
                    java.util.List.of(new BlocksAttacks.DamageReduction(90.0F, java.util.Optional.empty(), 0.0F, 1.0F)),
                    new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                    java.util.Optional.of(net.minecraft.tags.DamageTypeTags.BYPASSES_SHIELD),
                    java.util.Optional.of(net.minecraft.sounds.SoundEvents.SHIELD_BLOCK),
                    java.util.Optional.of(net.minecraft.sounds.SoundEvents.SHIELD_BREAK))));
        }

        @Override
        public ItemUseAnimation getUseAnimation(ItemStack stack) {
            return ItemUseAnimation.BLOCK;
        }

        @Override
        public int getUseDuration(ItemStack stack, LivingEntity entity) {
            return 72000;
        }

        @Override
        public InteractionResult use(Level world, Player player, InteractionHand hand) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
    }
}
