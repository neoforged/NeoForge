/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.group.Group;

/**
 * Interface with directly mutating methods for {@link TestFramework TestFrameworks}.
 * 
 * @see FrameworkConfiguration#create()
 * @see TestFrameworkImpl
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface MutableTestFramework extends TestFramework {
    Codec<TestFramework> REFERENCE_CODEC = ResourceLocation.CODEC.xmap(
            rl -> TestFrameworkImpl.FRAMEWORKS.stream()
                    .filter(testFramework -> testFramework.id().equals(rl))
                    .findFirst()
                    .orElseThrow(),
            TestFramework::id);

    FrameworkConfiguration configuration();

    void init(final IEventBus modBus, final ModContainer container);

    void registerCommands(LiteralArgumentBuilder<CommandSourceStack> node);

    List<Test> collectTests(final ModContainer container);

    PlayerTestStore playerTestStore();

    String commandName();

    default String buildCommand(String subCommand) {
        return "/" + commandName() + " " + subCommand;
    }

    default ClickEvent setStatusCommand(String testId, Test.Result result, String message) {
        if (message.isBlank()) {
            return new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    buildCommand("status set \"" + testId + "\" " + result));
        } else {
            return new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    buildCommand("status set \"" + testId + "\" " + result + " " + message));
        }
    }

    default ClickEvent enableCommand(String id) {
        return new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                buildCommand("enable " + id));
    }

    default ClickEvent disableCommand(String id) {
        return new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                buildCommand("disable " + id));
    }

    @Override
    MutableTests tests();

    interface MutableTests extends Tests {
        void initialiseDefaultEnabledTests();

        Stream<Test> enabled();

        Optional<Group> maybeGetGroup(String id);

        void setStatus(String testId, Test.Status status);
    }
}
