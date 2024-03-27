/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.item.ItemPickupAllowedEvent;
import net.neoforged.neoforge.event.entity.item.ItemPickupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Basic test mod to test the new item pickup events {@linkplain ItemPickupAllowedEvent} and {@linkplain ItemPickupEvent}.
 * <p>
 * When this test is enabled the following should be true
 * <ul>
 * <li>Players should not be allowed to pickup any item if they are sneaking.</li>
 * <li>Hoppers should not be allowed to pickup {@linkplain Tags.Items#GEMS_DIAMOND}</li>
 *
 * <li>Mobs should be forcefully allowed to pickup items when standing on {@linkplain Blocks#STONE}</li>
 * <li>Mobs should not be allowed to pickup items when standing on {@linkplain Blocks#DIAMOND_BLOCK}</li>
 * </ul>
 */
@Mod(DisallowItemPickupTest.ID)
public final class DisallowItemPickupTest {
    public static final String ID = "disallow_item_pickup_test";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean ENABLED = false;
    // set to true to log every item pick up, where it happened and who picked it up
    private static final boolean VERBOSE = false;

    public DisallowItemPickupTest() {
        NeoForge.EVENT_BUS.addListener(ItemPickupAllowedEvent.ByPlayer.class, event -> {
            if (ENABLED && event.getTarget().isShiftKeyDown()) {
                event.setResult(Event.Result.DENY);
            }
        });

        NeoForge.EVENT_BUS.addListener(ItemPickupAllowedEvent.ByHopper.class, event -> {
            if (ENABLED && event.getEntity().getItem().is(Tags.Items.GEMS_DIAMOND))
                event.setResult(Event.Result.DENY);
        });

        NeoForge.EVENT_BUS.addListener(ItemPickupAllowedEvent.ByMob.class, event -> {
            if (!ENABLED)
                return;

            var blockState = event.getTarget().level().getBlockState(event.getTarget().blockPosition().below());

            if (blockState.is(Blocks.STONE))
                event.setResult(Event.Result.ALLOW);
            else if (blockState.is(Blocks.DIAMOND_BLOCK))
                event.setResult(Event.Result.DENY);
        });

        NeoForge.EVENT_BUS.addListener(ItemPickupEvent.ByPlayer.class, event -> {
            if (!ENABLED || !VERBOSE)
                return;

            LOGGER.info("Item '{}' picked up by Player '{} ({}/{})'",
                    event.getStack().getHoverName().getString(),
                    event.getTarget().getScoreboardName(),
                    event.getTarget().blockPosition().toShortString(),
                    event.getTarget().level().dimension().location());
        });

        NeoForge.EVENT_BUS.addListener(ItemPickupEvent.ByHopper.class, event -> {
            if (!ENABLED || !VERBOSE)
                return;

            LOGGER.info("Item '{}' picked up by Hopper '{}, {}, {}'",
                    event.getStack().getHoverName().getString(),
                    event.getTarget().getLevelX(),
                    event.getTarget().getLevelY(),
                    event.getTarget().getLevelZ());
        });

        NeoForge.EVENT_BUS.addListener(ItemPickupEvent.ByMob.class, event -> {
            if (!ENABLED || !VERBOSE)
                return;

            LOGGER.info("Item '{}' picked up by Mob '{} ({}/{})'",
                    event.getStack().getHoverName().getString(),
                    event.getTarget().getScoreboardName(),
                    event.getTarget().blockPosition().toShortString(),
                    event.getTarget().level().dimension().location());
        });
    }
}
