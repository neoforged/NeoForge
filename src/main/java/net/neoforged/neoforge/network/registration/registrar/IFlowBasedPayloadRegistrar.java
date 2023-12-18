/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration.registrar;

import net.minecraft.network.protocol.PacketFlow;

/**
 * A registrar that is bound to a specific {@link PacketFlow}.
 */
public interface IFlowBasedPayloadRegistrar extends IPayloadRegistrar {
    
    /**
     * Forces all payloads registered with this registrar to be only capable of flowing in a given packet flow.
     *
     * @param flow The flow.
     * @return A registrar that is bound to the given flow.
     */
    IPayloadRegistrarWithAcceptableRange flowing(PacketFlow flow);
}
