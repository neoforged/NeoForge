/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// This event is fired when a player sends a [ServerboundCustomClickActionPacket] to the server or when a sign with the custom click event action is clicked.
/// 
/// This packet is sent by the client when a [ClickEvent.Custom] is clicked, be it from a component or a dialog.
/// 
/// This event is fired only on the logical server.
/// 
/// This event should be cancelled when handled
public class CustomClickActionEvent extends Event implements ICancellableEvent {
    @Nullable
    private final Player player;
    private final Identifier identifier;
    @Nullable
    private final Tag payload;

    @ApiStatus.Internal
    public CustomClickActionEvent(@Nullable Player player, Identifier identifier, @Nullable Tag payload) {
        this.player = player;
        this.identifier = identifier;
        this.payload = payload;
    }

    /// {@return the player who clicked this custom click event or null if the custom click event is received during configuration}
    @Nullable
    public Player getPlayer() {
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
