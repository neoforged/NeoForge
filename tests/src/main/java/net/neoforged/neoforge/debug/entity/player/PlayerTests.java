/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.player;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.debug.entity.EntityTests;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = PlayerTests.GROUP)
public class PlayerTests {
    public static final String GROUP = EntityTests.GROUP + ".player";

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the creative flight attribute lets a survival player fly while holding an item that grants it")
    static void creativeFlightAttribute(final DynamicTest test) {
        test.onGameTest(helper -> {
            ItemStack flightStick = new ItemStack(Items.STICK);
            flightStick.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                    .add(NeoForgeMod.CREATIVE_FLIGHT, new AttributeModifier(Identifier.fromNamespaceAndPath(test.createModId(), "flight"), 1.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .build());

            helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                    .thenExecute(player -> helper.assertFalse(player.mayFly(), "Expected a survival player not to be allowed to fly"))
                    .thenExecute(player -> player.setItemInHand(InteractionHand.MAIN_HAND, flightStick))
                    // The attributes of the held item are applied on the next tick
                    .thenExecuteAfter(1, player -> helper.assertTrue(player.mayFly(), "Expected the attribute to allow flying"))
                    .thenExecute(player -> {
                        // As if the client had started flying
                        Abilities abilities = new Abilities();
                        abilities.flying = true;
                        player.connection.handlePlayerAbilities(new ServerboundPlayerAbilitiesPacket(abilities));
                        helper.assertTrue(player.getAbilities().flying, "Expected the player to be able to start flying");
                    })
                    .thenExecute(player -> player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY))
                    .thenExecuteAfter(2, player -> {
                        helper.assertFalse(player.mayFly(), "Expected the player not to be allowed to fly without the item");
                        helper.assertFalse(player.getAbilities().flying, "Expected the player to stop flying without the item");
                    })
                    .thenSucceed();
        });
    }
}
