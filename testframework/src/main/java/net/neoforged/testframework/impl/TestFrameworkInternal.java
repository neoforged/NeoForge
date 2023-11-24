package net.neoforged.testframework.impl;

import net.neoforged.testframework.Test;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.group.Group;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Interface with internal methods for {@link TestFramework TestFrameworks}.
 * @see FrameworkConfiguration#create()
 * @see TestFrameworkImpl
 */
@ApiStatus.Internal
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface TestFrameworkInternal extends TestFramework {
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
                    buildCommand("status set \"" + testId + "\" " + result)
            );
        } else {
            return new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    buildCommand("status set \"" + testId + "\" " + result + " " + message)
            );
        }
    }
    default ClickEvent enableCommand(String id) {
        return new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                buildCommand("enable " + id)
        );
    }
    default ClickEvent disableCommand(String id) {
        return new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                buildCommand("disable " + id)
        );
    }

    @Override
    TestsInternal tests();

    @ApiStatus.Internal
    interface TestsInternal extends Tests {
        void initialiseDefaultEnabledTests();
        Stream<Test> enabled();
        Optional<Group> maybeGetGroup(String id);
        void setStatus(String testId, Test.Status status);
    }
}
