/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.neoforged.bus.api.Event;

/**
 * Fires after the client has completed loading or reloading its resources
 * successfully.
 * <p>
 * When the client first starts up, this event is fired after the resource
 * load, but before the client has set up the initial screens, and before
 * we emit the first {@link ClientTickEvent}.
 */
public class ClientResourceLoadFinishedEvent extends Event {
    private final boolean initial;

    public ClientResourceLoadFinishedEvent(boolean initial) {
        this.initial = initial;
    }

    /**
     * @return True if the reload that completed was the initial resource load of the client.
     */
    public boolean isInitial() {
        return initial;
    }
}
