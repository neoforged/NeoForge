/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import com.mojang.logging.LogUtils;
import net.minecraft.network.PacketListener;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

@ApiStatus.Internal
public interface QueuedPacket {
    void handle();

    record CustomPayload(PacketListener listener, Runnable task) implements QueuedPacket {
        private static final Logger LOGGER = LogUtils.getLogger();

        @Override
        public void handle() {
            if (listener.isAcceptingMessages()) {
                task.run();
            } else {
                LOGGER.debug("Ignoring payload task due to disconnection");
            }
        }
    }
}
