/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.filters;

import com.google.common.collect.ImmutableMap;
import io.netty.channel.ChannelPipeline;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.network.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkFilters {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Map<String, Function<Connection, VanillaPacketFilter>> instances = ImmutableMap.of(
            "neoforge:vanilla_filter", manager -> new VanillaConnectionNetworkFilter(),
            "neoforge:forge_fixes", NeoForgeConnectionNetworkFilter::new);

    public static void injectIfNecessary(Connection manager) {
        ChannelPipeline pipeline = manager.channel().pipeline();
        if (pipeline.get("packet_handler") == null)
            return; // Realistically this can only ever be null if the connection was prematurely closed due to an error. We return early here to reduce further log spam.

        instances.forEach((key, filterFactory) -> {
            VanillaPacketFilter filter = filterFactory.apply(manager);
            if (filter.isNecessary(manager)) {
                pipeline.addBefore("packet_handler", key, filter);
                LOGGER.debug("Injected {} into {}", filter, manager);
            }
        });
    }

    private NetworkFilters() {}

}
