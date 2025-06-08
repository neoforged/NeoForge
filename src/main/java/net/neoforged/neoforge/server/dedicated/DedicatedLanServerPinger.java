/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.dedicated;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.neoforged.neoforge.network.DualStackUtils;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

@ApiStatus.Internal
// Unlike LanServerPinger, this is present on the server. This should look practically identical to what LanServerPinger does
public class DedicatedLanServerPinger extends Thread {
    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String MULTICAST_GROUP = DualStackUtils.getMulticastGroup();
    private final String motd;
    private final DatagramSocket socket;
    private boolean isRunning = true;
    private final String serverAddress;

    public DedicatedLanServerPinger(String motd, String address) throws IOException {
        super("DedicatedLanServerPinger #" + UNIQUE_THREAD_ID.incrementAndGet());
        this.motd = motd;
        this.serverAddress = address;
        this.setDaemon(true);
        this.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        this.socket = new DatagramSocket();
    }

    @Override
    public void run() {
        String s = createPingString(this.motd, this.serverAddress);
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);

        while (!this.isInterrupted() && this.isRunning) {
            try {
                InetAddress inetaddress = InetAddress.getByName(MULTICAST_GROUP);
                DatagramPacket datagrampacket = new DatagramPacket(bytes, bytes.length, inetaddress, 4445);
                this.socket.send(datagrampacket);
            } catch (IOException ioexception) {
                LOGGER.warn("DedicatedLanServerPinger: {}", ioexception.getMessage());
                break;
            }

            try {
                sleep(1500L);
            } catch (InterruptedException interruptedexception) {}
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        this.isRunning = false;
    }

    private static String createPingString(String motd, String address) {
        return "[MOTD]" + motd + "[/MOTD][AD]" + address + "[/AD]";
    }
}
