/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.chat;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.ParsedCommandNode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.server.command.EnumArgument;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import org.jetbrains.annotations.Nullable;

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

        test.onGameTest(helper -> helper.startSequence(helper::makeMockPlayer)
                .thenSequence((seq, player) -> seq
                        .thenMap(p -> ErrorCatchingStack.createCommandSourceStack(player.get(), Commands.LEVEL_ADMINS))
                        .thenExecute(stack -> helper.getLevel().getServer().getCommands().performPrefixedCommand(stack, "/enumargumenttest ABC"))
                        .thenIdle(5) // Keep in mind that if a command errors, we have both the "error" failure and the failure with the position of the error
                        .thenExecute(stack -> helper.assertTrue(stack.errors.size() == 2, "Invalid command was successfully executed"))
                        .thenExecute(stack -> helper.assertTrue(stack.errors.stream().map(Component::getString).toList().equals(List.of(
                                "Enum constant must be one of [BLD, NV], found ABC",
                                "...nttest ABC<--[HERE]")), "Errors were wrong"))

                        .thenExecute(stack -> helper.getLevel().getServer().getCommands().performPrefixedCommand(stack, "/enumargumenttest NV"))
                        .thenIdle(5)
                        .thenExecute(stack -> helper.assertTrue(stack.errors.size() == 2, "Valid command was not executed"))
                        .thenExecuteAfter(3, () -> helper.assertLivingEntityHasMobEffect(player.get(), MobEffects.NIGHT_VISION, 0))
                        .thenSucceed()));
    }

    public final static class ErrorCatchingStack extends CommandSourceStack {

        public static ErrorCatchingStack createCommandSourceStack(Player player, int perm) {
            return new ErrorCatchingStack(
                    player,
                    player.position(),
                    player.getRotationVector(),
                    player.level() instanceof ServerLevel ? (ServerLevel) player.level() : null,
                    perm,
                    player.getName().getString(),
                    player.getDisplayName(),
                    player.level().getServer(),
                    player);
        }

        public ErrorCatchingStack(CommandSource p_81302_, Vec3 p_81303_, Vec2 p_81304_, ServerLevel p_81305_, int p_81306_, String p_81307_, Component p_81308_, MinecraftServer p_81309_, @Nullable Entity p_81310_) {
            super(p_81302_, p_81303_, p_81304_, p_81305_, p_81306_, p_81307_, p_81308_, p_81309_, p_81310_);
        }

        final List<Component> errors = new ArrayList<>();

        @Override
        public void sendFailure(Component failure) {
            errors.add(failure);
            super.sendFailure(failure);
        }
    }
}
