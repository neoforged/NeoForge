/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.common.MinecraftForge;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import org.jetbrains.annotations.Nullable;

/**
 * CommandEvent is fired after a command is parsed, but before it is executed.
 * This event is fired during the invocation of {@link Commands#performCommand(ParseResults, String)}.
 * <p>
 * This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 * If the event is cancelled, the command will not be executed.
 * <p>
 * This event is fired on the {@linkplain MinecraftForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#SERVER logical server}.
 **/
public class CommandEvent extends Event implements ICancellableEvent
{
    private ParseResults<CommandSourceStack> parse;
    @Nullable
    private Throwable exception;

    public CommandEvent(ParseResults<CommandSourceStack> parse)
    {
        this.parse = parse;
    }

    /**
     * {@return the parsed command results}
     */
    public ParseResults<CommandSourceStack> getParseResults()
    {
        return this.parse;
    }

    public void setParseResults(ParseResults<CommandSourceStack> parse)
    {
        this.parse = parse;
    }

    /**
     * {@return an exception to be thrown when performing the command, starts null}
     */
    @Nullable
    public Throwable getException()
    {
        return this.exception;
    }

    public void setException(@Nullable Throwable exception)
    {
        this.exception = exception;
    }
}
