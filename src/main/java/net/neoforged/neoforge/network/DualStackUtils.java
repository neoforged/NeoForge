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
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class DualStackUtils {
    private static final String INITIAL_PREFER_IPv4_STACK = System.getProperty("java.net.preferIPv4Stack") == null ? "false" : System.getProperty("java.net.preferIPv4Stack");
    private static final String INITIAL_PREFER_IPv6_ADDRESSES = System.getProperty("java.net.preferIPv6Addresses") == null ? "false" : System.getProperty("java.net.preferIPv6Addresses");

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Called by {@link NeoForge} to capture the initial networking preferences before checking addresses.
     * These values are used for diagnostics and when an address's protocol family is unknown.
     */
    @ApiStatus.Internal
    public static void initialise() {}

    /**
     * Checks whether an address is IPv6 without changing the JVM's networking preferences.
     * Those preferences are process-wide and may be cached by Java and Netty, so checking one address
     * must not change how other connections resolve addresses or create sockets.
     *
     * @param inetAddress the address to check, or {@code null} if no address is known
     * @return true for IPv6, false for IPv4; the initial preferences are used for an unknown address
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

            if (INITIAL_PREFER_IPv4_STACK.equalsIgnoreCase("false") && INITIAL_PREFER_IPv6_ADDRESSES.equalsIgnoreCase("true")) {
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
     * Logs the initial values of the {@code java.net.preferIPv4Stack} and {@code java.net.preferIPv6Addresses} system
     * properties that Java has read on JVM start. Useful for debugging hostname lookup failures.
     */
    public static void logInitialPreferences() {
        LOGGER.debug("Initial IPv4 stack preference: " + INITIAL_PREFER_IPv4_STACK);
        LOGGER.debug("Initial IPv6 addresses preference: " + INITIAL_PREFER_IPv6_ADDRESSES);
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
