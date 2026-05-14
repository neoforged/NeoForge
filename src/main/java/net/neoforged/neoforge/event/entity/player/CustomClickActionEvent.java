/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// Fired when a component's [custom click action][ClickEvent.Custom] is triggered by a player. 
/// This may be due to clicking a sign, or a component in a dialog box or screen.
/// 
/// If an event handler receives this and performs its own custom action, that event handler **must cancel** this event.
/// 
/// This event is fired only on the logical server.
/// 
/// This event is [cancelable][ICancellableEvent]. If canceled, vanilla's processing of the custom click payload is skipped.
public class CustomClickActionEvent extends Event implements ICancellableEvent {
    @Nullable
    private final ServerPlayer player;
    private final Identifier identifier;
    @Nullable
    private final Tag payload;

    @ApiStatus.Internal
    public CustomClickActionEvent(@Nullable ServerPlayer player, Identifier identifier, @Nullable Tag payload) {
        this.player = player;
        this.identifier = identifier;
        this.payload = payload;
    }

    /// {@return the player who clicked this custom click event or null if the custom click event is received during configuration}
    @Nullable
    public ServerPlayer getPlayer() {
        return player;
    }

    /// {@return the custom click event's identifier}
    public Identifier getIdentifier() {
        return identifier;
    }

    /// {@return the custom click event's payload}
    @Nullable
    public Tag getPayload() {
        return payload;
    }
}
