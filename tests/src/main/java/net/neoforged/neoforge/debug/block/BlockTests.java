/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.block;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = BlockTests.GROUP)
public class BlockTests {
    public static final String GROUP = "level.block";

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if custom fence gates without wood types work, allowing for the use of the vanilla block for non-wooden gates")
    static void woodlessFenceGate(final DynamicTest test, final RegistrationHelper reg) {
        final var gate = reg.blocks().register("gate", () -> new FenceGateBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_FENCE_GATE), SoundEvents.BARREL_OPEN, SoundEvents.CHEST_CLOSE))
                .withLang("Woodless Fence Gate").withBlockItem();
        reg.provider(BlockStateProvider.class, prov -> prov.fenceGateBlock(gate.get(), new ResourceLocation("block/iron_block")));
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(1, 1, 1, gate.get()))

                // Open gate
                .thenExecute(player -> helper.useBlock(new BlockPos(1, 1, 1)))
                .thenExecute(player -> helper.assertTrue(
                        player.getOutboundPackets(ClientboundSoundPacket.class)
                                .anyMatch(sound -> sound.getSound().value() == SoundEvents.BARREL_OPEN),
                        "Open sound was not broadcast"))

                // Close gate
                .thenExecute(player -> helper.pulseRedstone(1, 2, 1, 1))
                .thenExecute(player -> helper.assertTrue(
                        player.getOutboundPackets(ClientboundSoundPacket.class)
                                .anyMatch(sound -> sound.getSound().value() == SoundEvents.CHEST_CLOSE),
                        "Close sound was not broadcast"))
                .thenSucceed());
    }
}
