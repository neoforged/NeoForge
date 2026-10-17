/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import com.google.common.net.InetAddresses;
import com.mojang.logging.LogUtils;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import net.minecraft.util.HttpUtil;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class DualStackUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Checks if an address is an IPv6 one or an IPv4 one.
     *
     * @param inetAddress The address you want to check, or {@code null} if unknown
     * @return true if IPv6, false if IPv4. For an unknown address, true only if Java was explicitly told to prefer IPv6
     */
    public static boolean checkIPv6(@Nullable final InetAddress inetAddress) {
        // only log debug messages if we're not in the server pinger thread, as otherwise it's unclear which IP
        // corresponds to which server as soon as you have more than one server in the multiplayer server list
        final String currentThreadName = Thread.currentThread().getName();
        final boolean shouldLogDebug = !currentThreadName.contains("Server Pinger #");

        if (inetAddress instanceof Inet6Address addr) {
            if (shouldLogDebug)
                LOGGER.debug("Detected IPv6 address: \"" + addr.getHostAddress() + "\"");
            return true;
        } else if (inetAddress instanceof Inet4Address addr) {
            if (shouldLogDebug)
                LOGGER.debug("Detected IPv4 address: \"" + addr.getHostAddress() + "\"");
            return false;
        } else {
            if (shouldLogDebug) {
                final String addr = inetAddress == null ? "null" : "\"" + inetAddress.getHostAddress() + "\"";
                LOGGER.debug("Unable to determine IP version of address: " + addr);
            }

            if (!Boolean.getBoolean("java.net.preferIPv4Stack") && Boolean.getBoolean("java.net.preferIPv6Addresses")) {
                if (shouldLogDebug)
                    LOGGER.debug("Assuming IPv6 as Java was explicitly told to prefer it...");
                return true;
            }

            if (shouldLogDebug)
                LOGGER.debug("Assuming IPv4...");
            return false;
        }
    }

    /**
     * Get the device's local IP address, taking into account scenarios where the client's network adapter
     * supports IPv6 and has it enabled but the router's LAN does not.
     *
     * @return the client's local IP address or {@code null} if unable to determine it
     */
    @Nullable
    public static InetAddress getLocalAddress() {
        final InetAddress localAddr = new InetSocketAddress(HttpUtil.getAvailablePort()).getAddress();
        if (localAddr.isAnyLocalAddress()) return localAddr;

        try {
            return InetAddress.getByName("localhost");
        } catch (final UnknownHostException e) {
            return null;
        }
    }

    /**
     * Used for the "Open to LAN" feature.
     * 
     * @return The multicast group to use for LAN discovery - IPv6 if available, IPv4 otherwise.
     */
    public static String getMulticastGroup() {
        if (checkIPv6(getLocalAddress())) return "FF75:230::60";
        else return "224.0.2.60";
    }

    /**
     * {@link SocketAddress#toString()} but with IPv6 address compression support
     */
    public static String getAddressString(final SocketAddress address) {
        if (address instanceof final InetSocketAddress inetAddress) {
            String formatted;
            if (inetAddress.isUnresolved()) {
                formatted = inetAddress.getHostName() + "/<unresolved>";
            } else {
                formatted = InetAddresses.toAddrString(inetAddress.getAddress());
                if (inetAddress.getAddress() instanceof Inet6Address)
                    formatted = '[' + formatted + ']';

                formatted = '/' + formatted;
            }

            return formatted + ':' + inetAddress.getPort();
        }

        return address.toString();
    }
}
