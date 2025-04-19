/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TheGame;
import net.neoforged.bus.api.Event;

public abstract class ServerLifecycleEvent extends Event {
    protected final MinecraftServer server;
    protected final TheGame game;

    public ServerLifecycleEvent(MinecraftServer server) {
        this.server = server;
        this.game = server.theGame();
    }

    public ServerLifecycleEvent(TheGame game) {
        this.server = game.server();
        this.game = game;
    }

    public MinecraftServer getServer() {
        return server;
    }

    public TheGame getTheGame() {
        return game;
    }
}
