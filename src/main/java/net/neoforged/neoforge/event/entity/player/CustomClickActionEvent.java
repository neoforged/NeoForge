/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.CommonHooks;
import org.jspecify.annotations.Nullable;

/**
 * This event is fired when a player sends a {@link ServerboundCustomClickActionPacket} to the server.
 * <p>
 * This packet is sent by the client when a {@link ClickEvent.Custom} is clicked, be it from a component or a dialog.
 * <p>
 * This event is fired only on the logical server.
 */
public class CustomClickActionEvent extends Event {
    private final ServerPlayer player;
    private final Identifier identifier;
    private final Tag payload;

    /**
     * Fired via {@link CommonHooks#onCustomClickAction(ServerPlayer, ServerboundCustomClickActionPacket)}
     */
    public CustomClickActionEvent(ServerPlayer player, Identifier identifier, @Nullable Tag payload) {
        this.player = player;
        this.identifier = identifier;
        this.payload = payload;
    }

    /**
     * @return the player who clicked this custom click event
     */
    public ServerPlayer getPlayer() {
        return player;
    }

    /**
     * @return the custom click event's identifier
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * @return the custom click event's payload
     */
    @Nullable
    public Tag getPayload() {
        return payload;
    }
}
