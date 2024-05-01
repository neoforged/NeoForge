/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.potion;

import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public class PotionBucketItem extends Item {
    public PotionBucketItem(Properties properties) {
        super(properties);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        var potion = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
        return Potion.getName(potion, "%s.effect.".formatted(Items.POTION.getDescriptionId()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> tooltipLines, TooltipFlag tooltipFlag) {
        var contents = stack.get(DataComponents.POTION_CONTENTS);

        if (contents != null)
            contents.addPotionTooltip(tooltipLines::add, 1F, tooltipContext.tickRate());
    }

    @Override
    public ItemStack getDefaultInstance() {
        return PotionContents.createItemStack(this, Potions.WATER);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        var player = entity instanceof Player plr ? plr : null;

        if (player instanceof ServerPlayer sPlayer)
            CriteriaTriggers.CONSUME_ITEM.trigger(sPlayer, stack);

        if (!level.isClientSide) {
            stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).forEachEffect(effectInstance -> {
                var effect = effectInstance.getEffect().value();

                if (effect.isInstantenous())
                    effect.applyInstantenousEffect(player, player, entity, effectInstance.getAmplifier(), 1D);
                else
                    entity.addEffect(effectInstance);
            });
        }

        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
            stack.consume(1, player);
        }

        if (player == null || !player.hasInfiniteMaterials()) {
            if (!stack.isEmpty())
                return new ItemStack(Items.BUCKET);
            if (player != null)
                player.getInventory().add(new ItemStack(Items.BUCKET));
        }

        entity.gameEvent(GameEvent.DRINK);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    public static ICapabilityProvider<ItemStack, Void, IFluidHandlerItem> capabilityProvider(ItemLike emptyContainer, int capacity) {
        return (stack, $) -> new PotionFluidHandlerItem(stack, emptyContainer, capacity);
    }
}
