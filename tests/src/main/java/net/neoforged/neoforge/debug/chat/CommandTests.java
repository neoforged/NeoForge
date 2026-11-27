/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.chat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.ParsedCommandNode;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "chat.command")
public class CommandTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = { "Tests if the command event works", "Redirects /attribute with no arguments to effect" })
    static void commandEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final CommandEvent event) -> {
            CommandDispatcher<CommandSourceStack> dispatcher = event.getParseResults().getContext().getDispatcher();
            List<ParsedCommandNode<CommandSourceStack>> nodes = event.getParseResults().getContext().getNodes();
            CommandSourceStack source = event.getParseResults().getContext().getSource();

            // test: when the /attribute command is used with no arguments, automatically give effect
            if (nodes.size() == 1 && nodes.get(0).getNode() == dispatcher.getRoot().getChild("attribute")) {
                event.setParseResults(dispatcher.parse("effect give @s minecraft:blindness 10 2", source));
                test.pass();
            }
        });

        test.onGameTest(helper -> {
            final ServerPlayer player = helper.makeOpMockPlayer(LevelBasedPermissionSet.GAMEMASTER);
            helper.startSequence()
                    .thenExecute(() -> helper.getLevel().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), "/attribute"))
                    .thenExecuteAfter(5, () -> helper.assertLivingEntityHasMobEffect(player, MobEffects.BLINDNESS, 2))
                    .thenSucceed();
        });
    }
}
