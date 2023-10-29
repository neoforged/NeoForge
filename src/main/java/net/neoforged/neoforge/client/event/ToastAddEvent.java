/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.gui.components.toasts.Toast;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Fired when the client queues a {@link Toast} message to be shown onscreen.
 * Toasts are small popups that appear on the top right of the screen, for certain actions such as unlocking Advancements and Recipes.
 *
 * <p>This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 * Cancelling the event stops the toast from being queued, which means it never renders.</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class ToastAddEvent extends Event implements ICancellableEvent {
    private final Toast toast;

    public ToastAddEvent(Toast toast) {
        this.toast = toast;
    }

    public Toast getToast() {
        return toast;
    }
}
