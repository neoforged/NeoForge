/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.player;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.BlockInteractEvent;
import org.slf4j.Logger;

/**
 * Test mod for the BlockInteractEvent.
 *
 * When the player uses an item targeting a block,
 * each phase of the event is logged on both client and server threads.
 *
 * Additionally, attempting to sneak-place a dirt block on top of a dispenser block will
 * prevent the placement (but will not prevent opening the chest when not sneaking).
 */
@Mod(BlockInteractEventTest.MODID)
public class BlockInteractEventTest
{
    public static final String MODID = "block_interact_event_test";
    private static final Logger LOGGER = LogUtils.getLogger();

    public BlockInteractEventTest()
    {
        NeoForge.EVENT_BUS.addListener(this::onUseItemOnBlock);
    }

    private void onUseItemOnBlock(BlockInteractEvent event)
    {
        UseOnContext context = event.getUseOnContext();
        Level level = context.getLevel();
        LOGGER.info("phase={}; hand={}; isClient={}", event.getUsePhase(), event.getUseOnContext().getHand(), level.isClientSide);
        // cancel item logic if dirt is placed on top of grass
        if (event.getUsePhase() == BlockInteractEvent.UsePhase.ITEM_AFTER_BLOCK)
        {
            ItemStack stack = context.getItemInHand();
            Item item = stack.getItem();
            if (item instanceof BlockItem blockItem && blockItem.getBlock() == Blocks.DIRT)
            {
                BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());
                if (level.getBlockState(placePos.below()).getBlock() == Blocks.DISPENSER)
                {
                    if (!level.isClientSide)
                    {
                        context.getPlayer().displayClientMessage(Component.literal("Can't place dirt on dispenser"), false);
                    }
                    event.cancelWithResult(InteractionResult.SUCCESS);
                }
            }
        }
    }
}