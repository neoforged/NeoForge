/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.chat;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.ParsedCommandNode;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.server.command.EnumArgument;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

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
            final Player player = new Player(helper.getLevel(), BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "test-mock-player")) {
                @Override
                public boolean isSpectator() {
                    return false;
                }

                @Override
                public boolean isCreative() {
                    return false;
                }

                @Override
                public boolean isLocalPlayer() {
                    return true;
                }

                @Override
                protected int getPermissionLevel() {
                    return Commands.LEVEL_GAMEMASTERS;
                }
            };
            helper.startSequence()
                    .thenExecute(() -> helper.getLevel().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), "/attribute"))
                    .thenExecuteAfter(5, () -> helper.assertLivingEntityHasMobEffect(player, MobEffects.BLINDNESS, 2))
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = { "Tests if the forge enum argument works", "Adds a /enumargumenttest command with a single {NV, BLD} enum argument" })
    static void enumArgument(final DynamicTest test) {
        enum TestArgument {
            BLD {
                @Override
                public MobEffect getEffect() {
                    return MobEffects.BLINDNESS;
                }
            },
            NV {
                @Override
                public MobEffect getEffect() {
                    return MobEffects.NIGHT_VISION;
                }
            };

            public abstract MobEffect getEffect();
        }

        NeoForge.EVENT_BUS.addListener((final RegisterCommandsEvent event) -> {
            event.getDispatcher().register(Commands.literal("enumargumenttest")
                    .then(Commands.argument("enum", EnumArgument.enumArgument(TestArgument.class))
                            .executes(command -> {
                                ((LivingEntity) command.getSource().getEntityOrException())
                                        .addEffect(new MobEffectInstance(command.getArgument("enum", TestArgument.class).getEffect()));
                                test.pass();
                                return Command.SINGLE_SUCCESS;
                            })));
        });

        test.onGameTest(helper -> {
            final Player player = helper.makeMockPlayer();
            helper.startSequence()
                    .thenExecute(() -> helper.assertTrue(
                            helper.getLevel().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), "/enumargumenttest ABC") == 0,
                            "Invalid command was successfully executed"))
                    .thenExecute(() -> helper.assertTrue(
                            helper.getLevel().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), "/enumargumenttest NV") == 1,
                            "Valid command was not executed"))
                    .thenExecuteAfter(3, () -> helper.assertLivingEntityHasMobEffect(player, MobEffects.NIGHT_VISION, 0))
                    .thenSucceed();
        });
    }
}
